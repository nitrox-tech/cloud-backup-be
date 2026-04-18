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
@Table(name = "folders")
@Getter
@Setter
@NoArgsConstructor
public class Folder {

    @Id
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    /** Owner of the archive tree (root creator); every node in the same tree shares this value. */
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "parent_id", length = 36)
    private String parentId;

    /**
     * Root of this tree (same as {@link #id} for root folders). Used for fast shared-access checks.
     */
    @Column(name = "root_folder_id", length = 36)
    private String rootFolderId;

    /**
     * Only meaningful on <strong>root</strong> ({@link #parentId} null): {@code false} = private archive
     * (only owner sees it); {@code true} = shared root — members added on the root see the whole tree and
     * may CRUD folder structure and file metadata. Telegram storage is client-side; this app stores metadata only.
     */
    @Column(name = "shareable", nullable = false)
    private boolean shareable = false;

    /**
     * Telegram supergroup / shared chat id (opaque string, e.g. {@code -100…}) for a {@link #shareable} <strong>root</strong>
     * only — collaborators need the same peer to pair with each file's {@code message_id}. For <strong>private</strong>
     * roots ({@code shareable == false}), leave null: the client resolves Saved Messages / self-chat from the user's
     * Telegram session and fetches by {@code message_id} without this field. Persisted as {@code VARCHAR(200)} (not
     * unbounded TEXT) for tighter rows and index-friendly comparisons.
     */
    @Column(name = "telegram_chat_id", length = 200)
    private String telegramChatId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Root folder: no parent. No separate {@code is_root} column needed. */
    public boolean isRoot() {
        return parentId == null;
    }

    /** Tree root id for listings and access; on persisted roots equals {@link #id}. */
    public String effectiveRootFolderId() {
        return rootFolderId != null ? rootFolderId : id;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
