package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/**
 * JSON node cho {@code GET /clouds/private}, {@code GET /clouds/public-workspace}, body thành công của
 * {@code POST|GET|PUT /files/…} (metadata file), folder shallow từ {@code POST|PUT /folders} (tạo / đổi tên), và sau
 * {@code PUT /clouds/entries/{id}/move} (file hoặc folder):
 * cùng shape ({@code is_folder}, {@code child_number}, …).
 * Chỉ root listing có {@code children} (một lớp); folder con / file không kèm {@code children}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Một nút trong cloud listing (folder hoặc file metadata)")
public record CloudEntryResponse(
        @JsonProperty("is_folder") @Schema(description = "true = thư mục, false = file metadata") boolean isFolder,
        @JsonProperty("child_number") @Schema(description = "Số con trực tiếp (subfolder + file); 0 với file")
                int childNumber,
        @Schema(description = "UUID folder hoặc file") String id,
        @Schema(description = "Tên folder hoặc tên file") String name,
        @JsonProperty("root_folder_id")
                @Schema(description = "UUID root của cây; với file = root của folder chứa file")
                String rootFolderId,
        @JsonProperty("telegram_chat_id")
                @Schema(
                        description =
                                "Supergroup/archive Telegram trên **root shareable** của cây; private archive thường null. "
                                        + "Với **file** có thể null (client dùng Saved Messages / self-chat).")
                String telegramChatId,
        @JsonProperty("parent_folder_id")
                @Schema(
                        description =
                                "Folder cha: với folder = `folders.parent_id` (null nếu root); với file = `files.folder_id` "
                                        + "(folder chứa file).")
                String parentFolderId,
        @JsonProperty("created_by")
                @Schema(description = "User gắn với row (`folders.user_id` / `files.user_id` — owner cây hoặc người tạo metadata file)")
                CloudUserResponse createdBy,
        @JsonProperty("created_at") @Schema(description = "UTC") Instant createdAt,
        @JsonProperty("mime_type") @Schema(description = "Chỉ có khi là file") String mimeType,
        @JsonProperty("file_size") @Schema(description = "Kích thước (string); chỉ file") String fileSize,
        @JsonProperty("message_id") @Schema(description = "Chỉ file") String messageId,
        @JsonProperty("telegram_file_id") @Schema(description = "Chỉ file") String telegramFileId,
        @JsonProperty("is_favorite")
                @Schema(description = "Chỉ file: user hiện tại đã đánh dấu favorite; null khi không trả về (listing cloud)")
                Boolean isFavorite,
        @Schema(description = "Chỉ folder: một lớp con") List<CloudEntryResponse> children) {

    public static CloudEntryResponse forFolder(
            String id,
            String name,
            String rootFolderId,
            String telegramChatId,
            String parentFolderId,
            CloudUserResponse createdBy,
            Instant createdAt,
            int childNumber,
            List<CloudEntryResponse> children) {
        return new CloudEntryResponse(
                true,
                childNumber,
                id,
                name,
                rootFolderId,
                telegramChatId,
                parentFolderId,
                createdBy,
                createdAt,
                null,
                null,
                null,
                null,
                null,
                children);
    }

    public static CloudEntryResponse forFile(
            String id,
            String name,
            String rootFolderId,
            String telegramChatId,
            String parentFolderId,
            CloudUserResponse createdBy,
            Instant createdAt,
            String mimeType,
            String fileSize,
            String messageId,
            String telegramFileId,
            Boolean isFavorite) {
        return new CloudEntryResponse(
                false,
                0,
                id,
                name,
                rootFolderId,
                telegramChatId,
                parentFolderId,
                createdBy,
                createdAt,
                mimeType,
                fileSize,
                messageId,
                telegramFileId,
                isFavorite,
                null);
    }

    /** Folder ở lớp liệt kê: có {@code child_number}, không có {@code children} trong JSON. */
    public static CloudEntryResponse forFolderShallow(
            String id,
            String name,
            String rootFolderId,
            String telegramChatId,
            String parentFolderId,
            CloudUserResponse createdBy,
            Instant createdAt,
            int childNumber) {
        return new CloudEntryResponse(
                true,
                childNumber,
                id,
                name,
                rootFolderId,
                telegramChatId,
                parentFolderId,
                createdBy,
                createdAt,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
