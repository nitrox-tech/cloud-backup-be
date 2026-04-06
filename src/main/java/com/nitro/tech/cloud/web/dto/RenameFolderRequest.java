package com.nitro.tech.cloud.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Đổi tên folder")
public record RenameFolderRequest(
        @NotBlank @Size(max = 256) @Schema(description = "Tên mới") String name) {}
