package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User nội bộ (UUID) được thêm vào archive shareable")
public record AddFolderMemberRequest(
        @NotBlank @JsonProperty("user_id") @Schema(description = "UUID user trong hệ thống") String userId) {}
