package com.nitro.tech.cloud.service;

import com.nitro.tech.cloud.domain.StoredFile;
import com.nitro.tech.cloud.repository.FolderRepository;
import com.nitro.tech.cloud.repository.StoredFileRepository;
import com.nitro.tech.cloud.web.dto.FileMetadataRequest;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileMetadataService {

    private final StoredFileRepository storedFileRepository;
    private final FolderRepository folderRepository;
    private final FolderAccessService folderAccessService;

    public FileMetadataService(
            StoredFileRepository storedFileRepository,
            FolderRepository folderRepository,
            FolderAccessService folderAccessService) {
        this.storedFileRepository = storedFileRepository;
        this.folderRepository = folderRepository;
        this.folderAccessService = folderAccessService;
    }

    @Transactional
    public StoredFile save(String userId, FileMetadataRequest req) {
        validateChunks(req);
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
        f.setChunkGroupId(req.chunkGroupId());
        f.setChunkIndex(req.chunkIndex());
        f.setChunkTotal(req.chunkTotal());
        return storedFileRepository.save(f);
    }

    @Transactional(readOnly = true)
    public StoredFile getIfAccessible(String userId, String fileId) {
        StoredFile file = storedFileRepository.findById(fileId).orElseThrow(() -> new NotFoundException("Not found"));
        if (!folderAccessService.canAccessFile(userId, file)) {
            throw new NotFoundException("Not found");
        }
        return file;
    }

    @Transactional(readOnly = true)
    public List<StoredFile> list(String userId, String folderId) {
        if (folderId == null) {
            return storedFileRepository.findByUserIdAndFolderIdIsNullOrderByCreatedAtDesc(userId);
        }
        if (!folderAccessService.canAccessFolder(userId, folderId)) {
            throw new IllegalArgumentException("Folder not found");
        }
        return storedFileRepository.findByFolderIdOrderByCreatedAtDesc(folderId);
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

    private static void validateChunks(FileMetadataRequest req) {
        boolean any = req.chunkGroupId() != null || req.chunkIndex() != null || req.chunkTotal() != null;
        if (!any) {
            return;
        }
        if (req.chunkGroupId() == null || req.chunkIndex() == null || req.chunkTotal() == null) {
            throw new IllegalArgumentException("chunk_group_id, chunk_index, and chunk_total must be set together");
        }
        if (req.chunkIndex() < 0 || req.chunkTotal() <= 0 || req.chunkIndex() >= req.chunkTotal()) {
            throw new IllegalArgumentException("Invalid chunk range");
        }
    }

    private static boolean sameRoot(String leftRootId, String leftId, String rightRootId, String rightId) {
        String leftRoot = leftRootId != null ? leftRootId : leftId;
        String rightRoot = rightRootId != null ? rightRootId : rightId;
        return leftRoot.equals(rightRoot);
    }
}
