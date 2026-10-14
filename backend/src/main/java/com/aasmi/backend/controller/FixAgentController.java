package com.aasmi.backend.controller;

import com.aasmi.backend.model.FixPlan;
import com.aasmi.backend.service.FixAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fix-agent")
public class FixAgentController {

    private final FixAgentService fixAgentService;

    public FixAgentController(FixAgentService fixAgentService) {
        this.fixAgentService = fixAgentService;
    }

    @PostMapping("/{fixPlanId}/apply")
    public ResponseEntity<?> applyFix(
            @PathVariable String fixPlanId) {

        try {

            FixPlan result =
                    fixAgentService.applyFix(fixPlanId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body("Fix application failed: "
                            + e.getMessage());
        }
    }
}