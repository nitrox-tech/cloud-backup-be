package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Finalize join archive sau khi user join Telegram")
public record FinalizeFolderInviteJoinRequest(
        @NotBlank @JsonProperty("invite_code") @Schema(description = "Mã invite") String inviteCode,
        @NotBlank @JsonProperty("invite_id") @Schema(description = "UUID invite") String inviteId,
        @NotNull @Valid @JsonProperty("telegram_join_proof") TelegramJoinProof telegramJoinProof) {

    @Schema(description = "Proof từ app Telegram")
    public record TelegramJoinProof(
            @NotBlank @JsonProperty("chat_id") @Schema(description = "Telegram chat id đã join") String chatId) {}
}
