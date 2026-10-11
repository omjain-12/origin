package com.ramlaal.api.controller;

import com.ramlaal.domain.entity.Document;
import com.ramlaal.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty."));
        }
        try {
            Document doc = documentIngestionService.ingestUpload(file);
            return ResponseEntity.ok(Map.of(
                "documentId", doc.getId().toString(),
                "fileName", doc.getFileName(),
                "status", doc.getStatus().name(),
                "chunks", doc.getChunks().size()
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
