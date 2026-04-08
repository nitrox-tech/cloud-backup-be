# 1cloud-be API Docs (AI Friendly)

This document is optimized for humans and AI agents to quickly understand and call the API.

## Base Info

- Base URL (local): `http://localhost:8080`
- Swagger UI: `GET /swagger-ui.html`
- OpenAPI JSON: `GET /v3/api-docs`
- Content type: `application/json`

## Security Model

Most endpoints require **both**:

1. API key header: `X-API-Key: <your-api-key>`
2. JWT header: `Authorization: Bearer <access_token>`

Public endpoints (no API key, no JWT):

- `GET /health`
- `POST /auth/telegram`

## Auth Flow (canonical)

1. Call `POST /auth/telegram` with Telegram user payload.
2. Read `access_token` in response.
3. For protected APIs, send:
   - `X-API-Key`
   - `Authorization: Bearer <access_token>`

---

## Endpoint Index

### Health

- `GET /health` - liveness check

### Auth

- `POST /auth/telegram` - login/upsert user, returns JWT and Telegram client rules
  - For newly created users, server auto-creates one private root folder named `Private Folder` (`shareable=false`).

### Telegram Client Config

- `GET /config/telegram` - full Telegram client rules snapshot
- `GET /config/telegram/archive-group` - archive-group subset

### Folders

- `POST /folders` - create folder
- `GET /folders` - list accessible folders
- `PUT /folders/{id}` - rename folder
- `PUT /folders/{id}/move` - move folder to another parent
- `DELETE /folders/{id}` - delete folder
- `PUT /folders/{id}/telegram-chat` - set `telegram_chat_id`
- `DELETE /folders/{id}/telegram-chat` - clear `telegram_chat_id`
- `GET /folders/{id}/invite-link` - build invite deep link (shareable archive owner; needs `telegram_chat_id` on root)
- `GET /folders/{id}/members` - list folder members
- `POST /folders/{id}/members` - add member
- `DELETE /folders/{id}/members/{memberUserId}` - remove member

### Files

- `POST /files/metadata` - create file metadata
- `GET /files` - list file metadata (optional `folder_id`)
- `GET /files/{id}` - get file metadata
- `PUT /files/{id}` - update file metadata name
- `PUT /files/{id}/move` - move file metadata to another folder
- `DELETE /files/{id}` - delete file metadata

---

## Core Request/Response Schemas

## `POST /auth/telegram` (public)

Request body:

```json
{
  "id": 123456789,
  "first_name": "Marco",
  "last_name": "Duong",
  "username": "marco",
  "photo_url": "https://...",
  "auth_date": 1710000000,
  "hash": "optional-widget-hash"
}
```

Response body (important fields):

```json
{
  "access_token": "<jwt>",
  "token_type": "Bearer",
  "user": {
    "id": "internal-user-uuid",
    "telegram_user_id": "123456789",
    "username": "marco"
  },
  "telegram_client": {
    "schema_version": 4
  }
}
```

Notes:

- Server currently trusts client-sent Telegram user id.
- `access_token` is the value to put into `Authorization: Bearer ...`.
- On first login (new user record), backend auto-creates a private root folder:
  - `name = "Private Folder"`
  - `parent_id = null`
  - `shareable = false`

## `POST /folders`

Request:

```json
{
  "name": "My Root",
  "parent_id": null,
  "shareable": true,
  "telegram_chat_id": "-1001234567890"
}
```

Response (example):

```json
{
  "id": "folder-uuid",
  "user_id": "internal-user-uuid",
  "name": "My Root",
  "parent_id": null,
  "root_folder_id": "folder-uuid",
  "shareable": true,
  "telegram_chat_id": "-1001234567890",
  "is_root": true,
  "created_at": "2026-04-06T12:00:00Z"
}
```

Notes:

- Each user can create only **one** private root folder (`parent_id = null` and `shareable = false`).
- Creating another private root folder for the same user returns `409` with an error message.
- If creating a root folder (`parent_id = null`), folder type is decided by request field `shareable`.
- If creating a subfolder (`parent_id != null`), `shareable` is inherited from parent folder (request `shareable` is ignored for this case).
- `telegram_chat_id` is only accepted for shareable root folders.

## `PUT /folders/{id}/move`

Request:

```json
{
  "parent_id": "target-parent-folder-uuid"
}
```

