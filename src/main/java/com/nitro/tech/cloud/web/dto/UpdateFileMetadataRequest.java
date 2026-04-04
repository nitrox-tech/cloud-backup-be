package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFileMetadataRequest(
        @NotBlank @Size(max = 1024) @JsonProperty("file_name") String fileName) {}
