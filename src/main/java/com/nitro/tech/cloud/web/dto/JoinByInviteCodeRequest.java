package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Join archive bằng invite code")
public record JoinByInviteCodeRequest(
        @NotBlank @JsonProperty("invite_code") @Schema(description = "Mã mời archive shareable") String inviteCode) {}
