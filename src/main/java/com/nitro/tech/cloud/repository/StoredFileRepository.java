package com.nitro.tech.cloud.repository;

import com.nitro.tech.cloud.domain.StoredFile;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoredFileRepository extends JpaRepository<StoredFile, String> {

    List<StoredFile> findByFolderIdOrderByCreatedAtDesc(String folderId);

    long countByFolderId(String folderId);

    @Modifying
    @Query("DELETE FROM StoredFile f WHERE f.folderId IN :folderIds")
    void deleteByFolderIdIn(@Param("folderIds") Collection<String> folderIds);
}
