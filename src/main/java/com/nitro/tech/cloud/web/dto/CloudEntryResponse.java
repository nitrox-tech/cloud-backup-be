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
        @JsonProperty("created_at") @Schema(description = "UTC") Instant createdAt,
        @JsonProperty("mime_type") @Schema(description = "Chỉ có khi là file") String mimeType,
        @JsonProperty("file_size") @Schema(description = "Kích thước (string); chỉ file") String fileSize,
        @JsonProperty("message_id") @Schema(description = "Chỉ file") String messageId,
        @JsonProperty("telegram_file_id") @Schema(description = "Chỉ file") String telegramFileId,
        @Schema(description = "Chỉ folder: một lớp con") List<CloudEntryResponse> children) {

    public static CloudEntryResponse forFolder(
            String id,
            String name,
            String rootFolderId,
            Instant createdAt,
            int childNumber,
            List<CloudEntryResponse> children) {
        return new CloudEntryResponse(
                true, childNumber, id, name, rootFolderId, createdAt, null, null, null, null, children);
    }

    public static CloudEntryResponse forFile(
            String id,
            String name,
            String rootFolderId,
            Instant createdAt,
            String fileSize,
            String mimeType,
            String messageId,
            String telegramFileId) {
        return new CloudEntryResponse(
                false,
                0,
                id,
                name,
                rootFolderId,
                createdAt,
                mimeType,
                fileSize,
                messageId,
                telegramFileId,
                null);
    }

    /** Folder ở lớp liệt kê: có {@code child_number}, không có {@code children} trong JSON. */
    public static CloudEntryResponse forFolderShallow(
            String id, String name, String rootFolderId, Instant createdAt, int childNumber) {
        return new CloudEntryResponse(
                true, childNumber, id, name, rootFolderId, createdAt, null, null, null, null, null);
    }
}
