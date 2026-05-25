import os
import random
import re

import joblib
import pandas as pd
from datasets import load_dataset
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, classification_report
from sklearn.model_selection import train_test_split

os.environ["HF_HUB_DISABLE_SYMLINKS_WARNING"] = "1"
os.environ["HF_HUB_DISABLE_XET"] = "1"

MODEL_PATH = "model.pkl"
VECTORIZER_PATH = "vectorizer.pkl"
RANDOM_STATE = 42


def clean_text(text):
    text = str(text).lower()
    text = re.sub(r"[^a-z0-9\s]", " ", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text


def list_to_text(value):
    if isinstance(value, list):
        return " ".join(str(item) for item in value)
    return str(value)


def build_resume_text(row):
    return " ".join([
        str(row["role"]),
        str(row["seniority"]),
        str(row["industry"]),
        str(row["education"]),
        list_to_text(row["skills"]),
        str(row["summary"]),
        list_to_text(row["experience_bullets"]),
    ])


def build_job_description(row):
    skills = list_to_text(row["skills"])
    return (
        f"We are hiring a {row['seniority']} {row['role']} for the {row['industry']} industry. "
        f"The ideal candidate has experience with {skills}. "
        f"Education preferred: {row['education']}."
    )


def main():
    print("Loading dataset...")
    ds = load_dataset("michaelozon/candidate-matching-synthetic")
    resumes = ds["resumes"].to_pandas()

    resumes = resumes.sample(n=min(1000, len(resumes)), random_state=RANDOM_STATE).reset_index(drop=True)

    positive_rows = []
    negative_rows = []

    for index, row in resumes.iterrows():
        resume_text = build_resume_text(row)
        matching_job = build_job_description(row)

        positive_rows.append({
            "resume_text": resume_text,
            "job_description": matching_job,
            "label": 1
        })

        negative_index = random.choice([i for i in range(len(resumes)) if i != index])
        negative_row = resumes.iloc[negative_index]
        non_matching_job = build_job_description(negative_row)

        negative_rows.append({
            "resume_text": resume_text,
            "job_description": non_matching_job,
            "label": 0
        })

    training_data = pd.DataFrame(positive_rows + negative_rows)
    training_data = training_data.sample(frac=1, random_state=RANDOM_STATE).reset_index(drop=True)

    training_data["combined_text"] = (
        training_data["resume_text"].apply(clean_text)
        + " "
        + training_data["job_description"].apply(clean_text)
    )

    X = training_data["combined_text"]
    y = training_data["label"]

    vectorizer = TfidfVectorizer(max_features=3000)
    X_vectorized = vectorizer.fit_transform(X)

    X_train, X_test, y_train, y_test = train_test_split(
        X_vectorized,
        y,
        test_size=0.2,
        random_state=RANDOM_STATE,
        stratify=y
    )

    model = LogisticRegression(max_iter=1000)
    model.fit(X_train, y_train)

    predictions = model.predict(X_test)

    print("Accuracy:", accuracy_score(y_test, predictions))
    print(classification_report(y_test, predictions))

    joblib.dump(model, MODEL_PATH)
    joblib.dump(vectorizer, VECTORIZER_PATH)

    print(f"Saved {MODEL_PATH}")
    print(f"Saved {VECTORIZER_PATH}")


if __name__ == "__main__":
    main()