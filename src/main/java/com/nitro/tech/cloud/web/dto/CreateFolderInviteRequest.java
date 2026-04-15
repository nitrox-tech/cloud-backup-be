package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Schema(description = "Tạo invite cho archive shareable")
public record CreateFolderInviteRequest(
        @NotBlank
                @JsonProperty("telegram_join_link")
                @Schema(description = "Telegram join link để client mở trước khi finalize join")
                String telegramJoinLink,
        @JsonProperty("expires_at")
                @Schema(description = "Thời điểm hết hạn (UTC), null nghĩa là không hết hạn")
                Instant expiresAt) {}
