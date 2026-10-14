package com.aasmi.backend.controller;

import com.aasmi.backend.model.Finding;
import com.aasmi.backend.service.StaticAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class StaticAnalysisController {

    private final StaticAnalysisService staticAnalysisService;

    public StaticAnalysisController(
            StaticAnalysisService staticAnalysisService) {

        this.staticAnalysisService = staticAnalysisService;
    }

    @PostMapping("/{projectId}/analyses/{analysisId}/static-analysis")
    public ResponseEntity<List<Finding>> runStaticAnalysis(
            @PathVariable String projectId,
            @PathVariable String analysisId) {

        try {

            List<Finding> findings =
                    staticAnalysisService.analyzeJavaCode(
                            projectId,
                            analysisId
                    );

            return ResponseEntity.ok(findings);

        } catch (Exception e) {

            return ResponseEntity.badRequest().build();
        }
    }
}