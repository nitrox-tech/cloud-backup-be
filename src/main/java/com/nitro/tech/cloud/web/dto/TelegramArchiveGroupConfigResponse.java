package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for {@code GET /config/telegram/archive-group} (same {@code archive_group} block as in full Telegram client config).
 */
public record TelegramArchiveGroupConfigResponse(
        @JsonProperty("schema_version") int schemaVersion,
        @JsonProperty("archive_group") TelegramClientRulesResponse.ArchiveGroupRules archiveGroup) {}
