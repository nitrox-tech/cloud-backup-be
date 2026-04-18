package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Trang danh sách file đã favorite (sort theo thời điểm favorite mới nhất); `page` 1-based giống query")
public record FavoriteFilesPageResponse(
        @JsonProperty("items") List<CloudEntryResponse> items,
        @Schema(description = "Trang hiện tại (1-based, trùng query `page`)") int page,
        int size,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages) {}
