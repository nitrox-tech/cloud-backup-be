package com.nitro.tech.cloud.privatecloud;

import com.nitro.tech.cloud.domain.Folder;
import com.nitro.tech.cloud.web.dto.CloudEntryResponse;
import java.util.ArrayList;
import java.util.List;

/** Composite pattern: branch — a folder containing ordered child components (folders + files). */
public final class PrivateCloudFolderComposite implements CloudComponent {

    private final Folder folder;
    private final List<CloudComponent> children;
    /** {@link com.nitro.tech.cloud.domain.Folder#getTelegramChatId()} của root cây (có thể null). */
    private final String treeTelegramChatId;

    public PrivateCloudFolderComposite(Folder folder, List<CloudComponent> children, String treeTelegramChatId) {
        this.folder = folder;
        this.children = List.copyOf(new ArrayList<>(children));
        this.treeTelegramChatId = treeTelegramChatId;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public int directChildCount() {
        return children.size();
    }

    @Override
    public CloudEntryResponse toResponse() {
        List<CloudEntryResponse> childDtos = children.stream().map(CloudComponent::toResponse).toList();
        return CloudEntryResponse.forFolder(
                folder.getId(),
                folder.getName(),
                folder.effectiveRootFolderId(),
                treeTelegramChatId,
                folder.getCreatedAt(),
                childDtos.size(),
                childDtos);
    }
}
