from fastapi import FastAPI, Depends
from pydantic import BaseModel
from sqlalchemy import create_engine, Column, Integer, String, ForeignKey
from sqlalchemy.orm import sessionmaker, Session, relationship
from sqlalchemy.ext.declarative import declarative_base
from sentence_transformers import SentenceTransformer, util

# ------------------------------
# FastAPI 앱 & DB 설정
# ------------------------------
app = FastAPI()

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
# 유사도 분석 함수
# ------------------------------
def find_similar_adts(user_input: str, adt_items: list, top_n: int = 5):
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
    return {"results": results}

@app.get("/")
def root():
    return {"message": "Bloom 유사도 분석 API입니다."}