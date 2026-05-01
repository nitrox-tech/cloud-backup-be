package com.nitro.tech.cloud.repository;

import com.nitro.tech.cloud.domain.StoredFile;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoredFileRepository extends JpaRepository<StoredFile, String> {

    List<StoredFile> findByFolderIdOrderByCreatedAtDesc(String folderId);

    long countByFolderId(String folderId);

    @Query(
            value =
                    """
            SELECT f.* FROM files f
            LEFT JOIN folders p ON f.folder_id = p.id
            LEFT JOIN folders r ON p.root_folder_id = r.id
            WHERE (
                f.user_id = :userId
                OR (f.folder_id IS NOT NULL AND (
                    p.user_id = :userId
                    OR EXISTS (SELECT 1 FROM folder_members m WHERE m.folder_id = p.root_folder_id AND m.user_id = :userId)
                ))
            )
            AND (:query IS NULL OR LOWER(f.file_name) LIKE LOWER(CONCAT('%', :query, '%')))
            AND (:createdAtStart IS NULL OR f.created_at >= :createdAtStart)
            AND (:source = 'all'
                 OR (:source = 'private' AND (f.folder_id IS NULL OR (r.id IS NOT NULL AND r.shareable = false)))
                 OR (:source = 'shared' AND f.folder_id IS NOT NULL AND r.id IS NOT NULL AND r.shareable = true)
            )
            AND (:fileType = 'all'
                 OR (:fileType = 'image' AND f.mime_type LIKE 'image/%')
                 OR (:fileType = 'video' AND f.mime_type LIKE 'video/%')
                 OR (:fileType = 'audio' AND f.mime_type LIKE 'audio/%')
                 OR (:fileType = 'document' AND (f.mime_type LIKE 'text/%' OR f.mime_type = 'application/pdf' OR f.mime_type LIKE 'application/vnd.ms-%' OR f.mime_type LIKE 'application/vnd.openxmlformats-officedocument.%'))
                 OR (:fileType = 'archive' AND f.mime_type IN ('application/zip', 'application/x-rar-compressed', 'application/x-7z-compressed', 'application/x-tar', 'application/x-gzip'))
            )
            ORDER BY f.created_at DESC
            """,
            nativeQuery = true)
    List<StoredFile> searchFiles(
            @Param("userId") String userId,
            @Param("query") String query,
            @Param("source") String source,
            @Param("fileType") String fileType,
            @Param("createdAtStart") Instant createdAtStart);

    @Modifying
    @Query("DELETE FROM StoredFile f WHERE f.folderId IN :folderIds")
    void deleteByFolderIdIn(@Param("folderIds") Collection<String> folderIds);
}
