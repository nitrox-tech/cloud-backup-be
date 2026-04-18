package com.nitro.tech.cloud.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Một folder làm `root` + một lớp `children` (dùng cho GET /clouds/private và GET /clouds/folders/{id})")
public record PrivateCloudTreeResponse(
        @Schema(description = "`root` = folder được liệt kê; `children` chỉ chứa folder/file trực tiếp trong folder đó")
                CloudEntryResponse root) {}
