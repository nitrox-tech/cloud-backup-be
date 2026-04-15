package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.ConflictException;
import com.nitro.tech.cloud.service.FolderService;
import com.nitro.tech.cloud.service.NotFoundException;
import com.nitro.tech.cloud.web.dto.FinalizeFolderInviteJoinRequest;
import com.nitro.tech.cloud.web.dto.FinalizeFolderInviteJoinResponse;
import com.nitro.tech.cloud.web.dto.VerifyFolderInviteRequest;
import com.nitro.tech.cloud.web.dto.VerifyFolderInviteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/folder-invites")
@Tag(name = "Folder Invites", description = "Verify invite và finalize join archive shareable")
public class FolderInviteController {

    private final FolderService folderService;

    public FolderInviteController(FolderService folderService) {
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new FolderController.ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new FolderController.ErrorBody(e.getMessage()));
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new FolderController.ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new FolderController.ErrorBody(e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new FolderController.ErrorBody(e.getMessage()));
        }
    }
}
