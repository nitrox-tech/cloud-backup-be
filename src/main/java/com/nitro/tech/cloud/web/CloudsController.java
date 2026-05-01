package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.CloudHomeService;
import com.nitro.tech.cloud.service.ConflictException;
import com.nitro.tech.cloud.service.FileMetadataService;
import com.nitro.tech.cloud.service.FolderService;
import com.nitro.tech.cloud.service.NotFoundException;
import com.nitro.tech.cloud.service.PrivateCloudService;
import com.nitro.tech.cloud.web.dto.CloudHomeResponse;
import com.nitro.tech.cloud.web.dto.CloudSearchResponse;
import com.nitro.tech.cloud.web.dto.FavoriteFilesPageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clouds")
@Tag(
        name = "Clouds",
        description =
                "API nhóm `/clouds` — private root, chi tiết folder + một lớp con, shareable workspace (listing), move entry")
public class CloudsController {

    private static final int FAVORITE_PAGE_MAX_SIZE = 100;

    private final PrivateCloudService privateCloudService;
    private final CloudHomeService cloudHomeService;
    private final FileMetadataService fileMetadataService;
    private final FolderService folderService;

    public CloudsController(
            PrivateCloudService privateCloudService,
            CloudHomeService cloudHomeService,
            FileMetadataService fileMetadataService,
            FolderService folderService) {
        this.privateCloudService = privateCloudService;
        this.cloudHomeService = cloudHomeService;
        this.fileMetadataService = fileMetadataService;
        this.folderService = folderService;
    }

    @Operation(
            summary = "Home cloud — favorite & recent",
            description =
                    "Tối đa 15 file favorite và 15 file recent (theo thời gian favorite / lần cuối POST metadata), chỉ file user vẫn có quyền xem.")
    @GetMapping("/home")
    public CloudHomeResponse getHome() {
        return cloudHomeService.buildHome(SecurityUtils.currentUserId());
    }

    @Operation(
            summary = "Danh sách file đã favorite (phân trang)",
            description =
                    "Sort theo `created_at` của favorite (mới nhất trước). **`page` theo UI: bắt đầu từ 1** (trang đầu = `page=1`); backend map sang Spring 0-based. "
                            + "`size` mặc định 20, tối đa 100. Response `page` cùng quy ước 1-based.")
    @GetMapping("/favorite")
    public FavoriteFilesPageResponse listFavoriteFiles(
            @Parameter(description = "Trang 1-based, tối thiểu 1 (trang đầu)") @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        int uiPage = Math.max(1, page);
        int safeSize = Math.min(FAVORITE_PAGE_MAX_SIZE, Math.max(1, size));
        return cloudHomeService.pageFavorites(SecurityUtils.currentUserId(), uiPage, safeSize);
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
            summary = "Chi tiết folder + một lớp con",
            description =
                    "GET `/clouds/folders/{id}`: folder bất kỳ user có quyền — cùng JSON với `GET /clouds/private` "
                            + "(`root` = folder đích, `children` = một lớp folder/file con, không đệ quy).")
    @GetMapping("/folders/{id}")
    public PrivateCloudTreeResponse getFolderDetail(
            @Parameter(description = "UUID folder") @PathVariable("id") String id) {
        return privateCloudService.buildFolderDetail(SecurityUtils.currentUserId(), id);
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
            summary = "Cây folder (không file) từ root của cây",
            description =
                    "GET `/clouds/folder-tree?folderId=…` **hoặc** `?fileId=…` (đúng một tham số): từ folder/file bất kỳ "
                            + "user có quyền, resolve **root cây** (`root_folder_id` / archive root), rồi build **cây đệ quy** "
                            + "chỉ thư mục — không đưa file vào JSON (chỉ đọc metadata file tối thiểu khi dùng `fileId`). "
                            + "`child_number` = số subfolder trực tiếp (không tính file).")
    @GetMapping("/folder-tree")
    public PrivateCloudTreeResponse getFolderTreeOnly(
            @Parameter(description = "UUID folder bất kỳ trong cây (xor với fileId)") @RequestParam(required = false)
                    String folderId,
            @Parameter(description = "UUID file metadata bất kỳ trong cây (xor với folderId)") @RequestParam(required = false)
                    String fileId) {
        return privateCloudService.buildFolderTreeFromFolderOrFile(SecurityUtils.currentUserId(), folderId, fileId);
    }

    @Operation(
            summary = "Tìm kiếm tệp tin (metadata-based)",
            description =
                    "Tìm kiếm tập trung dựa trên metadata trong database. Hỗ trợ lọc theo từ khóa, phạm vi (private/shared), loại tệp và thời gian.")
    @GetMapping("/search")
    public CloudSearchResponse searchFiles(
            @Parameter(description = "Từ khóa tìm kiếm (tên file)", required = true) @RequestParam String q,
            @Parameter(description = "Phạm vi: all, private, shared") @RequestParam(required = false, defaultValue = "all")
                    String source,
            @Parameter(description = "Loại tệp: all, image, video, audio, document, archive")
                    @RequestParam(required = false, defaultValue = "all")
                    String file_type,
            @Parameter(description = "Giới hạn thời gian (số ngày)") @RequestParam(required = false) Integer days) {
        var results = fileMetadataService.searchFiles(SecurityUtils.currentUserId(), q, source, file_type, days);
        return new CloudSearchResponse(results);
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
