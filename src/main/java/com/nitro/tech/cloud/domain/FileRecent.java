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
@Table(name = "file_recents")
@Getter
@Setter
@NoArgsConstructor
public class FileRecent {

    @Id
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "file_id", nullable = false, length = 36)
    private String fileId;

    @Column(name = "last_action_at", nullable = false)
    private Instant lastActionAt;

    @PrePersist
    void prePersist() {
        if (lastActionAt == null) {
            lastActionAt = Instant.now();
        }
    }
}
