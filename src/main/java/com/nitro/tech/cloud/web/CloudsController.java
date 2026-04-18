package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.PrivateCloudService;
import com.nitro.tech.cloud.web.dto.PrivateCloudTreeResponse;
import com.nitro.tech.cloud.web.dto.PublicWorkspaceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clouds")
@Tag(
        name = "Clouds",
        description = "API nhóm `/clouds` — private root và shareable workspace (một lớp listing)")
public class CloudsController {

    private final PrivateCloudService privateCloudService;

    public CloudsController(PrivateCloudService privateCloudService) {
        this.privateCloudService = privateCloudService;
    }

    @Operation(
            summary = "Lớp con trực tiếp của private root",
            description =
                    "GET `/clouds/private`: private root + **một lớp** folder/file con (không đệ quy). "
                            + "Mỗi phần tử có `is_folder` và `child_number` (với folder con, `child_number` là số con trực tiếp trong DB, không kèm `children`).")
    @GetMapping("/private")
    public PrivateCloudTreeResponse getPrivateCloud() {
        String userId = SecurityUtils.currentUserId();
        return privateCloudService.buildPrivateCloudTree(userId);
    }

    @Operation(
            summary = "Shareable workspace roots (một lớp)",
            description =
                    "GET `/clouds/public-workspace`: mọi root **shareable** mà user **sở hữu** hoặc **có trong** "
                            + "`folder_members`. Mỗi root cùng cấu trúc với `GET /clouds/private` → `root` (một lớp `children`).")
    @GetMapping("/public-workspace")
    public PublicWorkspaceResponse getPublicWorkspace() {
        String userId = SecurityUtils.currentUserId();
        return privateCloudService.buildPublicWorkspace(userId);
    }
}
