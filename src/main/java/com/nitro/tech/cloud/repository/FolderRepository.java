package com.nitro.tech.cloud.repository;

import com.nitro.tech.cloud.domain.Folder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FolderRepository extends JpaRepository<Folder, String> {

    List<Folder> findByUserIdOrderByCreatedAtAsc(String userId);

    long countByUserIdAndParentId(String userId, String parentId);

    long countByUserIdAndParentIdIsNull(String userId);

    long countByUserIdAndParentIdIsNullAndShareableFalse(String userId);

    /** All folders belonging to archive trees where the user is an explicit member (shareable roots only). */
    @Query(
            """
            SELECT f FROM Folder f
            WHERE f.rootFolderId IN (
                SELECT m.folderId FROM FolderMember m WHERE m.userId = :userId
            )
            """)
    List<Folder> findAllInSharedTrees(@Param("userId") String userId);
}
