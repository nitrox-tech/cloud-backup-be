package com.nitro.tech.cloud.service;

import com.nitro.tech.cloud.config.InviteProperties;
import com.nitro.tech.cloud.domain.Folder;
import com.nitro.tech.cloud.domain.FolderMember;
import com.nitro.tech.cloud.repository.FolderMemberRepository;
import com.nitro.tech.cloud.repository.FolderRepository;
import com.nitro.tech.cloud.repository.StoredFileRepository;
import com.nitro.tech.cloud.repository.UserRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    private final InviteProperties inviteProperties;

    public FolderService(
            FolderRepository folderRepository,
            StoredFileRepository storedFileRepository,
            FolderMemberRepository folderMemberRepository,
            FolderAccessService folderAccessService,
            UserRepository userRepository,
            InviteProperties inviteProperties) {
        this.folderRepository = folderRepository;
        this.storedFileRepository = storedFileRepository;
        this.folderMemberRepository = folderMemberRepository;
        this.folderAccessService = folderAccessService;
        this.userRepository = userRepository;
        this.inviteProperties = inviteProperties;
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

    /**
     * Builds invite payload for a shareable archive: backend root folder id + Telegram supergroup id. Only the tree
     * owner may call; {@code folderId} may be any node in that tree.
     */
    @Transactional(readOnly = true)
    public ArchiveInviteLink buildArchiveInviteLink(String actorId, String folderId) {
        if (!folderAccessService.canAccessFolder(actorId, folderId)) {
            throw new NotFoundException("Folder not found");
        }
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new NotFoundException("Folder not found"));
        String rootId = folder.getRootFolderId() != null ? folder.getRootFolderId() : folder.getId();
        Folder root = folderRepository.findById(rootId).orElseThrow(() -> new NotFoundException("Folder not found"));
        if (!root.isRoot() || !root.isShareable()) {
            throw new IllegalArgumentException("Invite links are only for shareable archive roots");
        }
        if (!folderAccessService.isTreeOwner(actorId, root)) {
            throw new NotFoundException("Folder not found");
        }
        String chatId = root.getTelegramChatId();
        if (chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException(
                    "Set telegram_chat_id on the shareable root before creating an invite link");
        }
        String url = buildInviteUrl(inviteProperties.getBaseUrl(), root.getId(), chatId.trim());
        return new ArchiveInviteLink(url, root.getId(), chatId.trim());
    }

    @Transactional
    public Folder create(String actorId, String name, String parentId, boolean shareable, String telegramChatIdRaw) {
        String telegramChatId = normalizeOptionalTelegramChatId(telegramChatIdRaw);
        if (telegramChatId != null && parentId != null) {
            throw new IllegalArgumentException("telegram_chat_id is only allowed on shareable root folders");
        }
        if (!shareable && parentId == null && telegramChatId != null) {
            throw new IllegalArgumentException("telegram_chat_id is only for shareable archives");
        }

        Folder f = new Folder();
        f.setName(name.trim());
        f.setShareable(shareable);

        if (parentId == null) {
            if (!shareable && folderRepository.countByUserIdAndParentIdIsNullAndShareableFalse(actorId) > 0) {
                throw new ConflictException("Only one private root folder is allowed per user");
            }
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
        f.setShareable(parent.isShareable());
        try {
            return folderRepository.save(f);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("A folder with this name already exists here", e);
        }
    }

    @Transactional
    public Folder move(String actorId, String folderId, String targetParentId) {
        Folder source = folderRepository.findById(folderId).orElseThrow(() -> new NotFoundException("Folder not found"));
        Folder targetParent =
                folderRepository.findById(targetParentId).orElseThrow(() -> new IllegalArgumentException("Parent folder not found"));

        if (!folderAccessService.canAccessFolder(actorId, folderId)) {
            throw new NotFoundException("Folder not found");
        }
        if (source.isRoot()) {
            throw new IllegalArgumentException("Cannot move root folder");
        }
        if (!Objects.equals(source.getRootFolderId(), targetParent.getRootFolderId())) {
            throw new IllegalArgumentException("Only directories inside a root folder can be moved.");
        }

        if (!folderAccessService.canAccessFolder(actorId, targetParentId)) {
            throw new IllegalArgumentException("Parent folder not found");
        }
        if (!targetParent.isShareable()) {
            throw new IllegalArgumentException("Target parent must be in a shareable tree");
        }
        if (folderId.equals(targetParentId) || isDescendantOf(source.getId(), targetParent.getId())) {
            throw new IllegalArgumentException("Cannot move a folder into itself or its descendants");
        }
        if (!sameRoot(source, targetParent)) {
            throw new IllegalArgumentException("Move is only allowed inside the same tree");
        }
        source.setParentId(targetParentId);
        source.setShareable(targetParent.isShareable());
        try {
            return folderRepository.save(source);
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
        List<String> subtreeFolderIds = collectSubtreeFolderIds(folderId);
        storedFileRepository.deleteByFolderIdIn(subtreeFolderIds);
        folderRepository.deleteById(folderId);
    }

    /**
     * BFS: this folder and all descendants (DB deletes children via {@code parent_id} CASCADE).
     */
    private List<String> collectSubtreeFolderIds(String rootFolderId) {
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(rootFolderId);
        List<String> ids = new ArrayList<>();
        while (!queue.isEmpty()) {
            String id = queue.removeFirst();
            ids.add(id);
            for (Folder child : folderRepository.findByParentId(id)) {
                queue.add(child.getId());
            }
        }
        return ids;
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

    /**
     * Owner-only: set the Telegram supergroup / chat id on a shareable root (opaque string).
     */
    @Transactional
    public Folder setTelegramChatId(String actorId, String folderId, String telegramChatIdRaw) {
        Folder root = loadShareableRootForTelegramChat(actorId, folderId);
        root.setTelegramChatId(telegramChatIdRaw.trim());
        return folderRepository.save(root);
    }

    /**
     * Owner-only: remove {@code telegram_chat_id} from a shareable root.
     */
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

    private boolean isDescendantOf(String ancestorId, String nodeId) {
        Folder cursor = folderRepository.findById(nodeId).orElse(null);
        while (cursor != null && cursor.getParentId() != null) {
            if (ancestorId.equals(cursor.getParentId())) {
                return true;
            }
            cursor = folderRepository.findById(cursor.getParentId()).orElse(null);
        }
        return false;
    }

    private static boolean sameRoot(Folder left, Folder right) {
        String leftRoot = left.getRootFolderId() != null ? left.getRootFolderId() : left.getId();
        String rightRoot = right.getRootFolderId() != null ? right.getRootFolderId() : right.getId();
        return leftRoot.equals(rightRoot);
    }

    private static String buildInviteUrl(String baseUrlRaw, String folderId, String telegramChatId) {
        if (baseUrlRaw == null) {
            return null;
        }
        String base = baseUrlRaw.trim();
        if (base.isEmpty()) {
            return null;
        }
        String sep = base.contains("?") ? "&" : "?";
        return base
                + sep
                + "folder_id="
                + URLEncoder.encode(folderId, StandardCharsets.UTF_8)
                + "&telegram_chat_id="
                + URLEncoder.encode(telegramChatId, StandardCharsets.UTF_8);
    }
}
