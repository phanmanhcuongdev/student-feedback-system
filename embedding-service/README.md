# Local Embedding Service

FastAPI service for local text embeddings used by the backend AI summary novelty scoring.

## Run

```powershell
python -m venv .venv
.\.venv\Scripts\pip install -r requirements.txt
.\.venv\Scripts\uvicorn app.main:app --host 127.0.0.1 --port 8000
```

Enable the backend client with:

```powershell
$env:APP_AI_EMBEDDING_ENABLED="true"
$env:APP_AI_EMBEDDING_BASE_URL="http://127.0.0.1:8000"
$env:APP_AI_EMBEDDING_MODEL="intfloat/multilingual-e5-small"
```

## API

`POST /embed`

```json
{
  "texts": ["noi dung can cai thien"]
}
```

Response:

```json
{
  "model": "intfloat/multilingual-e5-small",
  "embeddings": [[0.1, 0.2]]
}
```
