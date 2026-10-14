package com.aasmi.backend.service;

import com.aasmi.backend.model.Finding;
import com.aasmi.backend.repository.FindingRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QualityScoreService {

    private final FindingRepository findingRepository;

    public QualityScoreService(FindingRepository findingRepository) {
        this.findingRepository = findingRepository;
    }

    public Map<String, Object> calculateQualityScore(String projectId) {

        List<Finding> findings =
                findingRepository.findByProjectId(projectId);

        int totalFindings = findings.size();
        int openFindings = 0;
        int resolvedFindings = 0;

        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;

        int deductions = 0;

        for (Finding finding : findings) {

            String status = finding.getStatus();
            String severity = finding.getSeverity();

            if ("RESOLVED".equalsIgnoreCase(status)) {
                resolvedFindings++;
                continue;
            }

            if (!"OPEN".equalsIgnoreCase(status)) {
                continue;
            }

            openFindings++;

            if ("CRITICAL".equalsIgnoreCase(severity)) {
                criticalCount++;
                deductions += 15;

            } else if ("HIGH".equalsIgnoreCase(severity)) {
                highCount++;
                deductions += 10;

            } else if ("MEDIUM".equalsIgnoreCase(severity)) {
                mediumCount++;
                deductions += 5;

            } else if ("LOW".equalsIgnoreCase(severity)) {
                lowCount++;
                deductions += 2;
            }
        }

        int qualityScore = Math.max(0, 100 - deductions);

        String rating;

        if (qualityScore >= 90) {
            rating = "EXCELLENT";
        } else if (qualityScore >= 75) {
            rating = "GOOD";
        } else if (qualityScore >= 50) {
            rating = "NEEDS_IMPROVEMENT";
        } else {
            rating = "POOR";
        }

        Map<String, Object> severityCounts = new HashMap<>();
        severityCounts.put("critical", criticalCount);
        severityCounts.put("high", highCount);
        severityCounts.put("medium", mediumCount);
        severityCounts.put("low", lowCount);

        Map<String, Object> result = new HashMap<>();

        result.put("projectId", projectId);
        result.put("qualityScore", qualityScore);
        result.put("rating", rating);
        result.put("totalFindings", totalFindings);
        result.put("openFindings", openFindings);
        result.put("resolvedFindings", resolvedFindings);
        result.put("severityCounts", severityCounts);

        return result;
    }
}