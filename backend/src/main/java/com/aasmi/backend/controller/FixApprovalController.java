package com.aasmi.backend.controller;

import com.aasmi.backend.model.FixPlan;
import com.aasmi.backend.repository.FixPlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fix-plans")
public class FixApprovalController {

    private final FixPlanRepository fixPlanRepository;

    public FixApprovalController(FixPlanRepository fixPlanRepository) {
        this.fixPlanRepository = fixPlanRepository;
    }

    @PostMapping("/{fixPlanId}/approve")
    public ResponseEntity<?> approveFixPlan(@PathVariable String fixPlanId) {

        FixPlan fixPlan = fixPlanRepository.findById(fixPlanId).orElse(null);

        if (fixPlan == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Fix plan not found.");
        }

        if (!"PENDING".equalsIgnoreCase(fixPlan.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Only PENDING fix plans can be approved.");
        }

        fixPlan.setStatus("APPROVED");
        FixPlan saved = fixPlanRepository.save(fixPlan);

        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{fixPlanId}/reject")
    public ResponseEntity<?> rejectFixPlan(@PathVariable String fixPlanId) {

        FixPlan fixPlan = fixPlanRepository.findById(fixPlanId).orElse(null);

        if (fixPlan == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Fix plan not found.");
        }

        if (!"PENDING".equalsIgnoreCase(fixPlan.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Only PENDING fix plans can be rejected.");
        }

        fixPlan.setStatus("REJECTED");
        FixPlan saved = fixPlanRepository.save(fixPlan);

        return ResponseEntity.ok(saved);
    }
}