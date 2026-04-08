package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Di chuyển folder sang parent mới")
public record MoveFolderRequest(
        @NotBlank @JsonProperty("parent_id") @Schema(description = "UUID folder cha mới") String parentId) {}
