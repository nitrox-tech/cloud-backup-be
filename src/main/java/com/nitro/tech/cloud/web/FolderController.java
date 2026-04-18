package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.ConflictException;
import com.nitro.tech.cloud.service.FolderService;
import com.nitro.tech.cloud.service.NotFoundException;
import com.nitro.tech.cloud.web.dto.CreateFolderRequest;
import com.nitro.tech.cloud.web.dto.RenameFolderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/folders")
@Tag(name = "Folders", description = "Thư mục / archive Telegram (metadata). Cần API key + JWT")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @Operation(
            summary = "Tạo folder",
            description = "Tạo root hoặc folder con. Root shareable có thể gắn telegram_chat_id. 409 nếu trùng tên trong cùng parent.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateFolderRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            var f = folderService.create(userId, body.name(), body.parentId(), body.shareable(), body.telegramChatId());
            return ResponseEntity.status(HttpStatus.CREATED).body(folderService.toCloudEntryShallow(f));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(summary = "Đổi tên folder", description = "409 nếu trùng tên trong cùng parent.")
    @PutMapping("/{id}")
    public ResponseEntity<?> rename(
            @Parameter(description = "UUID folder") @PathVariable String id,
            @Valid @RequestBody RenameFolderRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            return ResponseEntity.ok(folderService.toCloudEntryShallow(folderService.rename(userId, id, body.name())));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(
            summary = "Xóa folder",
            description = "Xóa folder và toàn bộ folder con; metadata file trong cây cũng bị xóa. Root chỉ owner mới xóa được.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@Parameter(description = "UUID folder") @PathVariable String id) {
        String userId = SecurityUtils.currentUserId();
        try {
            folderService.delete(userId, id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorBody(e.getMessage()));
        }
    }

    @Schema(name = "FolderError", description = "Lỗi 400/404/409 — message từ server")
    public record ErrorBody(String error) {}
}
