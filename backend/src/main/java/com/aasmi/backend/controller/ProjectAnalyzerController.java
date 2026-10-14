package com.aasmi.backend.controller;

import com.aasmi.backend.model.Analysis;
import com.aasmi.backend.service.ProjectAnalyzerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectAnalyzerController {

    private final ProjectAnalyzerService projectAnalyzerService;

    public ProjectAnalyzerController(
            ProjectAnalyzerService projectAnalyzerService) {
        this.projectAnalyzerService = projectAnalyzerService;
    }

    @PostMapping("/{projectId}/analyze")
    public ResponseEntity<Analysis> analyzeProject(
            @PathVariable String projectId) {

        try {

            Analysis analysis =
                    projectAnalyzerService.analyzeProject(projectId);

            return ResponseEntity.ok(analysis);

        } catch (Exception e) {

            return ResponseEntity.badRequest().build();
        }
    }
}