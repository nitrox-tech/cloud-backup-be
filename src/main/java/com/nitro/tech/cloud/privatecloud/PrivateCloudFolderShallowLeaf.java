package com.nitro.tech.cloud.privatecloud;

import com.nitro.tech.cloud.domain.Folder;
import com.nitro.tech.cloud.web.dto.CloudEntryResponse;

/**
 * Composite: folder node at listing depth — exposes {@code child_number} from DB but does not embed
 * {@code children} (single-layer listing).
 */
public final class PrivateCloudFolderShallowLeaf implements CloudComponent {

    private final Folder folder;
    private final int directChildCount;
    private final String treeTelegramChatId;

    public PrivateCloudFolderShallowLeaf(Folder folder, int directChildCount, String treeTelegramChatId) {
        this.folder = folder;
        this.directChildCount = directChildCount;
        this.treeTelegramChatId = treeTelegramChatId;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public int directChildCount() {
        return directChildCount;
    }

    @Override
    public CloudEntryResponse toResponse() {
        return CloudEntryResponse.forFolderShallow(
                folder.getId(),
                folder.getName(),
                folder.effectiveRootFolderId(),
                treeTelegramChatId,
                folder.getCreatedAt(),
                directChildCount);
    }
}
