package com.nitro.tech.cloud.service;

import com.nitro.tech.cloud.domain.User;
import com.nitro.tech.cloud.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

    private final UserRepository userRepository;

    public UserAccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                    return userRepository.save(u);
                });
    }
}
