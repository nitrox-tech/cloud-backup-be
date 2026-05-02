package com.nitro.tech.cloud.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê dung lượng lưu trữ của người dùng")
public record CloudStorageStatsResponse(
        @Schema(description = "Tổng dung lượng (Bytes)") long total,
        @Schema(description = "Dung lượng ảnh (Bytes)") long photo,
        @Schema(description = "Dung lượng video (Bytes)") long video,
        @Schema(description = "Dung lượng âm thanh (Bytes)") long audio,
        @Schema(description = "Dung lượng tài liệu (Bytes)") long document,
        @Schema(description = "Dung lượng khác (Bytes)") long other) {}
