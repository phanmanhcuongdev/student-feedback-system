# Survey AI Summary

## Muc tieu

Tinh nang nay tom tat y kien dang text cua sinh vien theo tung khao sat rieng, chi duoc kich hoat khi nguoi dung chu dong goi.

## Nguyen tac

- Khong cham vao API `survey result detail` cu.
- Khong generate summary khi mo trang ket qua.
- Chi tao job khi client goi endpoint generate.
- Neu du lieu comment chua doi, he thong tra lai summary cu theo `source_hash`.

## Endpoint moi

- `GET /api/v1/survey-results/{surveyId}/ai-summary`
  - Lay trang thai hien tai, loi gan nhat, va summary neu da co.
- `POST /api/v1/survey-results/{surveyId}/ai-summary/generate`
  - Tao job tom tat neu can.
  - Neu summary cu van con hop le, tra lai summary da cache.

## Bien moi truong

- `APP_AI_API_KEY`
- `APP_AI_BASE_URL`
- `APP_AI_MODEL`

Mac dinh hien tai da huong den Gemini API qua endpoint OpenAI compatibility chinh thuc:

- `APP_AI_BASE_URL=https://generativelanguage.googleapis.com/v1beta/openai/chat/completions`
- `APP_AI_MODEL=gemini-2.5-flash-lite`
- `APP_AI_API_KEY=<Gemini API key>`

Mau bien da co san trong `.env.example` va `.env.openai.example`.

## Cach chay bang Gemini free tier

1. Tao Gemini API key trong Google AI Studio.
2. Gan `APP_AI_API_KEY` bang key vua tao.
3. Giu nguyen `APP_AI_BASE_URL` va `APP_AI_MODEL` mac dinh, hoac doi sang model Gemini khac neu can.
4. Khoi dong backend nhu binh thuong.

Khong can OpenAI billing neu dung Gemini free tier.

## Bang du lieu moi

- `Survey_AI_Summary`
- `Survey_AI_Summary_Job`

## Hanh vi

1. Client goi `POST /generate`.
2. Backend kiem tra quyen nhu luong survey result.
3. Backend tai comment text cua dung survey duoc yeu cau.
4. Backend tinh `source_hash`.
5. Neu summary cu cung hash, tra lai ngay.
6. Neu chua co, tao job `QUEUED` va day vao worker nen.
7. Worker goi model, chunk du lieu, merge ket qua, luu summary.
8. Client co the poll `GET` de xem khi nao `status = COMPLETED`.
