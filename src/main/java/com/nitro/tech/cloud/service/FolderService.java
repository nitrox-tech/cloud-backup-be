package com.nitro.tech.cloud.service;

import com.nitro.tech.cloud.domain.Folder;
import com.nitro.tech.cloud.domain.FolderMember;
import com.nitro.tech.cloud.repository.FolderMemberRepository;
import com.nitro.tech.cloud.repository.FolderRepository;
import com.nitro.tech.cloud.repository.StoredFileRepository;
import com.nitro.tech.cloud.repository.UserRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final StoredFileRepository storedFileRepository;
    private final FolderMemberRepository folderMemberRepository;
    private final FolderAccessService folderAccessService;
    private final UserRepository userRepository;

    public FolderService(
            FolderRepository folderRepository,
            StoredFileRepository storedFileRepository,
            FolderMemberRepository folderMemberRepository,
            FolderAccessService folderAccessService,
            UserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.storedFileRepository = storedFileRepository;
        this.folderMemberRepository = folderMemberRepository;
        this.folderAccessService = folderAccessService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Folder> list(String userId) {
        List<Folder> owned = folderRepository.findByUserIdOrderByCreatedAtAsc(userId);
        List<Folder> shared = folderRepository.findAllInSharedTrees(userId);
        LinkedHashMap<String, Folder> merged = new LinkedHashMap<>();
        for (Folder f : owned) {
            merged.put(f.getId(), f);
        }
        for (Folder f : shared) {
            merged.putIfAbsent(f.getId(), f);
        }
        return merged.values().stream()
                .sorted(Comparator.comparing(Folder::getCreatedAt))
                .toList();
    }

    @Transactional
    public Folder create(String actorId, String name, String parentId, boolean shareable, String telegramChatIdRaw) {
        if (shareable && parentId != null) {
            throw new IllegalArgumentException("Only root folders can be shareable");
        }
        String telegramChatId = normalizeOptionalTelegramChatId(telegramChatIdRaw);
        if (telegramChatId != null && (!shareable || parentId != null)) {
            throw new IllegalArgumentException("telegram_chat_id is only allowed on shareable root folders");
        }
        if (!shareable && telegramChatId != null) {
            throw new IllegalArgumentException("telegram_chat_id is only for shareable archives");
        }

        Folder f = new Folder();
        f.setName(name.trim());
        f.setShareable(shareable);

        if (parentId == null) {
            f.setUserId(actorId);
            f.setParentId(null);
            if (shareable) {
                f.setTelegramChatId(telegramChatId);
            }
            Folder saved = folderRepository.save(f);
            saved.setRootFolderId(saved.getId());
            try {
                return folderRepository.save(saved);
            } catch (DataIntegrityViolationException e) {
                throw new ConflictException("A folder with this name already exists here", e);
            }
        }

        if (!folderAccessService.canAccessFolder(actorId, parentId)) {
            throw new IllegalArgumentException("Parent folder not found");
        }
        Folder parent = folderRepository
                .findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent folder not found"));
        String rootId = parent.getRootFolderId() != null ? parent.getRootFolderId() : parent.getId();
        Folder root = folderRepository.findById(rootId).orElse(parent);
        f.setUserId(root.getUserId());
        f.setParentId(parentId);
        f.setRootFolderId(rootId);
        f.setShareable(false);
        try {
            return folderRepository.save(f);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("A folder with this name already exists here", e);
        }
    }

    @Transactional
    public Folder rename(String userId, String folderId, String name) {
        Folder f = folderRepository
                .findById(folderId)
                .orElseThrow(() -> new NotFoundException("Folder not found"));
        if (!folderAccessService.canAccessFolder(userId, folderId)) {
            throw new NotFoundException("Folder not found");
        }
        f.setName(name.trim());
        try {
            return folderRepository.save(f);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("A folder with this name already exists here", e);
        }
    }

    @Transactional
    public void delete(String userId, String folderId) {
        Folder f = folderRepository.findById(folderId).orElse(null);
        if (f == null || !folderAccessService.canAccessFolder(userId, folderId)) {
            throw new NotFoundException("Folder not found");
        }
        if (f.isRoot() && !folderAccessService.isTreeOwner(userId, f)) {
            throw new IllegalArgumentException("Only the archive owner can delete the root folder");
        }
        long subfolders = folderRepository.countByUserIdAndParentId(f.getUserId(), folderId);
        long files = storedFileRepository.countByFolderId(folderId);
        if (subfolders > 0 || files > 0) {
            throw new ConflictException("Folder is not empty");
        }
        folderRepository.deleteById(folderId);
    }

    @Transactional(readOnly = true)
    public List<FolderMember> listMembers(String actorId, String rootFolderId) {
        Folder root = loadShareableRootOrThrow(rootFolderId);
        if (!folderAccessService.isTreeOwner(actorId, root)) {
            throw new NotFoundException("Folder not found");
        }
        return folderMemberRepository.findByFolderIdOrderByCreatedAtAsc(root.getId());
    }

    @Transactional
    public FolderMember addMember(String actorId, String rootFolderId, String memberUserId) {
        Folder root = loadShareableRootOrThrow(rootFolderId);
        if (!folderAccessService.isTreeOwner(actorId, root)) {
            throw new NotFoundException("Folder not found");
        }
        if (memberUserId.equals(root.getUserId())) {
            throw new IllegalArgumentException("Archive owner is already a member implicitly");
        }
        if (!userRepository.existsById(memberUserId)) {
            throw new IllegalArgumentException("User not found");
        }
        if (folderMemberRepository.existsByFolderIdAndUserId(root.getId(), memberUserId)) {
            throw new ConflictException("User is already in this archive");
        }
        FolderMember m = new FolderMember();
        m.setFolderId(root.getId());
        m.setUserId(memberUserId);
        try {
            return folderMemberRepository.save(m);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("User is already in this archive", e);
        }
    }

    @Transactional
    public void removeMember(String actorId, String rootFolderId, String memberUserId) {
        Folder root = loadShareableRootOrThrow(rootFolderId);
        if (!folderAccessService.isTreeOwner(actorId, root)) {
            throw new NotFoundException("Folder not found");
        }
        if (!folderMemberRepository.existsByFolderIdAndUserId(root.getId(), memberUserId)) {
            throw new NotFoundException("Member not found");
        }
        folderMemberRepository.deleteByFolderIdAndUserId(root.getId(), memberUserId);
    }

    private Folder loadShareableRootOrThrow(String folderId) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new NotFoundException("Folder not found"));
        String rootId = folder.getRootFolderId() != null ? folder.getRootFolderId() : folder.getId();
        Folder root = folderRepository.findById(rootId).orElseThrow(() -> new NotFoundException("Folder not found"));
        if (!root.isRoot() || !root.isShareable()) {
            throw new IllegalArgumentException("Members can only be managed on shareable root archives");
        }
        return root;
    }

    /** Owner-only: set the Telegram supergroup / chat id on a shareable root (opaque string). */
    @Transactional
    public Folder setTelegramChatId(String actorId, String folderId, String telegramChatIdRaw) {
        Folder root = loadShareableRootForTelegramChat(actorId, folderId);
        root.setTelegramChatId(telegramChatIdRaw.trim());
        return folderRepository.save(root);
    }

    /** Owner-only: remove {@code telegram_chat_id} from a shareable root. */
    @Transactional
    public Folder clearTelegramChatId(String actorId, String folderId) {
        Folder root = loadShareableRootForTelegramChat(actorId, folderId);
        root.setTelegramChatId(null);
        return folderRepository.save(root);
    }

    private Folder loadShareableRootForTelegramChat(String actorId, String folderId) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new NotFoundException("Folder not found"));
        if (!folderAccessService.canAccessFolder(actorId, folderId)) {
            throw new NotFoundException("Folder not found");
        }
        String rootId = folder.getRootFolderId() != null ? folder.getRootFolderId() : folder.getId();
        Folder root = folderRepository.findById(rootId).orElseThrow(() -> new NotFoundException("Folder not found"));
        if (!root.isRoot() || !root.isShareable()) {
            throw new IllegalArgumentException("telegram_chat_id can only be set on shareable root archives");
        }
        if (!folderAccessService.isTreeOwner(actorId, root)) {
            throw new NotFoundException("Folder not found");
        }
        return root;
    }

    private static String normalizeOptionalTelegramChatId(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        return s.isEmpty() ? null : s;
    }
}
