package com.nitro.tech.cloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Server-driven rules for how the <strong>client</strong> should talk to Telegram for <strong>files</strong>.
 * We do <strong>not</strong> prescribe Bot API for storage (policy / ToS risk); use normal user + group membership.
 * Change via env/config without shipping a new app.
 */
@Data
@ConfigurationProperties(prefix = "app.telegram.client")
public class TelegramClientRulesProperties {

    /** Bump when response shape or semantics change. */
    private int schemaVersion = 4;

    @NestedConfigurationProperty
    private final Integration integration = new Integration();

    /** Soft hints for UI / chunking (not Bot API specific). */
    private long maxUploadBytes = 2L * 1024 * 1024 * 1024;

    private long recommendedChunkBytes = 1024 * 1024;

    private int uploadRetryMax = 3;

    /** Public bot username for Telegram Login Widget only (not for file Bot API). */
    private String botUsername = "";

    @NestedConfigurationProperty
    private final UploadRouting uploadRouting = new UploadRouting();

    @NestedConfigurationProperty
    private final ArchiveGroup archiveGroup = new ArchiveGroup();

    @Data
    public static class Integration {
        /**
         * Machine-readable tag, e.g. {@code telegram_user_client} — client uses logged-in user's Telegram session
         * (MTProto / official client patterns), not bot token, for sending/receiving files.
         */
        private String fileStorageModel = "telegram_user_client";

        private String policyNote =
                "Do not use Bot API for file upload/download in the client. Use the user's Telegram account and normal chats/groups. POST /auth/telegram does not verify Telegram Login Widget hash — it trusts the client-supplied telegram user id (intentionally lightweight).";
    }

    @Data
    public static class UploadRouting {

        @NestedConfigurationProperty
        private final PrivateArchive privateArchive = new PrivateArchive();

        @NestedConfigurationProperty
        private final SharedGroup sharedGroup = new SharedGroup();

        @Data
        public static class PrivateArchive {
            private String storageContext = "self_chat_saved_messages_or_cloud";

            private String description =
                    "Non-shareable archive: store files in the user's own Telegram space (e.g. Saved Messages / 'self' chat) using the user's Telegram client session — not Bot API.";
        }

        @Data
        public static class SharedGroup {
            private String storageContext = "telegram_supergroup";

            private String description =
                    "Shareable archive: all collaborators must be added as real members of the same Telegram supergroup. Files are normal group messages; everyone in the group can access them per Telegram. Align app folder_members with who should receive the invite link.";

            private boolean membersMustJoinTelegramSupergroup = true;
        }
    }

    @Data
    public static class ArchiveGroup {

        private String groupTitleMandatoryPrefix = "nitro-tech-cloud-";

        private String groupTitleTemplate = "nitro-tech-cloud-{segment}-{suffix}";

        private String segmentMeaning = "numeric_or_short_archive_identifier";

        private String exampleTitle = "nitro-tech-cloud-123-team-alpha";

        private String groupTitleClientValidationRegex = "^nitro-tech-cloud-[0-9]+-[a-zA-Z0-9._-]+$";

        private int maxGroupTitleLength = 128;

        private String telegramGroupType = "supergroup";

        private String clientOnboardingHint =
                "Create a Telegram supergroup, set the title using the template, invite every collaborator into that group (they must join as users). Link the supergroup chat_id to this shareable root in the app. No bot is required in the group for file access under this model.";
    }
}
