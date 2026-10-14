package com.aasmi.backend.controller;

import com.aasmi.backend.model.Finding;
import com.aasmi.backend.repository.FindingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class FindingController {

    private final FindingRepository findingRepository;

    public FindingController(FindingRepository findingRepository) {
        this.findingRepository = findingRepository;
    }

    @GetMapping("/{projectId}/findings")
    public ResponseEntity<List<Finding>> getProjectFindings(
            @PathVariable String projectId) {

        return ResponseEntity.ok(
                findingRepository.findByProjectId(projectId)
        );
    }

    @GetMapping("/{projectId}/findings/open")
    public ResponseEntity<List<Finding>> getOpenFindings(
            @PathVariable String projectId) {

        return ResponseEntity.ok(
                findingRepository.findByProjectIdAndStatus(
                        projectId,
                        "OPEN"
                )
        );
    }

    @GetMapping("/{projectId}/analyses/{analysisId}/findings")
    public ResponseEntity<List<Finding>> getAnalysisFindings(
            @PathVariable String projectId,
            @PathVariable String analysisId) {

        return ResponseEntity.ok(
                findingRepository.findByAnalysisId(analysisId)
        );
    }
}