package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.domain.FolderMember;
import java.time.Instant;

public record FolderMemberResponse(
        String id,
        @JsonProperty("folder_id") String folderId,
        @JsonProperty("user_id") String userId,
        @JsonProperty("created_at") Instant createdAt) {

    public static FolderMemberResponse from(FolderMember m) {
        return new FolderMemberResponse(m.getId(), m.getFolderId(), m.getUserId(), m.getCreatedAt());
    }
}
