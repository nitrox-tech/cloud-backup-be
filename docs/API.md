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

- `POST /auth/telegram` - login/upsert user, returns JWT and full client rules (replaces separate config endpoints)
    - For newly created users, server auto-creates one private root folder named `Private Folder` (`shareable=false`).

### Folders

- `POST /folders` - create folder (`telegram_chat_id` chỉ trên request khi tạo root shareable; không có API riêng set/clear chat sau tạo)
- `PUT /folders/{id}` - rename folder → `CloudEntryResponse` (folder shallow)
- `DELETE /folders/{id}` - delete folder (cascade subtree + file metadata trong cây)

### Workspace invites (archive shareable)

- `POST /workspace-invites/verify` - verify invite code trước khi join Telegram
- `POST /workspace-invites/finalize-join` - finalize join sau khi join Telegram
- `POST /workspace-invites/{workspaceId}/invites` - tạo invite (owner; `workspaceId` = root shareable hoặc folder trong cây)
- `GET /workspace-invites/{workspaceId}/members` - danh sách member (owner)
- `DELETE /workspace-invites/{workspaceId}/members/{memberUserId}` - gỡ member (owner). Thêm member chỉ qua invite + finalize-join.

### Files

- `POST /files/metadata` - create file metadata → `CloudEntryResponse` (file row, có `is_favorite` cho user hiện tại)
- `GET /files/{id}` - get file metadata → `CloudEntryResponse` (có `is_favorite`)
- `PUT /files/{id}` - update file metadata name → `CloudEntryResponse` (có `is_favorite`)
- `POST /files/{id}/favorite` - toggle favorite của user hiện tại cho file → `CloudEntryResponse` với `is_favorite` mới
- `DELETE /files/{id}` - delete file metadata

Listing theo folder / tree: dùng `GET /clouds/private` hoặc `GET /clouds/public-workspace` (một lớp `children`), không có `GET /files` list riêng.

### Clouds

- `GET /clouds/home` - tối đa 15 favorite + 15 recent (`CloudHomeResponse`)
- `GET /clouds/favorite` - favorite có phân trang; **`page` 1-based** (trang đầu `page=1`), `size` mặc định 20 (`FavoriteFilesPageResponse`)
- `GET /clouds/private` - private root + one layer of children (`PrivateCloudTreeResponse`)
- `GET /clouds/folders/{id}` - any accessible folder + one layer of children (same shape as `/clouds/private`)
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
  "rules": {
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

## `GET /clouds/folders/{id}`

Protected (API key + JWT). Folder bất kỳ user có quyền (`FolderAccessService`): response **`PrivateCloudTreeResponse`** giống `GET /clouds/private` — trường `root` là folder `{id}` kèm `children` một lớp (folder con shallow + file).

`404` nếu folder không tồn tại hoặc không có quyền.

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

## `POST /workspace-invites/{workspaceId}/invites`

Protected (API key + JWT). **Owner only** for a **shareable** workspace. Path `{workspaceId}` may be the shareable root UUID or any folder in that tree (server resolves to root).

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

## `POST /workspace-invites/verify`

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

## `POST /workspace-invites/finalize-join`

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

Response (example — `CloudEntryResponse` file row; thêm `is_favorite` cho user hiện tại):

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
  "telegram_file_id": "AgACAgUAAxkBAAIB...",
  "is_favorite": false
}
```

## `POST /files/{id}/favorite`

Protected (API key + JWT). Toggle favorite **theo user đang đăng nhập** (một dòng trong `file_favorites` / user + file). Cùng quyền xem file như `GET /files/{id}`.

Response: `CloudEntryResponse` file với `is_favorite` bật/tắt sau thao tác.

## `GET /workspace-invites/{workspaceId}/members`

Protected (API key + JWT). **Owner only.** Trả `{ "items": [ ... ] }` giống cấu trúc `ListResponse` trước đây; mỗi phần tử là metadata membership (`FolderMemberResponse`).

## `DELETE /workspace-invites/{workspaceId}/members/{memberUserId}`

Protected (API key + JWT). **Owner only.** `204` khi gỡ thành công.

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
- `is_favorite` on file rows: có trên `GET|POST|PUT /files/...` và sau `POST /files/{id}/favorite`; thường **không** có trên file trong `GET /clouds/...` listing (để tránh N+1).
- Telegram file bytes are not served by this backend; this service stores metadata and access rules.
- For business semantics of Telegram routing, also read:
    - `docs/TELEGRAM_CLIENT_RULES.md`
    - `docs/MOBILE_INTEGRATION_AND_FILE_UI.md`

