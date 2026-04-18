package com.nitro.tech.cloud.repository;

import com.nitro.tech.cloud.domain.Folder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FolderRepository extends JpaRepository<Folder, String> {

    List<Folder> findByUserIdOrderByCreatedAtAsc(String userId);

    List<Folder> findByParentId(String parentId);

    /**
     * Oldest private (non-shareable) root for the user — canonical "private archive" root when several exist.
     */
    Optional<Folder> findFirstByUserIdAndParentIdIsNullAndShareableFalseOrderByCreatedAtAsc(String userId);

    List<Folder> findByParentIdAndRootFolderIdOrderByNameAsc(String parentId, String rootFolderId);

    /** Shareable roots owned by this user. */
    List<Folder> findByUserIdAndParentIdIsNullAndShareableTrueOrderByCreatedAtAsc(String userId);

    /**
     * Shareable roots where the user appears in {@code folder_members} (archive collaborator). Root id =
     * {@code FolderMember.folderId}.
     */
    @Query(
            """
            SELECT f FROM Folder f
            WHERE f.parentId IS NULL AND f.shareable = true
              AND f.id IN (SELECT m.folderId FROM FolderMember m WHERE m.userId = :userId)
            """)
    List<Folder> findShareableRootsByMembership(@Param("userId") String userId);

    long countByParentId(String parentId);

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
