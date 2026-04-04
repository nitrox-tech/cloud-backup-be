package com.nitro.tech.cloud.service;

import com.nitro.tech.cloud.domain.Folder;
import com.nitro.tech.cloud.domain.StoredFile;
import com.nitro.tech.cloud.repository.FolderMemberRepository;
import com.nitro.tech.cloud.repository.FolderRepository;
import com.nitro.tech.cloud.repository.StoredFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FolderAccessService {

    private final FolderRepository folderRepository;
    private final FolderMemberRepository folderMemberRepository;
    private final StoredFileRepository storedFileRepository;

    public FolderAccessService(
            FolderRepository folderRepository,
            FolderMemberRepository folderMemberRepository,
            StoredFileRepository storedFileRepository) {
        this.folderRepository = folderRepository;
        this.folderMemberRepository = folderMemberRepository;
        this.storedFileRepository = storedFileRepository;
    }

    /** Archive owner (root creator) for the whole tree. */
    public boolean isTreeOwner(String userId, Folder folder) {
        return folder.getUserId().equals(userId);
    }

    @Transactional(readOnly = true)
    public boolean canAccessFolder(String userId, String folderId) {
        Folder f = folderRepository.findById(folderId).orElse(null);
        if (f == null) {
            return false;
        }
        if (f.getUserId().equals(userId)) {
            return true;
        }
        String rootId = f.getRootFolderId();
        if (rootId == null) {
            return false;
        }
        Folder root = folderRepository.findById(rootId).orElse(null);
        if (root == null || !root.isShareable()) {
            return false;
        }
        return folderMemberRepository.existsByFolderIdAndUserId(rootId, userId);
    }

    @Transactional(readOnly = true)
    public boolean canAccessFile(String userId, String fileId) {
        return storedFileRepository
                .findById(fileId)
                .map(file -> canAccessFile(userId, file))
                .orElse(false);
    }

    public boolean canAccessFile(String userId, StoredFile file) {
        if (file.getUserId().equals(userId)) {
            return true;
        }
        if (file.getFolderId() == null) {
            return false;
        }
        return canAccessFolder(userId, file.getFolderId());
    }
}
