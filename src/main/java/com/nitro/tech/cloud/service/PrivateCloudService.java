package com.nitro.tech.cloud.service;

import com.nitro.tech.cloud.domain.Folder;
import com.nitro.tech.cloud.domain.StoredFile;
import com.nitro.tech.cloud.privatecloud.CloudComponent;
import com.nitro.tech.cloud.privatecloud.PrivateCloudFileLeaf;
import com.nitro.tech.cloud.privatecloud.PrivateCloudFolderComposite;
import com.nitro.tech.cloud.privatecloud.PrivateCloudFolderShallowLeaf;
import com.nitro.tech.cloud.repository.FolderRepository;
import com.nitro.tech.cloud.repository.StoredFileRepository;
import com.nitro.tech.cloud.web.dto.CloudEntryResponse;
import com.nitro.tech.cloud.web.dto.PrivateCloudTreeResponse;
import com.nitro.tech.cloud.web.dto.PublicWorkspaceResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrivateCloudService {

    private final FolderRepository folderRepository;
    private final StoredFileRepository storedFileRepository;
    private final FolderAccessService folderAccessService;

    public PrivateCloudService(
            FolderRepository folderRepository,
            StoredFileRepository storedFileRepository,
            FolderAccessService folderAccessService) {
        this.folderRepository = folderRepository;
        this.storedFileRepository = storedFileRepository;
        this.folderAccessService = folderAccessService;
    }

    /**
     * One level under private root only: direct subfolders (with {@code child_number} from DB, no nested
     * {@code children}) and files in that folder (any uploader) visible to the user.
     */
    @Transactional(readOnly = true)
    public PrivateCloudTreeResponse buildPrivateCloudTree(String userId) {
        Folder privateRoot = folderRepository
                .findFirstByUserIdAndParentIdIsNullAndShareableFalseOrderByCreatedAtAsc(userId)
                .orElseThrow(() -> new NotFoundException("Private archive root not found"));
        return new PrivateCloudTreeResponse(buildOneLayerUnderFolder(userId, privateRoot));
    }

    /**
     * All shareable roots the user owns or is a member of — each with one layer of children (same JSON shape as
     * {@link #buildPrivateCloudTree} root).
     */
    /**
     * Bất kỳ folder nào user có quyền: cùng shape với {@link #buildPrivateCloudTree} — {@code root} = folder đó + một
     * lớp {@code children}.
     */
    @Transactional(readOnly = true)
    public PrivateCloudTreeResponse buildFolderDetail(String userId, String folderId) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new NotFoundException("Folder not found"));
        return new PrivateCloudTreeResponse(buildOneLayerUnderFolder(userId, folder));
    }

    @Transactional(readOnly = true)
    public PublicWorkspaceResponse buildPublicWorkspace(String userId) {
        LinkedHashMap<String, Folder> roots = new LinkedHashMap<>();
        for (Folder f : folderRepository.findByUserIdAndParentIdIsNullAndShareableTrueOrderByCreatedAtAsc(userId)) {
            if (folderAccessService.canAccessFolder(userId, f.getId())) {
                roots.putIfAbsent(f.getId(), f);
            }
        }
        for (Folder f : folderRepository.findShareableRootsByMembership(userId)) {
            if (folderAccessService.canAccessFolder(userId, f.getId())) {
                roots.putIfAbsent(f.getId(), f);
            }
        }
        List<Folder> ordered = new ArrayList<>(roots.values());
        ordered.sort(Comparator.comparing(Folder::getName, String.CASE_INSENSITIVE_ORDER));
        List<CloudEntryResponse> entries =
                ordered.stream().map(root -> buildOneLayerUnderFolder(userId, root)).toList();
        return new PublicWorkspaceResponse(entries);
    }

    /** One listing layer under {@code folder}: shallow folder rows + file rows; same DTO as private cloud root. */
    public CloudEntryResponse buildOneLayerUnderFolder(String userId, Folder folder) {
        if (!folderAccessService.canAccessFolder(userId, folder.getId())) {
            throw new NotFoundException("Folder not found");
        }

        String rootId = folder.effectiveRootFolderId();

        List<Folder> subfolders =
                folderRepository.findByParentIdAndRootFolderIdOrderByNameAsc(folder.getId(), rootId);
        subfolders = new ArrayList<>(subfolders);
        subfolders.sort(Comparator.comparing(Folder::getName, String.CASE_INSENSITIVE_ORDER));

        List<StoredFile> files = storedFileRepository.findByFolderIdOrderByCreatedAtDesc(folder.getId());
        files = new ArrayList<>(files);
        files.sort(Comparator.comparing(StoredFile::getFileName, String.CASE_INSENSITIVE_ORDER));

        List<CloudComponent> children = new ArrayList<>();
        for (Folder sub : subfolders) {
            if (!folderAccessService.canAccessFolder(userId, sub.getId())) {
                continue;
            }
            children.add(new PrivateCloudFolderShallowLeaf(sub, countDirectChildren(sub)));
        }
        for (StoredFile file : files) {
            if (!folderAccessService.canAccessFile(userId, file)) {
                continue;
            }
            children.add(new PrivateCloudFileLeaf(file, rootId));
        }

        CloudComponent rootComposite = new PrivateCloudFolderComposite(folder, children);
        return rootComposite.toResponse();
    }

    private int countDirectChildren(Folder folder) {
        long dirs = folderRepository.countByParentId(folder.getId());
        long fls = storedFileRepository.countByFolderId(folder.getId());
        long total = dirs + fls;
        if (total > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) total;
    }
}
