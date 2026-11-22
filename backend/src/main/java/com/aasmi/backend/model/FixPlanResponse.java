package com.aasmi.backend.model;

import java.util.List;

public record FixPlanResponse(
        String findingId,
        String problemSummary,
        String rootCause,
        List<String> proposedChanges,
        List<String> implementationSteps,
        List<String> potentialRisks,
        List<String> verificationSteps,
        String confidence
) {
}