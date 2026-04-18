package com.nitro.tech.cloud.repository;

import com.nitro.tech.cloud.domain.FolderMember;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderMemberRepository extends JpaRepository<FolderMember, String> {

    boolean existsByFolderIdAndUserId(String folderId, String userId);

    Optional<FolderMember> findByFolderIdAndUserId(String folderId, String userId);

    List<FolderMember> findByFolderIdOrderByCreatedAtAsc(String folderId);

    void deleteByFolderIdAndUserId(String folderId, String userId);

    /** Remove every membership row whose archive root id is in the given set (e.g. before deleting those folders). */
    long deleteByFolderIdIn(Collection<String> folderIds);
}
