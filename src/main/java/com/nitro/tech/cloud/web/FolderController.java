package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.ConflictException;
import com.nitro.tech.cloud.service.FolderService;
import com.nitro.tech.cloud.service.NotFoundException;
import com.nitro.tech.cloud.web.dto.AddFolderMemberRequest;
import com.nitro.tech.cloud.web.dto.CreateFolderInviteRequest;
import com.nitro.tech.cloud.web.dto.CreateFolderInviteResponse;
import com.nitro.tech.cloud.web.dto.CreateFolderRequest;
import com.nitro.tech.cloud.web.dto.FolderMemberResponse;
import com.nitro.tech.cloud.web.dto.FolderResponse;
import com.nitro.tech.cloud.web.dto.ListResponse;
import com.nitro.tech.cloud.web.dto.MoveFolderRequest;
import com.nitro.tech.cloud.web.dto.RenameFolderRequest;
import com.nitro.tech.cloud.web.dto.SetFolderTelegramChatRequest;
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
            return ResponseEntity.status(HttpStatus.CREATED).body(FolderResponse.from(f));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(summary = "Danh sách folder", description = "Cây folder user có quyền xem (theo rule service).")
    @GetMapping
    public ListResponse<FolderResponse> list() {
        String userId = SecurityUtils.currentUserId();
        var items = folderService.list(userId).stream().map(FolderResponse::from).toList();
        return new ListResponse<>(items);
    }

    @Operation(
            summary = "Tạo invite cho archive shareable",
            description =
                    "Chỉ owner archive shareable mới tạo được. {id} có thể là bất kỳ folder trong cây — "
                            + "response luôn dùng UUID root backend và metadata link Telegram.")
    @PostMapping("/{id}/invites")
    public ResponseEntity<?> createInviteCode(
            @Parameter(description = "UUID folder (bất kỳ node trong cây shareable)") @PathVariable String id,
            @Valid @RequestBody CreateFolderInviteRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            var inviteCode = folderService.createArchiveInviteCode(userId, id, body.telegramJoinLink(), body.expiresAt());
            var root = folderService.loadRootById(inviteCode.getFolderId());
            var verifyResult = folderService.verifyInviteCode(inviteCode.getCode());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CreateFolderInviteResponse.from(inviteCode, root, verifyResult.inviteUrl()));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(
            summary = "Gắn Telegram supergroup chat id",
            description = "Áp dụng cho root shareable — id chat nhóm (-100…) để client đồng bộ message.")
    @PutMapping("/{id}/telegram-chat")
    public ResponseEntity<?> setTelegramChat(
            @Parameter(description = "UUID folder (thường là root shareable)") @PathVariable String id,
            @Valid @RequestBody SetFolderTelegramChatRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            return ResponseEntity.ok(FolderResponse.from(folderService.setTelegramChatId(userId, id, body.telegramChatId())));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(summary = "Gỡ Telegram chat id khỏi folder", description = "Xóa telegram_chat_id trên root shareable.")
    @DeleteMapping("/{id}/telegram-chat")
    public ResponseEntity<?> clearTelegramChat(
            @Parameter(description = "UUID folder root shareable") @PathVariable String id) {
        String userId = SecurityUtils.currentUserId();
        try {
            return ResponseEntity.ok(FolderResponse.from(folderService.clearTelegramChatId(userId, id)));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(summary = "Đổi tên folder", description = "409 nếu trùng tên trong cùng parent.")
    @PutMapping("/{id}")
    public ResponseEntity<?> rename(
            @Parameter(description = "UUID folder") @PathVariable String id,
            @Valid @RequestBody RenameFolderRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            return ResponseEntity.ok(FolderResponse.from(folderService.rename(userId, id, body.name())));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(
            summary = "Di chuyển folder",
            description =
                    "Không cho phép move root. Chỉ folder trong cây shareable mới được move, và chỉ move trong cùng cây shareable.")
    @PutMapping("/{id}/move")
    public ResponseEntity<?> move(
            @Parameter(description = "UUID folder") @PathVariable String id,
            @Valid @RequestBody MoveFolderRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            return ResponseEntity.ok(FolderResponse.from(folderService.move(userId, id, body.parentId())));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
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

    @Operation(summary = "Danh sách thành viên folder", description = "Thành viên được mời vào archive chia sẻ (root shareable).")
    @GetMapping("/{id}/members")
    public ResponseEntity<?> listMembers(@Parameter(description = "UUID folder (root shareable)") @PathVariable String id) {
        String userId = SecurityUtils.currentUserId();
        try {
            var items =
                    folderService.listMembers(userId, id).stream().map(FolderMemberResponse::from).toList();
            return ResponseEntity.ok(new ListResponse<>(items));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(
            summary = "Thêm thành viên",
            description = "Thêm user (internal id) vào folder shareable. 409 nếu đã là thành viên.")
    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(
            @Parameter(description = "UUID folder root shareable") @PathVariable String id,
            @Valid @RequestBody AddFolderMemberRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            var m = folderService.addMember(userId, id, body.userId());
            return ResponseEntity.status(HttpStatus.CREATED).body(FolderMemberResponse.from(m));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(summary = "Xóa thành viên", description = "Gỡ user khỏi folder shareable.")
    @DeleteMapping("/{id}/members/{memberUserId}")
    public ResponseEntity<?> removeMember(
            @Parameter(description = "UUID folder") @PathVariable String id,
            @Parameter(description = "UUID user (internal) cần gỡ") @PathVariable String memberUserId) {
        String userId = SecurityUtils.currentUserId();
        try {
            folderService.removeMember(userId, id, memberUserId);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @Schema(name = "FolderError", description = "Lỗi 400/404/409 — message từ server")
    public record ErrorBody(String error) {}
}
