package com.example.alina.controller.api;

import com.example.alina.dto.function.TabulatedFunctionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.alina.service.TabulatedFunctionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tabulated-functions")
public class TabulatedFunctionController {

    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionController.class);

    private final TabulatedFunctionService service;

    public TabulatedFunctionController(TabulatedFunctionService service) {
        this.service = service;
    }

    // Утилита: получение ownerId из заголовка
    private Long getOwnerId(@RequestHeader(value = "X-User-Id", required = false) Long ownerId) {
        if (ownerId == null) {
            logger.warn("Missing required header X-User-Id in request");
            throw new IllegalArgumentException("X-User-Id header is required");
        }
        return ownerId;
    }

    // GET /api/v1/tabulated-functions
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<List<TabulatedFunctionDto>> getAll(
            @RequestHeader("X-User-Id") Long ownerId) {
        return ResponseEntity.ok(service.findAllByOwner(ownerId));
    }

    // GET /api/v1/tabulated-functions/page?page=0&size=10
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<Page<TabulatedFunctionDto>> getPage(
            @RequestHeader("X-User-Id") Long ownerId,
            Pageable pageable) {
        return ResponseEntity.ok(service.findAllByOwner(ownerId, pageable));
    }

    // GET /api/v1/tabulated-functions/sorted/name
    @GetMapping("/sorted/name")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<List<TabulatedFunctionDto>> getAllSortedByName(
            @RequestHeader("X-User-Id") Long ownerId) {
        return ResponseEntity.ok(service.findAllByOwnerSortedByNameAsc(ownerId));
    }

    // GET /api/v1/tabulated-functions/sorted/created
    @GetMapping("/sorted/created")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<List<TabulatedFunctionDto>> getAllSortedByCreatedAt(
            @RequestHeader("X-User-Id") Long ownerId) {
        return ResponseEntity.ok(service.findAllByOwnerSortedByCreatedAtDesc(ownerId));
    }

    // GET /api/v1/tabulated-functions/1
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<TabulatedFunctionDto> getById(
            @RequestHeader("X-User-Id") Long ownerId,
            @PathVariable Long id) {
        return service.findByIdAndOwner(id, ownerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/tabulated-functions/search?q=sin
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<List<TabulatedFunctionDto>> search(
            @RequestHeader("X-User-Id") Long ownerId,
            @RequestParam String q) {
        return ResponseEntity.ok(service.searchByNameFragmentAndOwner(q, ownerId));
    }

    // GET /api/v1/tabulated-functions/by-type/5
    @GetMapping("/by-type/{typeId}")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<List<TabulatedFunctionDto>> getByFunctionType(
            @RequestHeader("X-User-Id") Long ownerId,
            @PathVariable Long typeId) {
        return ResponseEntity.ok(service.findByFunctionTypeIdAndOwner(typeId, ownerId));
    }

    // POST /api/v1/tabulated-functions
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<TabulatedFunctionDto> create(
            @RequestHeader("X-User-Id") Long ownerId,
            @RequestBody TabulatedFunctionDto dto) {
        TabulatedFunctionDto created = service.create(ownerId, dto);
        logger.info("Created TabulatedFunction ID={} for user {}", created.getId(), ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PATCH /api/v1/tabulated-functions/1/name
    @PatchMapping("/{id}/name")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<TabulatedFunctionDto> updateName(
            @RequestHeader("X-User-Id") Long ownerId,
            @PathVariable Long id,
            @RequestBody NameUpdateRequest request) {
        TabulatedFunctionDto updated = service.updateName(id, ownerId, request.getName());
        logger.info("Updated name for TabulatedFunction ID={} for user {}", id, ownerId);
        return ResponseEntity.ok(updated);
    }

    // PATCH /api/v1/tabulated-functions/1/data-and-name
    @PatchMapping("/{id}/data-and-name")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<TabulatedFunctionDto> updateDataAndName(
            @RequestHeader("X-User-Id") Long ownerId,
            @PathVariable Long id,
            @RequestBody DataAndNameUpdateRequest request) {
        TabulatedFunctionDto updated = service.updateDataAndName(
                id, ownerId,
                request.getData(), // byte[]
                request.getName()
        );
        logger.info("Updated data and name for TabulatedFunction ID={} for user {}", id, ownerId);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/v1/tabulated-functions/1
    @DeleteMapping("/{id}")
    @PreAuthorize("@tabulatedFunctionService.canModify(authentication, #id)")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") Long ownerId,
            @PathVariable Long id) {
        service.deleteByIdAndOwner(id, ownerId);
        logger.info("Deleted TabulatedFunction ID={} for user {}", id, ownerId);
        return ResponseEntity.noContent().build();
    }

    // ─── DTO для частичных обновлений ─────────────────────────────

    public static class NameUpdateRequest {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class DataAndNameUpdateRequest {
        private String name;
        private byte[] data; // JSON или бинарные данные

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public byte[] getData() { return data; }
        public void setData(byte[] data) { this.data = data; }
    }
}
