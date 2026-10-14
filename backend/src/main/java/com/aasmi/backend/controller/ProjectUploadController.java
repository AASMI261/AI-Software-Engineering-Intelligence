package com.aasmi.backend.controller;

import com.aasmi.backend.service.ProjectUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/projects")
public class ProjectUploadController {

    private final ProjectUploadService projectUploadService;

    public ProjectUploadController(ProjectUploadService projectUploadService) {
        this.projectUploadService = projectUploadService;
    }

    @PostMapping("/{projectId}/upload")
    public ResponseEntity<String> uploadProject(
            @PathVariable String projectId,
            @RequestParam("file") MultipartFile file) {

        try {

            String result = projectUploadService.uploadProject(projectId, file);

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body("Upload failed: " + e.getMessage());
        }
    }
}