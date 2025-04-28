from fastapi import FastAPI, Depends
from pydantic import BaseModel
from sqlalchemy import create_engine, Column, Integer, String, ForeignKey
from sqlalchemy.orm import sessionmaker, Session, relationship
from sqlalchemy.ext.declarative import declarative_base
from sentence_transformers import SentenceTransformer, util
import os
from dotenv import load_dotenv
from openai import OpenAI
import logging
import json
from fastapi.responses import JSONResponse
import pandas as pd
from contextlib import asynccontextmanager


# ------------------------------
# FastAPI 앱 & DB 설정 & SBERT 모델 로딩
# ------------------------------

app = None
# 전역변수 선언 
content_df = None
content_embeddings = None
# SBERT 모델 로딩
model = None

# OpenAI API 키 로딩
load_dotenv()  
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))  


# DB 설정
SQLALCHEMY_DATABASE_URL = "mysql+pymysql://root:1234@localhost/instdesign"
engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"charset": "utf8mb4"})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Lifespan 설정
@asynccontextmanager
async def lifespan(app):
    global model, content_df, content_embeddings

    print("🚀 모델 및 콘텐츠 로딩 중...")
    model = SentenceTransformer('snunlp/KR-SBERT-V40K-klueNLI-augSTS')

    # ✅ 여기서 직접 DB 세션 생성
    db = SessionLocal()
    try:
        rows = db.query(Content).all()
        content_df = pd.DataFrame([{
            "id": row.id,
            "subject": row.subject,
            "topic": row.topic,
            "subtitle": row.subtitle,
            "title": row.title,
            "keywords": row.keywords,
            "url": row.url
        } for row in rows])

        content_keywords = content_df["keywords"].astype(str).fillna("").tolist()
        content_embeddings = model.encode(content_keywords, convert_to_tensor=True)

        print("✅ 콘텐츠 로딩 및 임베딩 완료")

        yield  # 여기까지 오면 서버 시작됨

    finally:
        db.close()  # ✅ 꼭 세션 닫아주기
        print("🛑 DB 세션 종료")


# 앱 선언부에서 lifespan 등록
app = FastAPI(lifespan=lifespan)

# ------------------------------
# DB 모델 정의
# ------------------------------
class TaxonomyCategory(Base):
    __tablename__ = "taxonomy_category"

    id = Column(Integer, primary_key=True, index=True)
    category = Column(String(20), nullable=False)      # 영어 이름 (예: Remembering)
    category_kor = Column(String(20), nullable=False)  # 한글 이름 (예: 기억하기)


class TaxonomyVerbs(Base):
    __tablename__ = "taxonomy_verbs"

    id = Column(Integer, primary_key=True, index=True)
    category_id = Column(Integer, ForeignKey("taxonomy_category.id"), nullable=False)
    verb_eng = Column(String(50), nullable=False)
    verb_kor = Column(String(50), nullable=False)
    desc = Column(String(200), nullable=True)

    category = relationship("TaxonomyCategory", backref="verbs")
    tool_adt_maps = relationship("ToolAdtMap", back_populates="taxonomy_verb")

class Tools(Base):
    __tablename__ = 'tools'
    
    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    description = Column(String(255), nullable=True)

    # tool_adt_map와 관계 설정 (optional)
    tool_adt_maps = relationship("ToolAdtMap", back_populates="tool")

class ToolAdtMap(Base):
    __tablename__ = 'tool_adt_map'

    tool_id = Column(Integer, ForeignKey('tools.id'), primary_key=True)
    verb_id = Column(Integer, ForeignKey('taxonomy_verbs.id'), primary_key=True)

    # 관계 설정
    tool = relationship("Tools", back_populates="tool_adt_maps")
    taxonomy_verb = relationship("TaxonomyVerbs", back_populates="tool_adt_maps")

class Content(Base):
    __tablename__ = 'content'

    id = Column(Integer, primary_key=True, autoincrement=True)
    subject = Column(String(10), nullable=True)
    topic = Column(String(25), nullable=True)
    subtitle = Column(String(50), nullable=True)
    title = Column(String(100), nullable=True)
    keywords = Column(String(400), nullable=True)
    url = Column(String(100), nullable=True)

# ------------------------------
# 요청 데이터 모델 --> Spring에서 받아오는 데이터터
# ------------------------------
class RequestData(BaseModel):
    grade: str
    subject: str
    goal: str

# ------------------------------
# DB 세션 의존성
# ------------------------------
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# ------------------------------
# ADT 항목 DB에서 가져오기
# ------------------------------
def get_adt_items(db: Session):
    results = (
        db.query(
            TaxonomyVerbs.category_id,
            TaxonomyCategory.category_kor.label("bloom_name"),
            TaxonomyVerbs.verb_kor,
            TaxonomyVerbs.desc
        )
        .join(TaxonomyCategory, TaxonomyVerbs.category_id == TaxonomyCategory.id)
        .all()
    )

    return [
        {
            "category_id": category_id,
            "bloom_name": bloom_name,
            "verb": verb,
            "desc": desc
        }
        for category_id, bloom_name, verb, desc in results
        if verb and desc
    ]

