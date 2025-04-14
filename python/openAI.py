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

# ------------------------------
# FastAPI 앱 & DB 설정
# ------------------------------
app = FastAPI()

load_dotenv()  
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))  

SQLALCHEMY_DATABASE_URL = "mysql+pymysql://root:1234@localhost/instdesign"
engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"charset": "utf8mb4"})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

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

# ------------------------------
# 요청 데이터 모델
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
# Sentence-BERT 모델 로딩 (서버 시작 시 1회)
# ------------------------------
model = SentenceTransformer("snunlp/KR-SBERT-V40K-klueNLI-augSTS")

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
# ADT기준 tool 가져오기기
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

    return [
        {
            "tool_name": tool_name,
            "tool_description": tool_description
        }
        for tool_name, tool_description in results
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
@app.post("/submit/")
def generate_similar_adts(req: RequestData, db: Session = Depends(get_db)):
    adt_items = get_adt_items(db)
    results = find_similar_adts(req.goal.strip(), adt_items)
    
    adtList = [result["ADT"] for result in results]
    tools = get_tools(db, adtList) # tool 가져옴
    logging.info(f"Tools: {tools}")


    prompt = f"""
## Learning Objective:
"{req.goal}"

## Bloom's Taxonomy Matches:
Verbs: {results[0]['BloomTaxonomy']}, {results[1]['BloomTaxonomy']}, {results[2]['BloomTaxonomy']}        

ADTs: {results[0]['ADT']}, {results[1]['ADT']}, {results[2]['ADT']}

Descriptions: {results[0]['ADTDesc']}, {results[1]['ADTDesc']}, {results[2]['ADTDesc']} 

## Suggested Tools:{tools}

Design 3 classroom-friendly, cognitively aligned digital learning activities based on the information above.

Each activity must include:
- Directly reflect the ADT: '{results[0]["ADT"]}' (which means: '{results[0]["ADTDesc"]}')
- Be engaging, digital-first, and cognitively aligned
- Use one or more of the suggested tools
- Use tools that are appropriate for the activity
- Be unique and thought-provoking, not generic
- Return the output as JSON

Respond in valid JSON
"""

    # GPT-4o-mini 호출
    response = client.responses.create(
        model="gpt-4o-mini",
        instructions=f"You are an expert instructional designer for {req.grade} students in the subject of {req.subject}.",
        input=prompt,
        temperature=0.7,
        max_output_tokens=700
    )

    return response.output_text 


    
    

@app.get("/")
def root():
    return {"message": "Bloom 유사도 분석 API입니다."}