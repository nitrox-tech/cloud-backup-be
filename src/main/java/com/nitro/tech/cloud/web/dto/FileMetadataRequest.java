package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Tạo metadata sau khi file đã có trên Telegram")
public record FileMetadataRequest(
        @NotBlank @JsonProperty("message_id") @Schema(description = "Id tin nhắn Telegram trong chat archive") String messageId,
        @NotBlank @JsonProperty("telegram_file_id") @Schema(description = "file_id từ Telegram") String telegramFileId,
        @NotBlank @JsonProperty("file_name") @Schema(description = "Tên hiển thị") String fileName,
        @NotNull @Min(0) @JsonProperty("file_size") @Schema(description = "Kích thước byte") Long fileSize,
        @JsonProperty("mime_type") @Schema(description = "MIME, mặc định application/octet-stream") String mimeType,
        @JsonProperty("folder_id") @Schema(description = "UUID folder chứa file") String folderId,
        @JsonProperty("chunk_group_id") @Schema(description = "Nhóm chunk (cùng file chia nhỏ)") String chunkGroupId,
        @JsonProperty("chunk_index") @Schema(description = "Chỉ số chunk (0-based)") Integer chunkIndex,
        @JsonProperty("chunk_total") @Schema(description = "Tổng số chunk") Integer chunkTotal) {

    public FileMetadataRequest {
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "application/octet-stream";
        }
    }
}