Rules:

- Cannot move root folder.
- Only folders in `shareable=true` trees can be moved.
- Target parent must also be in a `shareable=true` tree.
- Move is only allowed inside the same root tree.
- Cannot move a folder into itself or into one of its descendants.

Response: same schema as `FolderResponse`.

## `GET /folders/{id}/invite-link`

Protected (API key + JWT). **Owner only** for a **shareable** archive root; root must already have `telegram_chat_id`. Path `{id}` may be any folder in that tree — the response always uses the **backend root** `folder_id`.

Response (example):

```json
{
  "invite_url": "nitro-tech-cloud://invite?folder_id=folder-uuid&telegram_chat_id=-1001234567890",
  "folder_id": "folder-uuid",
  "telegram_chat_id": "-1001234567890"
}
```

## `PUT /files/{id}/move`

Request:

```json
{
  "folder_id": "target-folder-uuid"
}
```

Rules:

- Only files already located in a `shareable=true` folder can be moved.
- Target folder must be in a `shareable=true` tree.
- Move is only allowed inside the same root tree.

Response: same schema as `FileResponse`.

Override the URL prefix with env `INVITE_BASE_URL` (see `app.invite.base-url` in `application.yml`). If `base-url` is empty, `invite_url` is `null` and clients can use `folder_id` + `telegram_chat_id` from JSON.

## `POST /files/metadata`

Request:

```json
{
  "message_id": "12345",
  "telegram_file_id": "AgACAgUAAxkBAAIB...",
  "file_name": "report.pdf",
  "file_size": 345678,
  "mime_type": "application/pdf",
  "folder_id": "folder-uuid",
  "chunk_group_id": null,
  "chunk_index": null,
  "chunk_total": null
}
```

Response (example):

```json
{
  "id": "file-meta-uuid",
  "user_id": "internal-user-uuid",
  "message_id": "12345",
  "telegram_file_id": "AgACAgUAAxkBAAIB...",
  "file_name": "report.pdf",
  "file_size": "345678",
  "mime_type": "application/pdf",
  "folder_id": "folder-uuid",
  "chunk_group_id": null,
  "chunk_index": null,
  "chunk_total": null,
  "created_at": "2026-04-06T12:05:00Z"
}
```

## `POST /folders/{id}/members`

Request:

```json
{
  "user_id": "internal-user-uuid-to-add"
}
```

Response:

```json
{
  "id": "membership-uuid",
  "folder_id": "folder-uuid",
  "user_id": "internal-user-uuid-to-add",
  "created_at": "2026-04-06T12:10:00Z"
}
```

---

## Query Parameters

- `GET /files`
  - `folder_id` (optional, string UUID)

---

## Error Format

For many 4xx responses, body format:

```json
{
  "error": "human readable error message"
}
```

Common statuses:

- `400` invalid request / validation
- `401` missing or invalid API key (protected endpoints)
- `404` entity not found or inaccessible
- `409` conflict (for example duplicate folder name, duplicate member)

---

## cURL Quick Calls

## 1) Login

```bash
curl -X POST "http://localhost:8080/auth/telegram" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 123456789,
    "username": "marco"
  }'
```

## 2) Create folder (protected)

```bash
curl -X POST "http://localhost:8080/folders" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-api-key-for-local-dev" \
  -H "Authorization: Bearer <access_token>" \
  -d '{
    "name": "Docs",
    "parent_id": null,
    "shareable": false
  }'
```

## 3) Create file metadata (protected)

```bash
curl -X POST "http://localhost:8080/files/metadata" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-api-key-for-local-dev" \
  -H "Authorization: Bearer <access_token>" \
  -d '{
    "message_id": "12345",
    "telegram_file_id": "AgAC...",
    "file_name": "demo.txt",
    "file_size": 123,
    "mime_type": "text/plain"
  }'
```

---

## AI Usage Notes

- Treat IDs as opaque strings unless explicitly numeric (`TelegramLoginRequest.id` is numeric).
- `file_size` in `FileResponse` is string (by current DTO implementation), but request uses numeric.
- Telegram file bytes are not served by this backend; this service stores metadata and access rules.
- For business semantics of Telegram routing, also read:
  - `docs/TELEGRAM_CLIENT_RULES.md`
  - `docs/MOBILE_INTEGRATION_AND_FILE_UI.md`

