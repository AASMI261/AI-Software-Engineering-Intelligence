package com.aasmi.backend.service;

import com.aasmi.backend.model.AiAnalysis;
import com.aasmi.backend.model.Finding;
import com.aasmi.backend.model.FixPlan;
import com.aasmi.backend.repository.AiAnalysisRepository;
import com.aasmi.backend.repository.FindingRepository;
import com.aasmi.backend.repository.FixPlanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class FixPlanService {

    private static final Set<String> VALID_LEVELS = Set.of("LOW", "MEDIUM", "HIGH");

    private final ChatClient chatClient;
    private final FindingRepository findingRepository;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final FixPlanRepository fixPlanRepository;
    private final ObjectMapper objectMapper;

    public FixPlanService(
            ChatClient.Builder chatClientBuilder,
            FindingRepository findingRepository,
            AiAnalysisRepository aiAnalysisRepository,
            FixPlanRepository fixPlanRepository,
            ObjectMapper objectMapper) {

        this.chatClient = chatClientBuilder.build();
        this.findingRepository = findingRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.fixPlanRepository = fixPlanRepository;
        this.objectMapper = objectMapper;
    }

    public FixPlan generateFixPlan(String projectId, String findingId) {

        Finding finding = findingRepository.findById(findingId)
                .orElseThrow(() -> new RuntimeException("Finding not found."));

        if (projectId != null && !projectId.isBlank()
                && finding.getProjectId() != null
                && !projectId.equals(finding.getProjectId())) {
            throw new RuntimeException("Finding does not belong to this project.");
        }

        List<AiAnalysis> aiAnalyses = aiAnalysisRepository.findByFindingId(findingId).stream()
                .filter(a -> projectId == null || projectId.isBlank()
                        || a.getProjectId() == null
                        || projectId.equals(a.getProjectId()))
                .toList();

        if (aiAnalyses.isEmpty()) {
            throw new RuntimeException("AI analysis not found. Run AI analysis first.");
        }

        AiAnalysis aiAnalysis = aiAnalyses.get(aiAnalyses.size() - 1);

        String prompt = """
                You are a senior software engineer creating a practical remediation plan.

                Project ID:
                %s

                Finding:
                Type: %s
                Severity: %s
                File: %s
                Line: %s
                Message: %s
                Rule: %s
                Suggested Fix: %s

                AI Root Cause:
                %s

                AI Explanation:
                %s

                AI Impact:
                %s

                AI Recommended Fix:
                %s

                Return ONLY a single valid JSON object. Every JSON string value must be on a single line (use \\n if a line break is needed, never raw newlines or unescaped quotes inside string values).

                Required JSON format:
                {
                  "problem": "Concise summary of the issue in the target file and line",
                  "changeDescription": "Clear engineering explanation of how to fix it safely",
                  "proposedChanges": "Specific code/config changes needed in the exact file without inventing classes or methods",
                  "testsToRun": "Concrete build/compile and test cases to run",
                  "verificationSteps": "Step-by-step checks to verify the fix resolves the finding without regressions",
                  "risk": "LOW",
                  "confidence": "HIGH"
                }

                Rules:
                - Do not modify source code directly.
                - Do not invent files, classes, or methods that do not exist in the context.
                - Keep changes minimal and production-safe.
                - risk must be LOW, MEDIUM, or HIGH.
                - confidence must be HIGH, MEDIUM, or LOW.
                - Output JSON only, no extra text before or after.
                """.formatted(
                projectId,
                finding.getType(),
                finding.getSeverity(),
                finding.getFilePath(),
                finding.getLineNumber(),
                finding.getMessage(),
                finding.getRule(),
                finding.getSuggestedFix(),
                aiAnalysis.getRootCause(),
                aiAnalysis.getExplanation(),
                aiAnalysis.getImpact(),
                aiAnalysis.getRecommendedFix()
        );

        String response = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("Ollama returned an empty Fix Plan response.");
        }

        System.out.println("========== FIX PLAN AI RESPONSE ==========");
        System.out.println(response);
        System.out.println("==========================================");

        try {
            JsonNode root = parseJsonObject(response);

            String problem = getTextOrDefault(root, "problem", "");
            String changeDescription = getTextOrDefault(root, "changeDescription", "");
            String proposedChanges = getTextOrDefault(root, "proposedChanges", "");
            String testsToRun = getTextOrDefault(root, "testsToRun", "");
            String verificationSteps = getTextOrDefault(root, "verificationSteps", "");
            String risk = normalizeLevel(getTextOrDefault(root, "risk", "MEDIUM"));
            String confidence = normalizeLevel(getTextOrDefault(root, "confidence", "MEDIUM"));

            FixPlan fixPlan = new FixPlan(
                    projectId,
                    findingId,
                    aiAnalysis.getId(),
                    finding.getFilePath(),
                    finding.getLineNumber(),
                    problem,
                    changeDescription,
                    proposedChanges,
                    testsToRun,
                    verificationSteps,
                    risk,
                    confidence,
                    "PENDING"
            );

            return fixPlanRepository.save(fixPlan);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Fix Plan response: " + e.getMessage(), e);
        }
    }

    private JsonNode parseJsonObject(String raw) throws Exception {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            int firstNewLine = cleaned.indexOf('\n');
            if (firstNewLine != -1) {
                cleaned = cleaned.substring(firstNewLine + 1);
            }
            int lastFence = cleaned.lastIndexOf("```");
            if (lastFence != -1) {
                cleaned = cleaned.substring(0, lastFence);
            }
        }
        cleaned = cleaned.trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start != -1 && end != -1 && end >= start) {
            cleaned = cleaned.substring(start, end + 1);
        }

        String sanitized = escapeControlCharsInJsonStrings(cleaned);
        JsonNode root = objectMapper.readTree(sanitized);
        if (root == null || !root.isObject()) {
            throw new IllegalStateException("Fix plan response is not a valid JSON object.");
        }
        return root;
    }

    private String escapeControlCharsInJsonStrings(String json) {
        StringBuilder sb = new StringBuilder(json.length() + 32);
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                sb.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                sb.append(c);
                if (inString) {
                    escaped = true;
                }
                continue;
            }

            if (c == '"') {
                inString = !inString;
                sb.append(c);
                continue;
            }

            if (inString) {
                if (c == '\n') {
                    sb.append("\\n");
                } else if (c == '\r') {
                    sb.append("\\r");
                } else if (c == '\t') {
                    sb.append("\\t");
                } else if (c < 0x20) {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private String getTextOrDefault(JsonNode root, String field, String fallback) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            return fallback;
        }
        String text = node.isTextual() ? node.asText() : node.toString();
        return text.isBlank() ? fallback : text.trim();
    }

    private String normalizeLevel(String value) {
        String upper = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return VALID_LEVELS.contains(upper) ? upper : "MEDIUM";
    }
}