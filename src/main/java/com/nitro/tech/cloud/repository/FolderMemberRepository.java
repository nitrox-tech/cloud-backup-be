package com.nitro.tech.cloud.repository;

import com.nitro.tech.cloud.domain.FolderMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderMemberRepository extends JpaRepository<FolderMember, String> {

    boolean existsByFolderIdAndUserId(String folderId, String userId);

    List<FolderMember> findByFolderIdOrderByCreatedAtAsc(String folderId);

    void deleteByFolderIdAndUserId(String folderId, String userId);
}