# ------------------------------
# ADT기준 tool 가져오기
# ------------------------------

def get_tools(db: Session, adt_values: list):
    results = (
        db.query(
            Tools.name.label("tool_name"),
            Tools.description.label("tool_description")
        )
        .join(ToolAdtMap, Tools.id == ToolAdtMap.tool_id) 
        .join(TaxonomyVerbs, TaxonomyVerbs.id == ToolAdtMap.verb_id)  
        .filter(TaxonomyVerbs.verb_kor.in_(adt_values))  
        .all()
    )

    # tool_name 기준으로 중복 제거 (가장 먼저 나온 걸 채택)
    tool_dict = {}
    for tool_name, tool_description in results:
        if tool_name not in tool_dict:
            tool_dict[tool_name] = tool_description

    # dict를 리스트로 변환
    return [
        {
            "tool_name": tool_name,
            "tool_description": tool_description
        }
        for tool_name, tool_description in tool_dict.items()
    ]

# ------------------------------
# 콘텐츠 유사도 분석 함수
# ------------------------------
# 과목 매핑 테이블
subject_map = {
    "국어": None, "수학": None, "바른 생활": None, "슬기로운 생활": None, "즐거운 생활": None,
    "사회": "사회", "도덕": "사회", "역사": "사회",
    "과학": "과학", "과학/기술": "과학",
    "가정/정보": "기술·가정·실과", "기가/정보": "기술·가정·실과", "기술가정/정보": "기술·가정·실과",
    "기술·가정·실과": "기술·가정·실과", "체육": "체육",
    "예술": None, "음악": "음악", "미술": "미술", "예술(음악/미술)": "음악",
    "영어": None, "한국사": "사회", "제2외국어/한문": None
}

def recommend_learning_content(
    input_learning_objective: str,
    user_subject: str,
    model,
    content_df,
    content_embeddings,
    top_n: int = 3,
    threshold: float = 0.5
):
    # description
    """
    사용자의 학습 목표 문장과 과목명을 기반으로 관련 콘텐츠 추천
    - input_learning_objective: 사용자 입력 문장
    - user_subject: '가정/정보'와 같은 실제 사용자 입력
    - model: SBERT 모델
    - content_df: 전체 콘텐츠 DataFrame
    - content_embeddings: SBERT 임베딩된 벡터 (Tensor)
    - top_n: 추천 개수
    - threshold: 유사도 기준 (기본 0.5 이상만 추천)

    Returns: 추천 결과 리스트 or 에러 메시지 dict
    """

    # 과목 매핑
    mapped_subject = subject_map.get(user_subject)
    if not mapped_subject:
        return {"message": f"'{user_subject}' 과목은 콘텐츠 DB에 존재하지 않거나 매핑할 수 없습니다."}

    # 과목 필터링
    filtered_df = content_df[content_df["subject"] == mapped_subject].copy()
    if filtered_df.empty:
        return {"message": f"매핑된 과목 '{mapped_subject}'에 해당하는 콘텐츠가 없습니다."}

    # 입력 문장 벡터화
    input_embedding = model.encode(input_learning_objective, convert_to_tensor=True)

    # 임베딩 슬라이싱 (Tensor 기반)
    filtered_embeddings = content_embeddings[filtered_df.index.tolist()]

    # 코사인 유사도 계산
    similarities = util.cos_sim(input_embedding, filtered_embeddings)[0]
    filtered_df["Similarity"] = similarities.cpu().numpy()

    # 유사도 기준 이상 콘텐츠 상위 N개 정렬
    top_contents = filtered_df[filtered_df["Similarity"] >= threshold] \
        .sort_values(by="Similarity", ascending=False).head(top_n)

    if top_contents.empty:
        return {"message": f"유사한 콘텐츠가 없습니다. (유사도 {threshold} 이상 없음)"}

    # 결과 포맷 구성
    return [
        {
            "topic": row["topic"],
            "subtitle": row["subtitle"].strip(),
            "title": row["title"],
            "url": row["url"],
            "similarity": round(row["Similarity"], 4)
        }
        for _, row in top_contents.iterrows()
    ]



# ------------------------------
# 유사도 분석 함수
# ------------------------------
def find_similar_adts(user_input: str, adt_items: list, top_n: int = 3):
    adt_texts = [f"{item['verb']} - {item['desc']}" for item in adt_items]
    user_embedding = model.encode(user_input, convert_to_tensor=True)
    adt_embeddings = model.encode(adt_texts, convert_to_tensor=True)

    similarities = util.cos_sim(user_embedding, adt_embeddings)[0]
    top_results = similarities.topk(k=top_n)

    results = []
    for score, idx in zip(top_results.values, top_results.indices):
        item = adt_items[idx]
        results.append({
            "BloomTaxonomy": f"{item['bloom_name']}",
            "ADT": f"{item['verb']}",
            "ADTDesc": f"{item['desc']}",
            "Similarity": float(score)
        })

    return results

