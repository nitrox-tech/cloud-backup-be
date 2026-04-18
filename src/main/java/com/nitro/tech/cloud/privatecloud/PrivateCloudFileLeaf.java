package com.nitro.tech.cloud.privatecloud;

import com.nitro.tech.cloud.domain.StoredFile;
import com.nitro.tech.cloud.web.dto.CloudEntryResponse;

/** Composite pattern: leaf — a file in the private tree. */
public final class PrivateCloudFileLeaf implements CloudComponent {

    private final StoredFile file;
    /** Same as parent folder's tree root ({@link Folder#effectiveRootFolderId()}). */
    private final String rootFolderId;

    public PrivateCloudFileLeaf(StoredFile file, String rootFolderId) {
        this.file = file;
        this.rootFolderId = rootFolderId;
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
                file.getCreatedAt(),
                String.valueOf(file.getFileSize()),
                file.getMimeType(),
                file.getMessageId(),
                file.getTelegramFileId());
    }
}
