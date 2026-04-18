package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.ConflictException;
import com.nitro.tech.cloud.service.FileMetadataService;
import com.nitro.tech.cloud.service.FolderService;
import com.nitro.tech.cloud.service.NotFoundException;
import com.nitro.tech.cloud.service.PrivateCloudService;
import com.nitro.tech.cloud.web.dto.MoveCloudEntryRequest;
import com.nitro.tech.cloud.web.dto.PrivateCloudTreeResponse;
import com.nitro.tech.cloud.web.dto.PublicWorkspaceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clouds")
@Tag(
        name = "Clouds",
        description = "API nhóm `/clouds` — private root, shareable workspace (listing), và move entry trong cùng cây")
public class CloudsController {

    private final PrivateCloudService privateCloudService;
    private final FileMetadataService fileMetadataService;
    private final FolderService folderService;

    public CloudsController(
            PrivateCloudService privateCloudService,
            FileMetadataService fileMetadataService,
            FolderService folderService) {
        this.privateCloudService = privateCloudService;
        this.fileMetadataService = fileMetadataService;
        this.folderService = folderService;
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

    @Operation(
            summary = "Di chuyển file hoặc folder trong cùng cây",
            description =
                    "Gom logic move file (`PUT /files/{id}/move` cũ) và move folder (`PUT /folders/{id}/move` cũ): đặt entry vào "
                            + "`target_folder_id`, nguồn và đích cùng `root_folder_id`. Không move root folder; file không có "
                            + "folder cha thì không được move. Trùng tên trong đích → 409 (folder).")
    @PutMapping("/entries/{id}/move")
    public ResponseEntity<?> moveEntry(
            @Parameter(description = "UUID file metadata hoặc UUID folder (theo `is_folder`)") @PathVariable String id,
            @Valid @RequestBody MoveCloudEntryRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            if (Boolean.TRUE.equals(body.isFolder())) {
                var moved = folderService.move(userId, id, body.targetFolderId());
                return ResponseEntity.ok(folderService.toCloudEntryShallow(moved));
            }
            var movedFile = fileMetadataService.moveIfAccessible(userId, id, body.targetFolderId());
            return ResponseEntity.ok(fileMetadataService.toCloudEntry(movedFile));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorBody(e.getMessage()));
        }
    }

    @Schema(name = "CloudsError", description = "Lỗi 400/404/409 — message từ server")
    public record ErrorBody(String error) {}
}
