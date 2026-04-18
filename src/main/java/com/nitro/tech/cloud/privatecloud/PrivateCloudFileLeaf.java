package com.nitro.tech.cloud.privatecloud;

import com.nitro.tech.cloud.domain.StoredFile;
import com.nitro.tech.cloud.web.dto.CloudEntryResponse;
import com.nitro.tech.cloud.web.dto.CloudUserResponse;

/** Composite pattern: leaf — a file in the private tree. */
public final class PrivateCloudFileLeaf implements CloudComponent {

    private final StoredFile file;
    /** Same as parent folder's tree root ({@link Folder#effectiveRootFolderId()}). */
    private final String rootFolderId;
    private final String treeTelegramChatId;
    private final CloudUserResponse createdBy;

    public PrivateCloudFileLeaf(
            StoredFile file, String rootFolderId, String treeTelegramChatId, CloudUserResponse createdBy) {
        this.file = file;
        this.rootFolderId = rootFolderId;
        this.treeTelegramChatId = treeTelegramChatId;
        this.createdBy = createdBy;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public int directChildCount() {
        return 0;
    }

    @Override
    public CloudEntryResponse toResponse() {
        return CloudEntryResponse.forFile(
                file.getId(),
                file.getFileName(),
                rootFolderId,
                treeTelegramChatId,
                file.getFolderId(),
                createdBy,
                file.getCreatedAt(),
                file.getMimeType(),
                String.valueOf(file.getFileSize()),
                file.getMessageId(),
                file.getTelegramFileId(),
                null);
    }
}
