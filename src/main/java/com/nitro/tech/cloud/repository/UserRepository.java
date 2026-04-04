package com.nitro.tech.cloud.repository;

import com.nitro.tech.cloud.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByTelegramUserId(String telegramUserId);
}
