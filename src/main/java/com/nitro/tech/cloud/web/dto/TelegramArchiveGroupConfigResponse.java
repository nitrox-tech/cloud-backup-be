package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Payload for {@code GET /config/telegram/archive-group} (same {@code archive_group} block as in full Telegram client config).
 */
@Schema(description = "Chỉ block archive_group + schema_version — gợi ý tạo supergroup")
public record TelegramArchiveGroupConfigResponse(
        @JsonProperty("schema_version") int schemaVersion,
        @JsonProperty("archive_group") TelegramClientRulesResponse.ArchiveGroupRules archiveGroup) {}
