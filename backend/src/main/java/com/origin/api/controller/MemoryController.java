package com.origin.api.controller;

import com.origin.domain.entity.UserMemory;
import com.origin.service.UserMemoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/memories")
public class MemoryController {

    private final UserMemoryService userMemoryService;

    public MemoryController(UserMemoryService userMemoryService) {
        this.userMemoryService = userMemoryService;
    }

    @GetMapping
    public ResponseEntity<List<UserMemory>> getMemories() {
        return ResponseEntity.ok(userMemoryService.getAllMemories());
    }

    public record CreateMemoryDto(String factText, String category) {}

    @PostMapping
    public ResponseEntity<UserMemory> createMemory(@RequestBody CreateMemoryDto dto) {
        if (dto.factText() == null || dto.factText().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        UserMemory created = userMemoryService.saveMemory(
            dto.factText(),
            dto.category() != null ? dto.category() : "GENERAL"
        );
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemory(@PathVariable UUID id) {
        userMemoryService.deleteMemory(id);
        return ResponseEntity.noContent().build();
    }
}
