package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Di chuyển file metadata sang folder khác")
public record MoveFileRequest(
        @NotBlank @JsonProperty("folder_id") @Schema(description = "UUID folder đích") String folderId) {}
