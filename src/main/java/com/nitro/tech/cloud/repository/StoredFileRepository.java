package com.nitro.tech.cloud.repository;

import com.nitro.tech.cloud.domain.StoredFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, String> {

    Optional<StoredFile> findByIdAndUserId(String id, String userId);

    List<StoredFile> findByUserIdAndFolderIdIsNullOrderByCreatedAtDesc(String userId);

    List<StoredFile> findByUserIdAndFolderIdOrderByCreatedAtDesc(String userId, String folderId);

    List<StoredFile> findByFolderIdOrderByCreatedAtDesc(String folderId);

    long countByUserIdAndFolderId(String userId, String folderId);

    long countByFolderId(String folderId);
}