# ------------------------------
# 라우터 정의
# ------------------------------
@app.post("/submit")
def generate_similar_adts(req: RequestData, db: Session = Depends(get_db)):

    adt_items = get_adt_items(db)
    results = find_similar_adts(req.goal.strip(), adt_items)
    
    adtList = [result["ADT"] for result in results]
    tools = get_tools(db, adtList) # tool 가져옴
    print("tools", tools)
    contentResults = recommend_learning_content(req.goal.strip(), req.subject, model, content_df, content_embeddings)


    prompt = f"""
You must generate learning activities based on the instructional information provided in each user input.

### Instructional Info:
- Subject: {req.subject}
- Grade: {req.grade}
- Learning Objective: {req.goal}

### Cognitive Elements:
- Bloom Levels: {results[0]['BloomTaxonomy']}, {results[1]['BloomTaxonomy']}, {results[2]['BloomTaxonomy']}
- ADT_kor: {results[0]['ADT']}, {results[1]['ADT']}, {results[2]['ADT']}
- ADT_desc: {results[0]['ADTDesc']}, {results[1]['ADTDesc']}, {results[2]['ADTDesc']}

### Tool List: {tools}

--- 

You are tasked with creating digital learning activities based on the given instructional info, cognitive elements, and tool list.

Here are two examples:

Example 1:
{{
"activity_title": "기후 데이터 분석",
"tool_name": "네이버 datalab",
"activity_desc": "학생들은 네이버 datalab에 접속하여 지역별 기온, 강수량, 기후 변화 데이터를 검색한다. 검색한 데이터를 바탕으로 각 조별로 특정 지역을 선정하고, 기온 및 강수량 변화를 표로 정리한다. 조별로 정리한 데이터를 분석하여 해당 지역의 기후 변화 특성과 생물 다양성에 미치는 영향을 토의한다. 마지막으로 분석 결과를 발표 자료로 정리하여 전체 앞에서 발표하고, 지역 간 기후 차이와 생물 다양성 문제를 종합적으로 논의한다.",
"activity_sentence": "네이버 datalab을 활용하여 기후 데이터 분석을 해본다."
}}

Example 2:
{{
"activity_title": "위성 지도 생물 관찰",
"tool_name": "구글어스",
"activity_desc": "학생들은 구글어스를 이용해 학교 주변 또는 관심 지역을 탐색한다. 탐색한 지역의 주요 지형적 특성을 조사하고, 그 지역에 서식하는 생물 종을 검색하여 정리한다. 이후 지형과 생물 분포의 관계를 분석하여 구글어스 지도 위에 표시하고, 조별로 포스터를 제작해 지역별 생물 다양성의 특징을 설명한다. 제작한 포스터를 바탕으로 조별 발표를 진행한다.",
"activity_sentence": "구글어스를 활용하여 위성 지도 생물 관찰을 해본다."
}}

Now, based on the provided instructional information, cognitive elements, and tool list, generate 3 new and distinct activities.

Each activity_desc must describe the activity in a natural, detailed, step-by-step manner without explicitly numbering steps, so that teachers can immediately apply it in a real classroom setting.

Follow the output rules and format.
"""



    


    
    instruction = f"""
You must always follow the output Rules and output format when generating learning activities.

### Output Rules:
You must follow these rules:
1. Generate exactly 3 distinct digital learning activities.
2. For each activity, select exactly one appropriate tool from the given tool list.
3. Output must be in JSON format.
4. Each JSON object must include the following keys: activity_title, tool_name, activity_desc, activity_sentence.
5. All values must be written in Korean.
6. Do not include any extra explanation or commentary outside of the JSON array.
7. Do not use quotation marks (" ") or apostrophes (' ') inside any field values.

Always return output in the following format:

### Output Format (in Korean):
[
{{
    "activity_title": "",
    "tool_name": "",
    "activity_desc": "",
    "activity_sentence": "[tool_name]을 활용하여 [activity_title]을 해본다."
}},
...
]
"""

    # GPT-4o-mini 호출
    response = client.responses.create(
        model="gpt-4.1",
        input=prompt,
        instructions=instruction,
        temperature=0.4,
        max_output_tokens=2048,
        top_p=1
    )

    try:
        parsed_output = json.loads(response.output_text)  # GPT 응답 파싱

        # ✅ SBERT 추천 결과 호출
        contentResults = recommend_learning_content(
            input_learning_objective=req.goal.strip(),
            user_subject=req.subject,
            model=model,
            content_df=content_df,
            content_embeddings=content_embeddings
        )

        if isinstance(contentResults, dict) and "message" in contentResults:
            return JSONResponse(content={
                "gptResults": parsed_output,
                "contentError": contentResults["message"]
            })

        # ✅ 두 결과를 합쳐서 응답
        return JSONResponse(content={
            "gptResults": parsed_output,
            "contentResults": contentResults
        })

    except json.JSONDecodeError as e:
        logging.error("JSON 파싱 실패", exc_info=True)
        return JSONResponse(status_code=500, content={"error": "OpenAI 응답 파싱 오류"})

    
    

@app.get("/")
def root():
    return {"message": "Bloom 유사도 분석 API입니다."}