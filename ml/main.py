import re

import joblib
from fastapi import FastAPI
from pydantic import BaseModel

MODEL_PATH = "model.pkl"
VECTORIZER_PATH = "vectorizer.pkl"

app = FastAPI(title="Resume Screener ML Service")

model = joblib.load(MODEL_PATH)
vectorizer = joblib.load(VECTORIZER_PATH)


class PredictionRequest(BaseModel):
    resumeText: str
    jobDescription: str


class PredictionResponse(BaseModel):
    interviewProbability: float
    matchScore: float
    modelVersion: str
    explanation: str


def clean_text(text):
    text = str(text).lower()
    text = re.sub(r"[^a-z0-9\s]", " ", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/predict", response_model=PredictionResponse)
def predict(request: PredictionRequest):
    combined_text = clean_text(request.resumeText) + " " + clean_text(request.jobDescription)
    vectorized_text = vectorizer.transform([combined_text])

    probability = model.predict_proba(vectorized_text)[0][1]
    match_score = round(probability * 100, 2)

    return PredictionResponse(
        interviewProbability=round(float(probability), 4),
        matchScore=match_score,
        modelVersion="tfidf-logistic-regression-v1",
        explanation="Score generated using TF-IDF text features and a scikit-learn classification model."
    )