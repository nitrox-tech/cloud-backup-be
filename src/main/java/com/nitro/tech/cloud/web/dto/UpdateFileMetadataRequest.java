package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Cập nhật tên file metadata")
public record UpdateFileMetadataRequest(
        @NotBlank @Size(max = 1024) @JsonProperty("file_name") @Schema(description = "Tên mới") String fileName) {}
