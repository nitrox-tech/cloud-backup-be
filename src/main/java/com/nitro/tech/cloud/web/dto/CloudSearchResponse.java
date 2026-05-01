package com.nitro.tech.cloud.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Kết quả tìm kiếm tệp tin")
public record CloudSearchResponse(
        @Schema(description = "Danh sách các tệp tin khớp với tiêu chí") List<CloudEntryResponse> items) {}
