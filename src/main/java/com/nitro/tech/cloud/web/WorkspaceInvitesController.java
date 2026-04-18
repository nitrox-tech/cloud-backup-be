package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.ConflictException;
import com.nitro.tech.cloud.service.FolderService;
import com.nitro.tech.cloud.service.NotFoundException;
import com.nitro.tech.cloud.web.dto.CreateFolderInviteRequest;
import com.nitro.tech.cloud.web.dto.CreateFolderInviteResponse;
import com.nitro.tech.cloud.web.dto.FinalizeFolderInviteJoinRequest;
import com.nitro.tech.cloud.web.dto.FinalizeFolderInviteJoinResponse;
import com.nitro.tech.cloud.web.dto.FolderMemberResponse;
import com.nitro.tech.cloud.web.dto.ListResponse;
import com.nitro.tech.cloud.web.dto.VerifyFolderInviteRequest;
import com.nitro.tech.cloud.web.dto.VerifyFolderInviteResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workspace-invites")
@Tag(
        name = "Workspace invites",
        description =
                "Invite / verify / join workspace (root shareable + cây con). Thành viên archive: list / remove (owner).")
public class WorkspaceInvitesController {

    private final FolderService folderService;

    public WorkspaceInvitesController(FolderService folderService) {
        this.folderService = folderService;
    }

    @Operation(
            summary = "Verify invite trước khi join Telegram",
            description = "Kiểm tra invite_code còn hợp lệ hay đã invalid/expired, trả metadata để app join Telegram.")
    @PostMapping("/verify")
    public ResponseEntity<?> verifyInvite(@Valid @RequestBody VerifyFolderInviteRequest body) {
        try {
            return ResponseEntity.ok(VerifyFolderInviteResponse.from(folderService.verifyInviteCode(body.inviteCode())));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(
            summary = "Finalize join archive sau khi join Telegram",
            description = "Xác thực invite + proof chat_id Telegram rồi tạo membership backend.")
    @PostMapping("/finalize-join")
    public ResponseEntity<?> finalizeJoin(@Valid @RequestBody FinalizeFolderInviteJoinRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            var result = folderService.finalizeJoinByInviteCode(
                    userId, body.inviteCode(), body.inviteId(), body.telegramJoinProof().chatId());
            return ResponseEntity.status(HttpStatus.CREATED).body(FinalizeFolderInviteJoinResponse.from(result));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(
            summary = "Tạo invite cho workspace shareable",
            description =
                    "Chỉ owner archive shareable. `{workspaceId}` có thể là UUID root shareable hoặc bất kỳ folder trong cây — "
                            + "server resolve về root; response dùng UUID root và metadata link Telegram.")
    @PostMapping("/{workspaceId}/invites")
    public ResponseEntity<?> createInviteCode(
            @Parameter(description = "UUID workspace (root shareable hoặc folder trong cây)") @PathVariable String workspaceId,
            @Valid @RequestBody CreateFolderInviteRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            var inviteCode =
                    folderService.createArchiveInviteCode(userId, workspaceId, body.telegramJoinLink(), body.expiresAt());
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
            summary = "Danh sách thành viên workspace",
            description = "Owner only. `{workspaceId}` = root shareable hoặc folder trong cây (resolve về root).")
    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<?> listMembers(
            @Parameter(description = "UUID workspace (root hoặc node trong cây shareable)") @PathVariable String workspaceId) {
        String userId = SecurityUtils.currentUserId();
        try {
            var items =
                    folderService.listMembers(userId, workspaceId).stream().map(FolderMemberResponse::from).toList();
            return ResponseEntity.ok(new ListResponse<>(items));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @Operation(summary = "Xóa thành viên workspace", description = "Owner only. Gỡ collaborator khỏi archive shareable.")
    @DeleteMapping("/{workspaceId}/members/{memberUserId}")
    public ResponseEntity<?> removeMember(
            @Parameter(description = "UUID workspace (root hoặc node trong cây shareable)") @PathVariable String workspaceId,
            @Parameter(description = "UUID user (internal) cần gỡ") @PathVariable String memberUserId) {
        String userId = SecurityUtils.currentUserId();
        try {
            folderService.removeMember(userId, workspaceId, memberUserId);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @Schema(name = "WorkspaceInvitesError", description = "Lỗi 400/404/409 — message từ server")
    public record ErrorBody(String error) {}
}
