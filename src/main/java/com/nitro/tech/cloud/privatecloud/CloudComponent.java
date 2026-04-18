package com.nitro.tech.cloud.privatecloud;

import com.nitro.tech.cloud.web.dto.CloudEntryResponse;

/**
 * Composite pattern: leaf (file) or branch (folder) node for cloud listings. Dùng chung cho private cloud và
 * các API tương tự khác.
 */
public interface CloudComponent {

    boolean isFolder();

    /** Number of direct children (subfolders + files in this folder). Always 0 for files. */
    int directChildCount();

    CloudEntryResponse toResponse();
}
