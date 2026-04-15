package com.nitro.tech.cloud.repository;

import com.nitro.tech.cloud.domain.FolderInviteCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderInviteCodeRepository extends JpaRepository<FolderInviteCode, String> {

    boolean existsByCode(String code);

    Optional<FolderInviteCode> findByCode(String code);
}
