package com.nitro.tech.cloud.service;

import com.nitro.tech.cloud.config.TelegramClientRulesProperties;
import com.nitro.tech.cloud.web.dto.TelegramArchiveGroupConfigResponse;
import com.nitro.tech.cloud.web.dto.TelegramClientRulesResponse;
import org.springframework.stereotype.Service;

@Service
public class TelegramClientRulesService {

    private final TelegramClientRulesProperties properties;

    public TelegramClientRulesService(TelegramClientRulesProperties properties) {
        this.properties = properties;
    }

    public TelegramClientRulesResponse snapshot() {
        String raw = properties.getBotUsername();
        TelegramClientRulesResponse.LoginWidget loginWidget =
                (raw == null || raw.isBlank())
                        ? null
                        : new TelegramClientRulesResponse.LoginWidget(raw.trim());

        var integ = properties.getIntegration();
        var integration =
                new TelegramClientRulesResponse.Integration(
                        integ.getFileStorageModel(), integ.getPolicyNote());

        var ur = properties.getUploadRouting();
        var pa = ur.getPrivateArchive();
        var sg = ur.getSharedGroup();
        var uploadRouting =
                new TelegramClientRulesResponse.UploadRouting(
                        new TelegramClientRulesResponse.UploadRouting.PrivateArchive(
                                pa.getStorageContext(), pa.getDescription()),
                        new TelegramClientRulesResponse.UploadRouting.SharedArchiveSupergroup(
                                sg.getStorageContext(),
                                sg.getDescription(),
                                sg.isMembersMustJoinTelegramSupergroup()));

        var ag = properties.getArchiveGroup();
        var archiveGroup =
                new TelegramClientRulesResponse.ArchiveGroupRules(
                        new TelegramClientRulesResponse.ArchiveGroupRules.GroupTitle(
                                ag.getGroupTitleMandatoryPrefix(),
                                ag.getGroupTitleTemplate(),
                                ag.getSegmentMeaning(),
                                ag.getExampleTitle(),
                                blankToNull(ag.getGroupTitleClientValidationRegex()),
                                ag.getMaxGroupTitleLength()),
                        new TelegramClientRulesResponse.ArchiveGroupRules.Telegram(
                                ag.getTelegramGroupType(), ag.getClientOnboardingHint()));

        return new TelegramClientRulesResponse(
                properties.getSchemaVersion(),
                integration,
                new TelegramClientRulesResponse.Limits(properties.getMaxUploadBytes()),
                new TelegramClientRulesResponse.Upload(
                        properties.getRecommendedChunkBytes(), properties.getUploadRetryMax()),
                uploadRouting,
                archiveGroup,
                loginWidget);
    }

    public TelegramArchiveGroupConfigResponse archiveGroupSnapshot() {
        TelegramClientRulesResponse full = snapshot();
        return new TelegramArchiveGroupConfigResponse(
                full.schemaVersion(), full.archiveGroup());
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
