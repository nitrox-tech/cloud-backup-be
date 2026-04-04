package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.TelegramClientRulesService;
import com.nitro.tech.cloud.web.dto.TelegramArchiveGroupConfigResponse;
import com.nitro.tech.cloud.web.dto.TelegramClientRulesResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Call when JWT is still valid. Full rules are also embedded in {@code POST /auth/telegram} after login.
 */
@RestController
@RequestMapping("/config")
public class TelegramClientConfigController {

    private final TelegramClientRulesService telegramClientRulesService;

    public TelegramClientConfigController(TelegramClientRulesService telegramClientRulesService) {
        this.telegramClientRulesService = telegramClientRulesService;
    }

    @GetMapping("/telegram")
    public TelegramClientRulesResponse telegramRules() {
        return telegramClientRulesService.snapshot();
    }

    /** Rules for creating / onboarding a Telegram supergroup for a shareable archive (title pattern, bot, hints). */
    @GetMapping("/telegram/archive-group")
    public TelegramArchiveGroupConfigResponse telegramArchiveGroupRules() {
        return telegramClientRulesService.archiveGroupSnapshot();
    }
}
