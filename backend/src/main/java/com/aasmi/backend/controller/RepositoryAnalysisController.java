
package com.aasmi.backend.controller;

import com.aasmi.backend.model.RepositoryAnalysis;
import com.aasmi.backend.service.RepositoryAnalysisService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repository-analyses")
public class RepositoryAnalysisController {

    private final RepositoryAnalysisService analysisService;

    public RepositoryAnalysisController(
            RepositoryAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping
    public ResponseEntity<?> createAnalysis(
            @RequestBody Map<String, String> request) {

        try {
            String projectId = request.get("projectId");
            String repositoryUrl = request.get("repositoryUrl");

            if (projectId == null || projectId.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Project ID is required.")
                );
            }

            RepositoryAnalysis analysis =
                    analysisService.createAnalysis(
                            projectId.trim(),
                            repositoryUrl
                    );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(analysis);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Could not create analysis request."
                    ));
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<RepositoryAnalysis>> getProjectAnalyses(
            @PathVariable String projectId) {

        return ResponseEntity.ok(
                analysisService.getProjectAnalyses(projectId)
        );
    }

    @GetMapping("/{analysisId}")
    public ResponseEntity<?> getAnalysisById(
            @PathVariable String analysisId) {

        try {
            return ResponseEntity.ok(
                    analysisService.getAnalysisById(analysisId)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
