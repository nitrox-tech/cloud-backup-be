package com.nitro.tech.cloud.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Private root + một lớp children (không đệ quy sâu)")
public record PrivateCloudTreeResponse(
        @Schema(description = "Gốc private; `children` chỉ chứa folder/file trực tiếp trong root") CloudEntryResponse root) {}
