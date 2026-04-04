package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.ConflictException;
import com.nitro.tech.cloud.service.FolderService;
import com.nitro.tech.cloud.service.NotFoundException;
import com.nitro.tech.cloud.web.dto.AddFolderMemberRequest;
import com.nitro.tech.cloud.web.dto.CreateFolderRequest;
import com.nitro.tech.cloud.web.dto.FolderMemberResponse;
import com.nitro.tech.cloud.web.dto.FolderResponse;
import com.nitro.tech.cloud.web.dto.ListResponse;
import com.nitro.tech.cloud.web.dto.RenameFolderRequest;
import com.nitro.tech.cloud.web.dto.SetFolderTelegramChatRequest;
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
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

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

    @GetMapping
    public ListResponse<FolderResponse> list() {
        String userId = SecurityUtils.currentUserId();
        var items = folderService.list(userId).stream().map(FolderResponse::from).toList();
        return new ListResponse<>(items);
    }

    @PutMapping("/{id}/telegram-chat")
    public ResponseEntity<?> setTelegramChat(
            @PathVariable String id, @Valid @RequestBody SetFolderTelegramChatRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            return ResponseEntity.ok(FolderResponse.from(folderService.setTelegramChatId(userId, id, body.telegramChatId())));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/telegram-chat")
    public ResponseEntity<?> clearTelegramChat(@PathVariable String id) {
        String userId = SecurityUtils.currentUserId();
        try {
            return ResponseEntity.ok(FolderResponse.from(folderService.clearTelegramChatId(userId, id)));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> rename(@PathVariable String id, @Valid @RequestBody RenameFolderRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            return ResponseEntity.ok(FolderResponse.from(folderService.rename(userId, id, body.name())));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorBody(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
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

    @GetMapping("/{id}/members")
    public ResponseEntity<?> listMembers(@PathVariable String id) {
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

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable String id, @Valid @RequestBody AddFolderMemberRequest body) {
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

    @DeleteMapping("/{id}/members/{memberUserId}")
    public ResponseEntity<?> removeMember(@PathVariable String id, @PathVariable String memberUserId) {
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

    public record ErrorBody(String error) {}
}
