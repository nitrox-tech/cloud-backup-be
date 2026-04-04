package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.domain.StoredFile;
import java.time.Instant;

public record FileResponse(
        String id,
        @JsonProperty("user_id") String userId,
        @JsonProperty("message_id") String messageId,
        @JsonProperty("telegram_file_id") String telegramFileId,
        @JsonProperty("file_name") String fileName,
        @JsonProperty("file_size") String fileSize,
        @JsonProperty("mime_type") String mimeType,
        @JsonProperty("folder_id") String folderId,
        @JsonProperty("chunk_group_id") String chunkGroupId,
        @JsonProperty("chunk_index") Integer chunkIndex,
        @JsonProperty("chunk_total") Integer chunkTotal,
        @JsonProperty("created_at") Instant createdAt) {

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
