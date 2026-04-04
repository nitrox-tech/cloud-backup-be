package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AddFolderMemberRequest(@NotBlank @JsonProperty("user_id") String userId) {}
