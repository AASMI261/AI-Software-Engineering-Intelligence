package com.aasmi.backend.controller;

import com.aasmi.backend.model.AiAnalysis;
import com.aasmi.backend.service.AiAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    public AiAnalysisController(
            AiAnalysisService aiAnalysisService) {

        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testAI(
            @RequestParam String message) {

        try {

            String response =
                    aiAnalysisService.askAI(message);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body("AI request failed: "
                            + e.getMessage());
        }
    }

    @PostMapping(
            "/projects/{projectId}/findings/{findingId}"
    )
    public ResponseEntity<AiAnalysis> analyzeFinding(
            @PathVariable String projectId,
            @PathVariable String findingId) {

        try {

            AiAnalysis result =
                    aiAnalysisService.analyzeFinding(
                            projectId,
                            findingId
                    );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            return ResponseEntity.badRequest().build();
        }
    }
}