package com.nitro.tech.cloud.repository;

import com.nitro.tech.cloud.domain.FileFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileFavoriteRepository extends JpaRepository<FileFavorite, String> {

    boolean existsByUserIdAndFileId(String userId, String fileId);

    void deleteByUserIdAndFileId(String userId, String fileId);

    Page<FileFavorite> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
