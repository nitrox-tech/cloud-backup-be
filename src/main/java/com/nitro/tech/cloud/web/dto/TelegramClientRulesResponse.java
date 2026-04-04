package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Serializable snapshot returned by {@code GET /config/telegram} and embedded in login response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelegramClientRulesResponse(
        @JsonProperty("schema_version") int schemaVersion,
        Integration integration,
        Limits limits,
        Upload upload,
        @JsonProperty("upload_routing") UploadRouting uploadRouting,
        @JsonProperty("archive_group") ArchiveGroupRules archiveGroup,
        @JsonProperty("login_widget") LoginWidget loginWidget) {

    public record Integration(
            @JsonProperty("file_storage_model") String fileStorageModel,
            @JsonProperty("policy_note") String policyNote) {}

    public record Limits(@JsonProperty("max_upload_bytes") long maxUploadBytes) {}

    public record Upload(
            @JsonProperty("recommended_chunk_bytes") long recommendedChunkBytes,
            @JsonProperty("retry_max") int retryMax) {}

    public record UploadRouting(
            @JsonProperty("private_archive") PrivateArchive privateArchive,
            @JsonProperty("shared_archive_supergroup") SharedArchiveSupergroup sharedArchiveSupergroup) {

        public record PrivateArchive(
                @JsonProperty("storage_context") String storageContext, String description) {}

        public record SharedArchiveSupergroup(
                @JsonProperty("storage_context") String storageContext,
                String description,
                @JsonProperty("members_must_join_telegram_supergroup") boolean membersMustJoinTelegramSupergroup) {}
    }

    public record ArchiveGroupRules(
            @JsonProperty("group_title") GroupTitle groupTitle,
            Telegram telegram) {

        public record GroupTitle(
                @JsonProperty("must_start_with") String mustStartWith,
                @JsonProperty("title_template") String titleTemplate,
                @JsonProperty("segment_meaning") String segmentMeaning,
                String example,
                @JsonProperty("client_validation_regex") String clientValidationRegex,
                @JsonProperty("max_title_length") int maxTitleLength) {}

        public record Telegram(
                @JsonProperty("group_type") String groupType,
                @JsonProperty("client_onboarding_hint") String clientOnboardingHint) {}
    }

    public record LoginWidget(@JsonProperty("bot_username") String botUsername) {}
}
