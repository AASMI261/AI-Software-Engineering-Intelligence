package com.aasmi.backend.controller;

import com.aasmi.backend.model.FixPlan;
import com.aasmi.backend.service.FixPlanService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class FixPlanController {

    private final FixPlanService fixPlanService;

    public FixPlanController(FixPlanService fixPlanService) {
        this.fixPlanService = fixPlanService;
    }

    @PostMapping("/projects/{projectId}/findings/{findingId}/fix-plan")
    public ResponseEntity<?> generateFixPlan(
            @PathVariable String projectId,
            @PathVariable String findingId) {

        try {

            FixPlan fixPlan =
                    fixPlanService.generateFixPlan(
                            projectId,
                            findingId
                    );

            return ResponseEntity.ok(fixPlan);

        } catch (Exception e) {

            System.err.println(
                    "========== FIX PLAN ERROR =========="
            );

            e.printStackTrace();

            return ResponseEntity.badRequest()
                    .body(
                            "Fix plan failed: "
                                    + e.getMessage()
                    );
        }
    }
}