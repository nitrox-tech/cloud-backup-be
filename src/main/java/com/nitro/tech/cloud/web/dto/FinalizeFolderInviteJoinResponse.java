package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.service.FolderService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Kết quả finalize join archive")
public record FinalizeFolderInviteJoinResponse(
        boolean joined,
        FolderView folder,
        MembershipView membership) {

    public static FinalizeFolderInviteJoinResponse from(FolderService.FinalizeJoinResult result) {
        return new FinalizeFolderInviteJoinResponse(
                result.joined(),
                new FolderView(result.rootFolder().getId(), result.rootFolder().getName()),
                new MembershipView("member", result.membership().getCreatedAt()));
    }

    public record FolderView(String id, String name) {}

    public record MembershipView(String role, @JsonProperty("joined_at") Instant joinedAt) {}
}
