# Mobile integration & file list UI (client-side)

Tài liệu này **bổ sung** [TELEGRAM_CLIENT_RULES.md](./TELEGRAM_CLIENT_RULES.md): cách app mobile tích hợp backend và **tự quyết định UI** (preview, icon) **không cần thêm field** trên server như `is_image` hay `thumb_aspect`.

---

## 1. Phạm vi trách nhiệm

| Thành phần | Việc làm |
|------------|----------|
| **Backend** | Auth, API key, metadata file/folder/member, rules snapshot (embedded in login). **Không** gọi Telegram để upload/download/preview file (tránh rủi ro block / sai mô hình sử dụng API). |
| **Mobile — lớp nghiệp vụ** | Gọi REST, hiển thị danh sách theo metadata; **không** nhúng chi tiết MTProto vào từng màn hình. |
| **Mobile — Telegram executor** | Một module: đọc rules + metadata (`message_id`, `telegram_file_id`, `folder`/`telegram_chat_id` gốc share…) và thực hiện gửi/nhận file qua **session user Telegram** (TDLib / tương đương). |

Luồng file **không đi qua server**; server chỉ lưu **tên và thông tin mô tả**, không lưu blob.

---

## 2. Không bắt buộc field `is_image` / `thumb_aspect` trên server

- Server **đã có** `file_name`, `mime_type`, `file_size` trong response file metadata.
- Việc “có phải ảnh không”, “có hiện preview không”, tỷ lệ khung list… có thể **hardcode / suy luận hoàn toàn trên client**:
  - Ưu tiên **`mime_type`** từ API khi đáng tin (client gửi đúng lúc `POST /files/metadata`).
  - Bổ sung **phần mở rộng tên file** (`file_name`) cho trường hợp `mime_type` generic (`application/octet-stream`) hoặc thiếu: `.jpg`, `.png`, `.webp`, `.heic`, …
- **Không cần** thêm cột hay JSON `is_image`, `thumb_aspect` trên backend cho mục đích này; tránh đồng bộ hai nguồn sự thật.

---

## 3. Danh sách file: preview ảnh vs không preview

- **Không coi là ảnh** (theo `mime_type` + fallback extension): **chỉ icon theo loại** (tài liệu, zip, …), **không** tải preview trong list.
- **Coi là ảnh**: hiển thị ô preview — executor Telegram lấy **thumbnail / kích thước nhỏ** từ message, **không** bắt buộc tải full file.
- **Cache**: nên cache **chỉ ảnh / thumbnail** trên máy (LRU, giới hạn dung lượng); file khác chỉ cache khi user mở/tải (policy tách riêng).

---

## 4. Gợi ý thứ tự suy luận trên client (gọn)

1. Nếu `mime_type` bắt đầu bằng `image/` → coi là ảnh (preview trong list nếu product yêu cầu).
2. Else, parse extension từ `file_name` (chuẩn hóa lowercase, lấy segment sau dấu chấm cuối) đối chiếu tập đuôi ảnh cố định trong app.
3. Else → không preview list; icon theo extension hoặc `mime_type`.

Tập đuôi / map icon nên là **hằng số trong app**, có thể mở rộng bản vá mà không cần deploy server.

---

## 5. Liên kết tài liệu

- Rules Telegram, API HTTP, identifier string: [TELEGRAM_CLIENT_RULES.md](./TELEGRAM_CLIENT_RULES.md).
- `telegram_chat_id` trên folder gốc share + cặp `(chat, message_id)` cho download: cùng file trên.

---

## 6. Tóm tắt

- **Server**: giữ metadata gồm **`file_name`** (và `mime_type`); không thêm flag UI-only trừ khi sau này có lý do sản phẩm khác.
- **Client**: dựa **`file_name` + `mime_type`** để quyết định preview/icon; **tải & cache ảnh** cục bộ qua Telegram executor, không qua backend.
