package com.aasmi.backend.controller;

import com.aasmi.backend.model.FixPlan;
import com.aasmi.backend.repository.FixPlanRepository;
import com.aasmi.backend.service.FixPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FixPlanController {

    private final FixPlanService fixPlanService;
    private final FixPlanRepository fixPlanRepository;

    public FixPlanController(
            FixPlanService fixPlanService,
            FixPlanRepository fixPlanRepository) {
        this.fixPlanService = fixPlanService;
        this.fixPlanRepository = fixPlanRepository;
    }

    // Generate a structured fix plan using AI + project/finding context
    @PostMapping({
        "/ai/projects/{projectId}/findings/{findingId}/fix-plan",
        "/projects/{projectId}/findings/{findingId}/fix-plan"
    })
    public ResponseEntity<?> generateFixPlan(
            @PathVariable String projectId,
            @PathVariable String findingId) {
        try {
            FixPlan fixPlan = fixPlanService.generateFixPlan(projectId, findingId);
            return ResponseEntity.ok(fixPlan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Not Found",
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Fix Plan Generation Failed",
                            "message", "Fix plan failed: " + e.getMessage()
                    ));
        }
    }

    // Get all fix plans for a project
    @GetMapping("/projects/{projectId}/fix-plans")
    public ResponseEntity<List<FixPlan>> getProjectFixPlans(
            @PathVariable String projectId) {
        List<FixPlan> fixPlans = fixPlanRepository.findByProjectId(projectId);
        return ResponseEntity.ok(fixPlans);
    }
}