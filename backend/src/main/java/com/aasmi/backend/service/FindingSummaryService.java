package com.aasmi.backend.service;

import com.aasmi.backend.model.Finding;
import com.aasmi.backend.repository.FindingRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FindingSummaryService {

    private final FindingRepository findingRepository;

    public FindingSummaryService(FindingRepository findingRepository) {
        this.findingRepository = findingRepository;
    }

    public Map<String, Object> getSummary(String projectId) {

        List<Finding> findings =
                findingRepository.findByProjectId(projectId);

        int total = findings.size();
        int open = 0;
        int resolved = 0;

        int critical = 0;
        int high = 0;
        int medium = 0;
        int low = 0;

        for (Finding finding : findings) {

            String status = finding.getStatus();
            String severity = finding.getSeverity();

            if ("RESOLVED".equalsIgnoreCase(status)) {
                resolved++;
            } else if ("OPEN".equalsIgnoreCase(status)) {
                open++;
            }

            if (!"OPEN".equalsIgnoreCase(status)) {
                continue;
            }

            if ("CRITICAL".equalsIgnoreCase(severity)) {
                critical++;
            } else if ("HIGH".equalsIgnoreCase(severity)) {
                high++;
            } else if ("MEDIUM".equalsIgnoreCase(severity)) {
                medium++;
            } else if ("LOW".equalsIgnoreCase(severity)) {
                low++;
            }
        }

        String projectStatus;

        if (open == 0) {
            projectStatus = "NO_OPEN_FINDINGS";
        } else if (critical > 0 || high > 0) {
            projectStatus = "NEEDS_URGENT_ATTENTION";
        } else {
            projectStatus = "ISSUES_FOUND";
        }

        Map<String, Object> severityCounts = new HashMap<>();
        severityCounts.put("critical", critical);
        severityCounts.put("high", high);
        severityCounts.put("medium", medium);
        severityCounts.put("low", low);

        Map<String, Object> summary = new HashMap<>();

        summary.put("projectId", projectId);
        summary.put("totalFindings", total);
        summary.put("openFindings", open);
        summary.put("resolvedFindings", resolved);
        summary.put("severityCounts", severityCounts);
        summary.put("projectStatus", projectStatus);

        return summary;
    }
}