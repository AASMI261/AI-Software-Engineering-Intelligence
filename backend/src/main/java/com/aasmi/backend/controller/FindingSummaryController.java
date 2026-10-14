package com.aasmi.backend.controller;

import com.aasmi.backend.service.FindingSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class FindingSummaryController {

    private final FindingSummaryService findingSummaryService;

    public FindingSummaryController(
            FindingSummaryService findingSummaryService) {
        this.findingSummaryService = findingSummaryService;
    }

    @GetMapping("/{projectId}/findings/summary")
    public ResponseEntity<Map<String, Object>> getFindingSummary(
            @PathVariable String projectId) {

        Map<String, Object> summary =
                findingSummaryService.getSummary(projectId);

        return ResponseEntity.ok(summary);
    }
}