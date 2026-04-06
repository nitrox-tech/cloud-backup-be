package com.nitro.tech.cloud.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Danh sách có bọc { items: [...] }")
public record ListResponse<T>(@Schema(description = "Phần tử") List<T> items) {}
