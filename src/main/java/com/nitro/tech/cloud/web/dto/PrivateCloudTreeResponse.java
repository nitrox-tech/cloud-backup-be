package com.nitro.tech.cloud.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description =
                "Một folder làm `root` + `children`: với GET /clouds/private và GET /clouds/folders/{id} là **một lớp** "
                        + "folder + file; với GET /clouds/folder-tree là **chỉ folder**, đệ quy theo cây.")
public record PrivateCloudTreeResponse(
        @Schema(
                        description =
                                "`root` = folder gốc của response; `children` tùy endpoint (một lớp hay cây folder đệ quy)")
                CloudEntryResponse root) {}
