package com.nitro.tech.cloud.service;

import com.nitro.tech.cloud.domain.FileFavorite;
import com.nitro.tech.cloud.domain.FileRecent;
import com.nitro.tech.cloud.domain.Folder;
import com.nitro.tech.cloud.domain.StoredFile;
import com.nitro.tech.cloud.repository.FileFavoriteRepository;
import com.nitro.tech.cloud.repository.FileRecentRepository;
import com.nitro.tech.cloud.repository.FolderRepository;
import com.nitro.tech.cloud.repository.StoredFileRepository;
import com.nitro.tech.cloud.repository.UserRepository;
import com.nitro.tech.cloud.web.dto.CloudEntryResponse;
import com.nitro.tech.cloud.web.dto.CloudStorageStatsResponse;
import com.nitro.tech.cloud.web.dto.CloudUserResponse;
import com.nitro.tech.cloud.web.dto.FileMetadataRequest;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileMetadataService {

    private static final int RECENT_FILES_CAP = 15;

    private final StoredFileRepository storedFileRepository;
    private final FolderRepository folderRepository;
    private final FileFavoriteRepository fileFavoriteRepository;
    private final FileRecentRepository fileRecentRepository;
    private final FolderAccessService folderAccessService;
    private final UserRepository userRepository;

    public FileMetadataService(
            StoredFileRepository storedFileRepository,
            FolderRepository folderRepository,
            FileFavoriteRepository fileFavoriteRepository,
            FileRecentRepository fileRecentRepository,
            FolderAccessService folderAccessService,
            UserRepository userRepository) {
        this.storedFileRepository = storedFileRepository;
        this.folderRepository = folderRepository;
        this.fileFavoriteRepository = fileFavoriteRepository;
        this.fileRecentRepository = fileRecentRepository;
        this.folderAccessService = folderAccessService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CloudStorageStatsResponse getStorageStats(String userId) {
        var projection = storedFileRepository.getStorageStats(userId);
        long other = projection.getTotal()
                - (projection.getPhoto() + projection.getVideo() + projection.getAudio() + projection.getDocument());
        return new CloudStorageStatsResponse(
                projection.getTotal(),
                projection.getPhoto(),
                projection.getVideo(),
                projection.getAudio(),
                projection.getDocument(),
                Math.max(0, other));
    }

    @Transactional(readOnly = true)
    public List<CloudEntryResponse> searchFiles(
            String userId, String query, String source, String fileType, Integer days) {
        Instant createdAtStart = null;
        if (days != null && days > 0) {
            createdAtStart = Instant.now().minus(java.time.Duration.ofDays(days));
        }

        String safeSource = (source == null || source.isBlank()) ? "all" : source.toLowerCase();
        String safeFileType = (fileType == null || fileType.isBlank()) ? "all" : fileType.toLowerCase();
        String safeQuery = (query == null || query.isBlank()) ? null : query.trim();

        return storedFileRepository
                .searchFiles(userId, safeQuery, safeSource, safeFileType, createdAtStart)
                .stream()
                .map(f -> toCloudEntryForViewer(userId, f))
                .toList();
    }

    @Transactional
    public StoredFile save(String userId, FileMetadataRequest req) {
        if (req.folderId() != null && !folderAccessService.canAccessFolder(userId, req.folderId())) {
            throw new IllegalArgumentException("Folder not found");
        }
        StoredFile f = new StoredFile();
        f.setUserId(userId);
        f.setMessageId(req.messageId());
        f.setTelegramFileId(req.telegramFileId());
        f.setFileName(req.fileName());
        f.setFileSize(req.fileSize());
        f.setMimeType(req.mimeType());
        f.setFolderId(req.folderId());
        StoredFile saved = storedFileRepository.save(f);
        touchRecent(userId, saved.getId());
        return saved;
    }

    /** Upsert recent row for this user+file and keep at most {@link #RECENT_FILES_CAP} rows per user. */
    private void touchRecent(String userId, String fileId) {
        Instant now = Instant.now();
        FileRecent row = fileRecentRepository
                .findByUserIdAndFileId(userId, fileId)
                .orElseGet(() -> {
                    FileRecent r = new FileRecent();
                    r.setUserId(userId);
                    r.setFileId(fileId);
                    return r;
                });
        row.setLastActionAt(now);
        fileRecentRepository.save(row);
        long cnt = fileRecentRepository.countByUserId(userId);
        if (cnt > RECENT_FILES_CAP) {
            int excess = (int) (cnt - RECENT_FILES_CAP);
            var oldest = fileRecentRepository.findByUserIdOrderByLastActionAtAsc(userId, PageRequest.of(0, excess));
            fileRecentRepository.deleteAll(oldest);
        }
    }

    @Transactional(readOnly = true)
    public StoredFile getIfAccessible(String userId, String fileId) {
        StoredFile file = storedFileRepository.findById(fileId).orElseThrow(() -> new NotFoundException("Not found"));
        if (!folderAccessService.canAccessFile(userId, file)) {
            throw new NotFoundException("Not found");
        }
        return file;
    }

    @Transactional
    public StoredFile updateIfAccessible(String userId, String fileId, String newFileName) {
        StoredFile file = storedFileRepository.findById(fileId).orElseThrow(() -> new NotFoundException("Not found"));
        if (!folderAccessService.canAccessFile(userId, file)) {
            throw new NotFoundException("Not found");
        }
        file.setFileName(newFileName.trim());
        return storedFileRepository.save(file);
    }

    @Transactional
    public void deleteIfAccessible(String userId, String fileId) {
        StoredFile file = storedFileRepository.findById(fileId).orElseThrow(() -> new NotFoundException("Not found"));
        if (!folderAccessService.canAccessFile(userId, file)) {
            throw new NotFoundException("Not found");
        }
        storedFileRepository.delete(file);
    }

    @Transactional
    public StoredFile moveIfAccessible(String userId, String fileId, String targetFolderId) {
        StoredFile file = storedFileRepository.findById(fileId).orElseThrow(() -> new NotFoundException("Not found"));
        if (!folderAccessService.canAccessFile(userId, file)) {
            throw new NotFoundException("Not found");
        }
        if (file.getFolderId() == null) {
            throw new IllegalArgumentException("Only files inside shareable folders can be moved");
        }
        var sourceFolder =
                folderRepository.findById(file.getFolderId()).orElseThrow(() -> new IllegalArgumentException("Source folder not found"));
        var targetFolder =
                folderRepository.findById(targetFolderId).orElseThrow(() -> new IllegalArgumentException("Target folder not found"));
        if (!Objects.equals(sourceFolder.getRootFolderId(), targetFolder.getRootFolderId())) {
            throw new IllegalArgumentException("Only directories inside a root folder can be moved.");
        }
        if (!folderAccessService.canAccessFolder(userId, targetFolderId)) {
            throw new IllegalArgumentException("Target folder not found");
        }
        if (!sameRoot(sourceFolder.getRootFolderId(), sourceFolder.getId(), targetFolder.getRootFolderId(), targetFolder.getId())) {
            throw new IllegalArgumentException("Move is only allowed inside the same tree");
        }
        file.setFolderId(targetFolderId);
        return storedFileRepository.save(file);
    }

    /**
     * Toggle favorite for the current user. Idempotent flip; requires same access as reading file metadata.
     *
     * @return updated {@code is_favorite} for the viewer
     */
    @Transactional
    public CloudEntryResponse toggleFavorite(String userId, String fileId) {
        StoredFile file = getIfAccessible(userId, fileId);
        boolean nowFavorite;
        if (fileFavoriteRepository.existsByUserIdAndFileId(userId, fileId)) {
            fileFavoriteRepository.deleteByUserIdAndFileId(userId, fileId);
            nowFavorite = false;
        } else {
            FileFavorite row = new FileFavorite();
            row.setUserId(userId);
            row.setFileId(fileId);
            fileFavoriteRepository.save(row);
            nowFavorite = true;
        }
        return buildFileCloudEntry(file, nowFavorite);
    }

    /** Same JSON shape as file rows in cloud listings; {@code is_favorite} omitted. */
    @Transactional(readOnly = true)
    public CloudEntryResponse toCloudEntry(StoredFile file) {
        return buildFileCloudEntry(file, null);
    }

    /** File row with {@code is_favorite} true/false for the viewer. */
    @Transactional(readOnly = true)
    public CloudEntryResponse toCloudEntryForViewer(String viewerId, StoredFile file) {
        boolean fav = fileFavoriteRepository.existsByUserIdAndFileId(viewerId, file.getId());
        return buildFileCloudEntry(file, fav);
    }

    private CloudEntryResponse buildFileCloudEntry(StoredFile file, Boolean isFavoriteOrOmit) {
        String rootFolderId = resolveRootFolderIdForFile(file);
        String telegramChatId = resolveTelegramChatIdForFile(file);
        CloudUserResponse createdBy =
                file.getUserId() == null
                        ? null
                        : userRepository.findById(file.getUserId()).map(CloudUserResponse::fromEntity).orElse(null);
        return CloudEntryResponse.forFile(
                file.getId(),
                file.getFileName(),
                rootFolderId,
                telegramChatId,
                file.getFolderId(),
                createdBy,
                file.getCreatedAt(),
                file.getMimeType(),
                String.valueOf(file.getFileSize()),
                file.getMessageId(),
                file.getTelegramFileId(),
                isFavoriteOrOmit);
    }

    private String resolveRootFolderIdForFile(StoredFile file) {
        if (file.getFolderId() == null) {
            return null;
        }
        return folderRepository.findById(file.getFolderId()).map(Folder::effectiveRootFolderId).orElse(null);
    }

    /** Telegram archive chat trên root shareable của cây chứa file; private / không có root → null. */
    private String resolveTelegramChatIdForFile(StoredFile file) {
        String rootId = resolveRootFolderIdForFile(file);
        if (rootId == null) {
            return null;
        }
        return folderRepository.findById(rootId).map(Folder::getTelegramChatId).orElse(null);
    }

    private static boolean sameRoot(String leftRootId, String leftId, String rightRootId, String rightId) {
        String leftRoot = leftRootId != null ? leftRootId : leftId;
        String rightRoot = rightRootId != null ? rightRootId : rightId;
        return leftRoot.equals(rightRoot);
    }
}
