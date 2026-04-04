package com.nitro.tech.cloud.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameFolderRequest(@NotBlank @Size(max = 256) String name) {}
