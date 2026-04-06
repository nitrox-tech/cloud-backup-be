package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.domain.StoredFile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Metadata file đã lưu")
public record FileResponse(
        @Schema(description = "UUID metadata") String id,
        @JsonProperty("user_id") @Schema(description = "UUID user sở hữu") String userId,
        @JsonProperty("message_id") @Schema(description = "Id tin nhắn Telegram") String messageId,
        @JsonProperty("telegram_file_id") @Schema(description = "file_id Telegram") String telegramFileId,
        @JsonProperty("file_name") @Schema(description = "Tên file") String fileName,
        @JsonProperty("file_size") @Schema(description = "Kích thước (string)") String fileSize,
        @JsonProperty("mime_type") @Schema(description = "MIME") String mimeType,
        @JsonProperty("folder_id") @Schema(description = "UUID folder") String folderId,
        @JsonProperty("chunk_group_id") @Schema(description = "Nhóm chunk") String chunkGroupId,
        @JsonProperty("chunk_index") @Schema(description = "Chunk index") Integer chunkIndex,
        @JsonProperty("chunk_total") @Schema(description = "Tổng chunk") Integer chunkTotal,
        @JsonProperty("created_at") @Schema(description = "Thời điểm tạo (UTC)") Instant createdAt) {

    public static FileResponse from(StoredFile f) {
        return new FileResponse(
                f.getId(),
                f.getUserId(),
                f.getMessageId(),
                f.getTelegramFileId(),
                f.getFileName(),
                String.valueOf(f.getFileSize()),
                f.getMimeType(),
                f.getFolderId(),
                f.getChunkGroupId(),
                f.getChunkIndex(),
                f.getChunkTotal(),
                f.getCreatedAt());
    }
}
