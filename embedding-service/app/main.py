from functools import lru_cache
from typing import List

from fastapi import FastAPI
from pydantic import BaseModel, Field
from sentence_transformers import SentenceTransformer

MODEL_NAME = "intfloat/multilingual-e5-small"

app = FastAPI(title="TTCS Local Embedding Service")


class EmbedRequest(BaseModel):
    texts: List[str] = Field(default_factory=list, max_length=256)


class EmbedResponse(BaseModel):
    model: str
    embeddings: List[List[float]]


@lru_cache(maxsize=1)
def model() -> SentenceTransformer:
    return SentenceTransformer(MODEL_NAME)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "model": MODEL_NAME}


@app.post("/embed", response_model=EmbedResponse)
def embed(request: EmbedRequest) -> EmbedResponse:
    texts = [text.strip() for text in request.texts if text and text.strip()]
    if not texts:
        return EmbedResponse(model=MODEL_NAME, embeddings=[])

    vectors = model().encode(
        texts,
        normalize_embeddings=True,
        convert_to_numpy=True,
    )
    return EmbedResponse(
        model=MODEL_NAME,
        embeddings=[vector.astype(float).tolist() for vector in vectors],
    )
