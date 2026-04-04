package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FileMetadataRequest(
        @NotBlank @JsonProperty("message_id") String messageId,
        @NotBlank @JsonProperty("telegram_file_id") String telegramFileId,
        @NotBlank @JsonProperty("file_name") String fileName,
        @NotNull @Min(0) @JsonProperty("file_size") Long fileSize,
        @JsonProperty("mime_type") String mimeType,
        @JsonProperty("folder_id") String folderId,
        @JsonProperty("chunk_group_id") String chunkGroupId,
        @JsonProperty("chunk_index") Integer chunkIndex,
        @JsonProperty("chunk_total") Integer chunkTotal) {

    public FileMetadataRequest {
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "application/octet-stream";
        }
    }
}
