package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Tối đa 15 favorite + 15 recent (theo thời gian), chỉ file user vẫn có quyền xem")
public record CloudHomeResponse(
        @JsonProperty("favorites") @Schema(description = "Sắp xếp theo thời điểm đánh dấu favorite (mới nhất trước)")
                List<CloudEntryResponse> favorites,
        @JsonProperty("recents")
                @Schema(description = "Sắp xếp theo lần cuối tạo/cập nhật metadata (POST /files/metadata) gần nhất trước")
                List<CloudEntryResponse> recents) {}
