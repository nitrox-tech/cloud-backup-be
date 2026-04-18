package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Di chuyển file hoặc folder vào folder đích (cùng root tree)")
public record MoveCloudEntryRequest(
        @NotNull @JsonProperty("is_folder") @Schema(description = "true = folder, false = file metadata") Boolean isFolder,
        @NotBlank @JsonProperty("target_folder_id") @Schema(description = "UUID folder đích (parent mới)") String targetFolderId) {}
