package com.nitro.tech.cloud.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
public class StoredFile {

    @Id
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    /** Telegram message id in the archive chat (opaque string). */
    @Column(name = "message_id", nullable = false, columnDefinition = "TEXT")
    private String messageId;

    @Column(name = "telegram_file_id", nullable = false, length = 2048)
    private String telegramFileId;

    @Column(name = "file_name", nullable = false, length = 1024)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 256)
    private String mimeType = "application/octet-stream";

    @Column(name = "folder_id", length = 36)
    private String folderId;

    @Column(name = "chunk_group_id", length = 36)
    private String chunkGroupId;

    @Column(name = "chunk_index")
    private Integer chunkIndex;

    @Column(name = "chunk_total")
    private Integer chunkTotal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
