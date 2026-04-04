package com.nitro.tech.cloud.web.dto;

import java.util.List;

public record ListResponse<T>(List<T> items) {}
