# 1cloud-be API Docs (AI Friendly)

This document is optimized for humans and AI agents to quickly understand and call the API.

## Base Info

- Base URL (local): `http://localhost:8080`
- Swagger UI: `GET /swagger-ui/index.html` (hoặc `/swagger-ui.html` tùy phiên bản springdoc)
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

- `GET /config/telegram` - full Telegram client rules snapshot (API key + JWT)
- `GET /config/telegram/archive-group` - archive-group subset (API key + JWT)

### Folders

- `POST /folders` - create folder (`telegram_chat_id` chỉ trên request khi tạo root shareable; không có API riêng set/clear chat sau tạo)
- `PUT /folders/{id}` - rename folder → `CloudEntryResponse` (folder shallow)
- `DELETE /folders/{id}` - delete folder (cascade subtree + file metadata trong cây)
- `POST /folders/{id}/invites` - tạo invite (owner archive shareable)
- `POST /folder-invites/verify` - verify invite code trước khi join Telegram
- `POST /folder-invites/finalize-join` - finalize join backend sau khi join Telegram
- `GET /folders/{id}/members` - list folder members
- `POST /folders/{id}/members` - add member
- `DELETE /folders/{id}/members/{memberUserId}` - remove member

### Files

- `POST /files/metadata` - create file metadata → `CloudEntryResponse` (file row)
- `GET /files/{id}` - get file metadata → `CloudEntryResponse`
- `PUT /files/{id}` - update file metadata name → `CloudEntryResponse`
- `DELETE /files/{id}` - delete file metadata

Listing theo folder / tree: dùng `GET /clouds/private` hoặc `GET /clouds/public-workspace` (một lớp `children`), không có `GET /files` list riêng.

### Clouds

- `GET /clouds/private` - private root + one layer of children
- `GET /clouds/public-workspace` - shareable roots + one layer each
- `PUT /clouds/entries/{id}/move` - move **file** or **folder** into `target_folder_id` (same `root_folder_id`; body có `is_folder`)

Chi tiết từng method/path không đều có mục `##` riêng bên dưới; bổ sung đầy đủ luôn có trong **OpenAPI** (`GET /v3/api-docs`) và Swagger UI.

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

Response (example — cùng shape `CloudEntryResponse` folder shallow như trong `GET /clouds/private`):

```json
{
  "is_folder": true,
  "child_number": 0,
  "id": "folder-uuid",
  "name": "My Root",
  "root_folder_id": "folder-uuid",
  "created_at": "2026-04-06T12:00:00Z"
}
```

Notes:

- Each user can create only **one** private root folder (`parent_id = null` and `shareable = false`).
- Creating another private root folder for the same user returns `409` with an error message.
- If creating a root folder (`parent_id = null`), folder type is decided by request field `shareable`.
- If creating a subfolder (`parent_id != null`), `shareable` is inherited from parent folder (request `shareable` is ignored for this case).
- `telegram_chat_id` is only accepted for shareable root folders.

## `PUT /clouds/entries/{id}/move`

Protected (API key + JWT). **Một endpoint** thay cho `PUT /folders/{id}/move` và `PUT /files/{id}/move` cũ.

Path `{id}`: UUID **file metadata** nếu `is_folder: false`, hoặc UUID **folder** nếu `is_folder: true`.

Request:

```json
{
  "is_folder": false,
  "target_folder_id": "target-folder-uuid"
}
```

Rules:

- **Folder** (`is_folder: true`): không move root; target phải cùng `root_folder_id`; không move vào chính folder hoặc descendant của nó; user phải có quyền trên cả source và target. Trùng tên trong target → `409`.
- **File** (`is_folder: false`): file phải đang có `folder_id` (trong cây shareable theo rule service); target cùng cây `root_folder_id`; user có quyền trên file và trên folder đích.

Response: `CloudEntryResponse` — folder shallow (`is_folder: true`) hoặc file row (`is_folder: false`), cùng shape với `GET /clouds/private` / listing.

## `POST /folders/{id}/invites`

Protected (API key + JWT). **Owner only** for a **shareable** archive root. Path `{id}` may be any folder in that tree.

Request (example):

```json
{
  "telegram_join_link": "https://t.me/+yourInviteLink",
  "expires_at": "2026-04-20T12:00:00Z"
}
```

Response (example):

```json
{
  "invite_id": "invite-uuid",
  "invite_code": "9K7M2QX4TP",
  "folder_id": "folder-root-uuid",
  "folder_name": "Team Shared",
  "telegram_join_link": "https://t.me/+yourInviteLink",
  "invite_url": "nitro-tech-cloud://invite?folder_id=...&telegram_chat_id=...&telegram_join_link=...",
  "expires_at": "2026-04-20T12:00:00Z",
  "created_at": "2026-04-16T10:00:00Z"
}
```

## `POST /folder-invites/verify`

Protected (API key + JWT). Kiểm tra invite trước khi app thực hiện join Telegram.

Request:

```json
{
  "invite_code": "9K7M2QX4TP"
}
```

Response (valid):

```json
{
  "valid": true,
  "invite_id": "invite-uuid",
  "invite_code": "9K7M2QX4TP",
  "invite_url": "nitro-tech-cloud://invite?folder_id=...&telegram_chat_id=...&telegram_join_link=...",
  "telegram_join_link": "https://t.me/+yourInviteLink",
  "expires_at": "2026-04-20T12:00:00Z",
  "folder_id": "folder-root-uuid",
  "folder_name": "Team Shared",
  "telegram_chat_id": "-1001234567890"
}
```

Response (invalid/expired):

```json
{
  "valid": false,
  "reason": "Invite code expired"
}
```

## `POST /folder-invites/finalize-join`

Protected (API key + JWT). Finalize join backend sau khi app join Telegram thành công.

Request:

```json
{
  "invite_code": "9K7M2QX4TP",
  "invite_id": "invite-uuid",
  "telegram_join_proof": {
    "chat_id": "-1001234567890"
  }
}
```

Response:

```json
{
  "joined": true,
  "folder": {
    "id": "folder-root-uuid",
    "name": "Team Shared"
  },
  "membership": {
    "role": "member",
    "joined_at": "2026-04-16T10:05:00Z"
  }
}
```

## `POST /files/metadata`

Request:

```json
{
  "message_id": "12345",
  "telegram_file_id": "AgACAgUAAxkBAAIB...",
  "file_name": "report.pdf",
  "file_size": 345678,
  "mime_type": "application/pdf",
  "folder_id": "folder-uuid"
}
```

Response (example — cùng shape `CloudEntryResponse` với file trong `GET /clouds/private`):

```json
{
  "is_folder": false,
  "child_number": 0,
  "id": "file-meta-uuid",
  "name": "report.pdf",
  "root_folder_id": "root-folder-uuid",
  "created_at": "2026-04-06T12:05:00Z",
  "mime_type": "application/pdf",
  "file_size": "345678",
  "message_id": "12345",
  "telegram_file_id": "AgACAgUAAxkBAAIB..."
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
- `file_size` in `CloudEntryResponse` (file row) is string, but `POST /files/metadata` request uses numeric `file_size`.
- Telegram file bytes are not served by this backend; this service stores metadata and access rules.
- For business semantics of Telegram routing, also read:
  - `docs/TELEGRAM_CLIENT_RULES.md`
  - `docs/MOBILE_INTEGRATION_AND_FILE_UI.md`

