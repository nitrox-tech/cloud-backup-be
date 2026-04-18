package com.nitro.tech.cloud.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Danh sách shareable roots; mỗi phần tử cùng shape với field {@link PrivateCloudTreeResponse#root()} kiểu
 * {@link CloudEntryResponse} (một lớp {@code children}).
 */
@Schema(description = "Các root archive shareable (của user hoặc được mời); mỗi root một lớp con")
public record PublicWorkspaceResponse(
        @Schema(description = "Mỗi entry = một shareable root + children một lớp") List<CloudEntryResponse> roots) {}
