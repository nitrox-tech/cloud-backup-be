package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.TelegramClientRulesService;
import com.nitro.tech.cloud.web.dto.TelegramArchiveGroupConfigResponse;
import com.nitro.tech.cloud.web.dto.TelegramClientRulesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Call when JWT is still valid. Full rules are also embedded in {@code POST /auth/telegram} after login.
 */
@RestController
@RequestMapping("/config")
@Tag(
        name = "Cấu hình client Telegram",
        description = "Quy tắc gợi ý cho app/mobile (cần API key + JWT). Cũng nhúng trong response sau POST /auth/telegram")
public class TelegramClientConfigController {

    private final TelegramClientRulesService telegramClientRulesService;

    public TelegramClientConfigController(TelegramClientRulesService telegramClientRulesService) {
        this.telegramClientRulesService = telegramClientRulesService;
    }

    @Operation(
            summary = "Snapshot quy tắc Telegram client",
            description = "Giới hạn upload, chunk, routing archive, gợi ý nhóm Telegram, v.v.")
    @GetMapping("/telegram")
    public TelegramClientRulesResponse telegramRules() {
        return telegramClientRulesService.snapshot();
    }

    @Operation(
            summary = "Quy tắc archive group (supergroup)",
            description = "Pattern tiêu đề nhóm, regex, gợi ý onboarding — dùng khi tạo archive chia sẻ.")
    @GetMapping("/telegram/archive-group")
    public TelegramArchiveGroupConfigResponse telegramArchiveGroupRules() {
        return telegramClientRulesService.archiveGroupSnapshot();
    }
}
