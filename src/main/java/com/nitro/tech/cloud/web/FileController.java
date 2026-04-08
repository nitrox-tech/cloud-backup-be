package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.FileMetadataService;
import com.nitro.tech.cloud.service.NotFoundException;
import com.nitro.tech.cloud.web.dto.FileMetadataRequest;
import com.nitro.tech.cloud.web.dto.FileResponse;
import com.nitro.tech.cloud.web.dto.ListResponse;
import com.nitro.tech.cloud.web.dto.MoveFileRequest;
import com.nitro.tech.cloud.web.dto.UpdateFileMetadataRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
@Tag(name = "Files", description = "Metadata file (Telegram message_id, chunk, …). Cần API key + JWT")
public class FileController {

    private final FileMetadataService fileMetadataService;

    public FileController(FileMetadataService fileMetadataService) {
        this.fileMetadataService = fileMetadataService;
    }

    @Operation(
            summary = "Tạo metadata file",
            description = "Lưu thông tin file đã gửi lên Telegram (message_id, telegram_file_id, folder, chunk…).")
    @PostMapping("/metadata")
    public ResponseEntity<?> createMetadata(@Valid @RequestBody FileMetadataRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            var saved = fileMetadataService.save(userId, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(FileResponse.from(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(
            summary = "Danh sách file",
            description = "Lọc theo folder (optional). Không truyền folder_id thì trả về file user có quyền xem theo rule service.")
    @GetMapping
    public ResponseEntity<?> list(
            @Parameter(description = "UUID folder (optional)") @RequestParam(name = "folder_id", required = false)
                    String folderId) {
        String userId = SecurityUtils.currentUserId();
        try {
            var items = fileMetadataService.list(userId, folderId).stream().map(FileResponse::from).toList();
            return ResponseEntity.ok(new ListResponse<>(items));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(summary = "Lấy chi tiết file", description = "Theo id metadata; 404 nếu không tồn tại hoặc không có quyền.")
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@Parameter(description = "UUID file metadata") @PathVariable String id) {
        String userId = SecurityUtils.currentUserId();
        try {
            return ResponseEntity.ok(FileResponse.from(fileMetadataService.getIfAccessible(userId, id)));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(summary = "Đổi tên file (metadata)", description = "Chỉ cập nhật tên hiển thị trong app.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @Parameter(description = "UUID file metadata") @PathVariable String id,
            @Valid @RequestBody UpdateFileMetadataRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            var updated = fileMetadataService.updateIfAccessible(userId, id, body.fileName());
            return ResponseEntity.ok(FileResponse.from(updated));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(summary = "Xóa metadata file", description = "Xóa bản ghi metadata; 404 nếu không có quyền.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@Parameter(description = "UUID file metadata") @PathVariable String id) {
        String userId = SecurityUtils.currentUserId();
        try {
            fileMetadataService.deleteIfAccessible(userId, id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(
            summary = "Di chuyển file metadata",
            description = "Chỉ file đang nằm trong folder shareable mới được move; chỉ move trong cùng cây shareable.")
    @PutMapping("/{id}/move")
    public ResponseEntity<?> move(
            @Parameter(description = "UUID file metadata") @PathVariable String id,
            @Valid @RequestBody MoveFileRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            var moved = fileMetadataService.moveIfAccessible(userId, id, body.folderId());
            return ResponseEntity.ok(FileResponse.from(moved));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @Schema(name = "FileError", description = "Lỗi 400/404 — message từ server")
    public record ErrorBody(String error) {}
}
