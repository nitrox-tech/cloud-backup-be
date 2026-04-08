package com.nitro.tech.cloud.service;

import com.nitro.tech.cloud.domain.User;
import com.nitro.tech.cloud.domain.Folder;
import com.nitro.tech.cloud.repository.FolderRepository;
import com.nitro.tech.cloud.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

    private final UserRepository userRepository;
    private final FolderRepository folderRepository;

    public UserAccountService(UserRepository userRepository, FolderRepository folderRepository) {
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
    }

    /**
     * Upsert by Telegram user id; returns row with stable internal {@link User#getId()}.
     */
    @Transactional
    public User upsertTelegramUser(String telegramUserId, String username) {
        return userRepository
                .findByTelegramUserId(telegramUserId)
                .map(existing -> {
                    existing.setUsername(username);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User u = new User();
                    u.setTelegramUserId(telegramUserId);
                    u.setUsername(username);
                    User savedUser = userRepository.save(u);
                    createDefaultPrivateRootFolder(savedUser.getId());
                    return savedUser;
                });
    }

    private void createDefaultPrivateRootFolder(String userId) {
        Folder root = new Folder();
        root.setUserId(userId);
        root.setName("Private Folder");
        root.setParentId(null);
        root.setShareable(false);
        Folder saved = folderRepository.save(root);
        saved.setRootFolderId(saved.getId());
        folderRepository.save(saved);
    }
}
