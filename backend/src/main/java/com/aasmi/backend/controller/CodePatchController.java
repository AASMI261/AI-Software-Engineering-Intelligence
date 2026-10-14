package com.aasmi.backend.controller;

import com.aasmi.backend.model.CodePatch;
import com.aasmi.backend.service.CodePatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code-patches")
public class CodePatchController {

    private final CodePatchService codePatchService;

    public CodePatchController(CodePatchService codePatchService) {
        this.codePatchService = codePatchService;
    }

    @PostMapping("/projects/{projectId}/findings/{findingId}/generate")
    public ResponseEntity<?> generatePatch(
            @PathVariable String projectId,
            @PathVariable String findingId) {

        try {

            CodePatch patch =
                    codePatchService.generatePatch(
                            projectId,
                            findingId
                    );

            return ResponseEntity.ok(patch);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(
                            "Patch generation failed: "
                                    + e.getMessage()
                    );
        }
    }
}