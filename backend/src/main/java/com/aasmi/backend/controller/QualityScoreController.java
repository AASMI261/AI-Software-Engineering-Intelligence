package com.aasmi.backend.controller;

import com.aasmi.backend.service.QualityScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class QualityScoreController {

    private final QualityScoreService qualityScoreService;

    public QualityScoreController(
            QualityScoreService qualityScoreService) {
        this.qualityScoreService = qualityScoreService;
    }

    @GetMapping("/{projectId}/quality-score")
    public ResponseEntity<Map<String, Object>> getQualityScore(
            @PathVariable String projectId) {

        Map<String, Object> score =
                qualityScoreService.calculateQualityScore(projectId);

        return ResponseEntity.ok(score);
    }
}