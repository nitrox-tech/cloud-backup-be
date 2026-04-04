package com.nitro.tech.cloud.web;

import com.nitro.tech.cloud.service.FileMetadataService;
import com.nitro.tech.cloud.service.NotFoundException;
import com.nitro.tech.cloud.web.dto.FileMetadataRequest;
import com.nitro.tech.cloud.web.dto.FileResponse;
import com.nitro.tech.cloud.web.dto.ListResponse;
import com.nitro.tech.cloud.web.dto.UpdateFileMetadataRequest;
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
public class FileController {

    private final FileMetadataService fileMetadataService;

    public FileController(FileMetadataService fileMetadataService) {
        this.fileMetadataService = fileMetadataService;
    }

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

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(name = "folder_id", required = false) String folderId) {
        String userId = SecurityUtils.currentUserId();
        try {
            var items = fileMetadataService.list(userId, folderId).stream().map(FileResponse::from).toList();
            return ResponseEntity.ok(new ListResponse<>(items));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorBody(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        String userId = SecurityUtils.currentUserId();
        try {
            return ResponseEntity.ok(FileResponse.from(fileMetadataService.getIfAccessible(userId, id)));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody UpdateFileMetadataRequest body) {
        String userId = SecurityUtils.currentUserId();
        try {
            var updated = fileMetadataService.updateIfAccessible(userId, id, body.fileName());
            return ResponseEntity.ok(FileResponse.from(updated));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        String userId = SecurityUtils.currentUserId();
        try {
            fileMetadataService.deleteIfAccessible(userId, id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
        }
    }

    public record ErrorBody(String error) {}
}
