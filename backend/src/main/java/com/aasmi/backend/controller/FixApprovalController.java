package com.aasmi.backend.controller;

import com.aasmi.backend.model.FixPlan;
import com.aasmi.backend.repository.FixPlanRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fix-plans")
public class FixApprovalController {

    private final FixPlanRepository fixPlanRepository;

    public FixApprovalController(
            FixPlanRepository fixPlanRepository) {

        this.fixPlanRepository = fixPlanRepository;
    }

    @PostMapping("/{fixPlanId}/approve")
    public ResponseEntity<?> approveFixPlan(
            @PathVariable String fixPlanId) {

        try {

            FixPlan fixPlan =
                    fixPlanRepository.findById(fixPlanId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Fix plan not found."
                                    ));

            fixPlan.setStatus("APPROVED");

            FixPlan saved =
                    fixPlanRepository.save(fixPlan);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body("Approval failed: " + e.getMessage());
        }
    }

    @PostMapping("/{fixPlanId}/reject")
    public ResponseEntity<?> rejectFixPlan(
            @PathVariable String fixPlanId) {

        try {

            FixPlan fixPlan =
                    fixPlanRepository.findById(fixPlanId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Fix plan not found."
                                    ));

            fixPlan.setStatus("REJECTED");

            FixPlan saved =
                    fixPlanRepository.save(fixPlan);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body("Rejection failed: " + e.getMessage());
        }
    }
}