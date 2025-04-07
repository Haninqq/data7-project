from fastapi import FastAPI
from pydantic import BaseModel
import openai

app = FastAPI()

openai.api_key = "키 여기다가"

class RequestData(BaseModel):
    # 여기에 뭐 필요할지는 생각해봐야 함
    toolName: str
    toolDesc: str
    goal: str


@app.post("/generate-goal")
def generation(req: RequestData):
    response = openai.Completion.create(
        engine="text-davinci-003", # 여기 모델 생각해봐야 함
        prompt=""""
        
        프롬프트 여기에..

        Tool: # ahr

        """, #목표, ADT, 6 VERB
        max_tokens=100
    )
    return response.choices[0].text


@app.get("/")
def read_root():
    return {"Hello": "World"}
