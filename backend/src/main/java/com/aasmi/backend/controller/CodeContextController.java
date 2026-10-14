package com.aasmi.backend.controller;

import com.aasmi.backend.service.CodeContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class CodeContextController {

    private final CodeContextService codeContextService;

    public CodeContextController(
            CodeContextService codeContextService) {

        this.codeContextService = codeContextService;
    }

    @GetMapping("/{projectId}/code-context")
    public ResponseEntity<String> getCodeContext(
            @PathVariable String projectId,
            @RequestParam String filePath,
            @RequestParam int lineNumber) {

        try {

            String context =
                    codeContextService.getCodeContext(
                            projectId,
                            filePath,
                            lineNumber
                    );

            return ResponseEntity.ok(context);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body("Unable to read code context: "
                            + e.getMessage());
        }
    }
}