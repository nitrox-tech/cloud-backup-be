package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.security.JwtService;
import com.nitro.tech.cloud.service.TelegramClientRulesService;
import com.nitro.tech.cloud.service.UserAccountService;
import com.nitro.tech.cloud.web.dto.AuthResponse;
import com.nitro.tech.cloud.web.dto.TelegramLoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserAccountService userAccountService;
    private final JwtService jwtService;
    private final TelegramClientRulesService telegramClientRulesService;

    public AuthController(
            UserAccountService userAccountService,
            JwtService jwtService,
            TelegramClientRulesService telegramClientRulesService) {
        this.userAccountService = userAccountService;
        this.jwtService = jwtService;
        this.telegramClientRulesService = telegramClientRulesService;
    }

    @PostMapping("/telegram")
    public ResponseEntity<AuthResponse> telegram(@Valid @RequestBody TelegramLoginRequest body) {
        String telegramUserId = String.valueOf(body.id());
        var user = userAccountService.upsertTelegramUser(telegramUserId, body.username());
        String jwt = jwtService.createAccessToken(user.getId());
        var rules = telegramClientRulesService.snapshot();
        return ResponseEntity.ok(AuthResponse.of(jwt, user.getId(), telegramUserId, body.username(), rules));
    }
}
