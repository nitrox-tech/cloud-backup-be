package com.nitro.tech.cloud.repository;

import com.nitro.tech.cloud.domain.FileRecent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRecentRepository extends JpaRepository<FileRecent, String> {

    Optional<FileRecent> findByUserIdAndFileId(String userId, String fileId);

    long countByUserId(String userId);

    List<FileRecent> findByUserIdOrderByLastActionAtDesc(String userId, Pageable pageable);

    /** Oldest rows first — for trimming when count exceeds cap. */
    List<FileRecent> findByUserIdOrderByLastActionAtAsc(String userId, Pageable pageable);
}
