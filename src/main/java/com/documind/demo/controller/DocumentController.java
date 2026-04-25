package com.documind.demo.controller;

import com.documind.demo.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(
            @RequestParam("file") MultipartFile file) {
        try {
            if (!file.getOriginalFilename().endsWith(".pdf")) {
                return ResponseEntity.badRequest()
                        .body("Only PDF files are supported.");
            }
            String result = documentService.uploadDocument(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error uploading document: " + e.getMessage());
        }
    }

    @GetMapping("/ask")
    public ResponseEntity<String> askQuestion(
            @RequestParam("question") String question) {
        try {
            String answer = documentService.askQuestion(question);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error processing question: " + e.getMessage());
        }
    }
}