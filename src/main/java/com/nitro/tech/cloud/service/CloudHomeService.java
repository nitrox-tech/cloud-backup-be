package com.nitro.tech.cloud.service;

import com.nitro.tech.cloud.domain.FileFavorite;
import com.nitro.tech.cloud.domain.FileRecent;
import com.nitro.tech.cloud.domain.StoredFile;
import com.nitro.tech.cloud.repository.FileFavoriteRepository;
import com.nitro.tech.cloud.repository.FileRecentRepository;
import com.nitro.tech.cloud.repository.StoredFileRepository;
import com.nitro.tech.cloud.web.dto.CloudEntryResponse;
import com.nitro.tech.cloud.web.dto.CloudHomeResponse;
import com.nitro.tech.cloud.web.dto.FavoriteFilesPageResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CloudHomeService {

    private static final int HOME_LIMIT = 15;

    private final FileFavoriteRepository fileFavoriteRepository;
    private final FileRecentRepository fileRecentRepository;
    private final StoredFileRepository storedFileRepository;
    private final FolderAccessService folderAccessService;
    private final FileMetadataService fileMetadataService;

    public CloudHomeService(
            FileFavoriteRepository fileFavoriteRepository,
            FileRecentRepository fileRecentRepository,
            StoredFileRepository storedFileRepository,
            FolderAccessService folderAccessService,
            FileMetadataService fileMetadataService) {
        this.fileFavoriteRepository = fileFavoriteRepository;
        this.fileRecentRepository = fileRecentRepository;
        this.storedFileRepository = storedFileRepository;
        this.folderAccessService = folderAccessService;
        this.fileMetadataService = fileMetadataService;
    }

    @Transactional(readOnly = true)
    public CloudHomeResponse buildHome(String userId) {
        List<CloudEntryResponse> favorites = new ArrayList<>();
        for (FileFavorite ff :
                fileFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, HOME_LIMIT)).getContent()) {
            mapIfAccessible(userId, ff.getFileId()).ifPresent(favorites::add);
        }
        List<CloudEntryResponse> recents = new ArrayList<>();
        for (FileRecent fr : fileRecentRepository.findByUserIdOrderByLastActionAtDesc(userId, PageRequest.of(0, HOME_LIMIT))) {
            mapIfAccessible(userId, fr.getFileId()).ifPresent(recents::add);
        }
        return new CloudHomeResponse(favorites, recents);
    }

    /**
     * @param pageOneBased trang theo UI (1 = trang đầu); chuyển thành 0-based cho {@link PageRequest}.
     */
    @Transactional(readOnly = true)
    public FavoriteFilesPageResponse pageFavorites(String userId, int pageOneBased, int size) {
        int springPage = pageOneBased - 1;
        Page<FileFavorite> p =
                fileFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(springPage, size));
        List<CloudEntryResponse> items = new ArrayList<>();
        for (FileFavorite ff : p.getContent()) {
            mapIfAccessible(userId, ff.getFileId()).ifPresent(items::add);
        }
        return new FavoriteFilesPageResponse(
                items, pageOneBased, p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    private Optional<CloudEntryResponse> mapIfAccessible(String userId, String fileId) {
        return storedFileRepository
                .findById(fileId)
                .filter(file -> folderAccessService.canAccessFile(userId, file))
                .map(file -> fileMetadataService.toCloudEntryForViewer(userId, file));
    }
}
