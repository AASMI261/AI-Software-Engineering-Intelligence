
package com.aasmi.backend.controller;

import com.aasmi.backend.service.RepositoryAnalyzer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {

    private final RepositoryAnalyzer repositoryAnalyzer;

    public RepositoryController(
            RepositoryAnalyzer repositoryAnalyzer) {
        this.repositoryAnalyzer = repositoryAnalyzer;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateRepository(
            @RequestBody Map<String, String> request) {

        try {
            String repositoryUrl =
                    repositoryAnalyzer.validateRepositoryUrl(
                            request.get("repositoryUrl")
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "valid", true,
                            "repositoryUrl", repositoryUrl,
                            "message",
                            "GitHub repository URL is valid."
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "valid", false,
                            "message", e.getMessage()
                    )
            );
        }
    }
}