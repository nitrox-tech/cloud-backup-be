package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Kiểm tra invite code trước khi join Telegram")
public record VerifyFolderInviteRequest(
        @NotBlank @JsonProperty("invite_code") @Schema(description = "Mã invite cần verify") String inviteCode) {}
