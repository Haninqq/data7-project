# FastAPI Dockerfile
FROM python:3.11-slim
WORKDIR /app
COPY . .
RUN pip install --no-cache-dir fastapi uvicorn
CMD ["uvicorn", "openAI:app", "--host", "0.0.0.0", "--port", "8000"]