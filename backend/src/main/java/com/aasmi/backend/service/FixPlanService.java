package com.aasmi.backend.service;

import com.aasmi.backend.model.AiAnalysis;
import com.aasmi.backend.model.Finding;
import com.aasmi.backend.model.FixPlan;
import com.aasmi.backend.repository.AiAnalysisRepository;
import com.aasmi.backend.repository.FindingRepository;
import com.aasmi.backend.repository.FixPlanRepository;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FixPlanService {

    private final ChatClient chatClient;
    private final FindingRepository findingRepository;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final FixPlanRepository fixPlanRepository;

    public FixPlanService(
            ChatClient.Builder chatClientBuilder,
            FindingRepository findingRepository,
            AiAnalysisRepository aiAnalysisRepository,
            FixPlanRepository fixPlanRepository) {

        this.chatClient = chatClientBuilder.build();
        this.findingRepository = findingRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.fixPlanRepository = fixPlanRepository;
    }

    public FixPlan generateFixPlan(String projectId, String findingId) {

        // 1. Finding find karo
        Finding finding = findingRepository.findById(findingId)
                .orElseThrow(() -> new RuntimeException("Finding not found."));

        // 2. AI analysis find karo
        List<AiAnalysis> aiAnalyses =
                aiAnalysisRepository.findByFindingId(findingId);

        if (aiAnalyses.isEmpty()) {
            throw new RuntimeException(
                    "AI analysis not found. Run AI analysis first."
            );
        }

        // Latest AI analysis
        AiAnalysis aiAnalysis =
                aiAnalyses.get(aiAnalyses.size() - 1);

        // 3. AI ko Fix Plan generate karne bolo
        String prompt = """
                You are an expert software engineering assistant.

                Create a safe and practical fix plan for the following software issue.

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

                Return ONLY valid JSON.

                Required JSON format:

                {
                  "problem": "...",
                  "changeDescription": "...",
                  "proposedChanges": "...",
                  "testsToRun": "...",
                  "verificationSteps": "...",
                  "risk": "LOW",
                  "confidence": "HIGH"
                }

                Rules:
                - Do not modify source code.
                - Only create a plan.
                - Do not invent files, classes or methods.
                - Keep the proposed change minimal.
                - risk must be LOW, MEDIUM or HIGH.
                - confidence must be HIGH, MEDIUM or LOW.
                - Return JSON only.
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

        System.out.println("========== FIX PLAN AI RESPONSE ==========");
        System.out.println(response);
        System.out.println("==========================================");

        // 4. JSON values extract karo
        String problem = extractJsonValue(response, "problem");
        String changeDescription =
                extractJsonValue(response, "changeDescription");
        String proposedChanges =
                extractJsonValue(response, "proposedChanges");
        String testsToRun =
                extractJsonValue(response, "testsToRun");
        String verificationSteps =
                extractJsonValue(response, "verificationSteps");
        String risk =
                extractJsonValue(response, "risk");
        String confidence =
                extractJsonValue(response, "confidence");

        // 5. FixPlan create karo
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

        // 6. MongoDB me save
        return fixPlanRepository.save(fixPlan);
    }

    private String extractJsonValue(String json, String key) {

        if (json == null || json.isBlank()) {
            return "";
        }

        String cleaned = json.trim();

        // Markdown code fence remove
        cleaned = cleaned
                .replace("```json", "")
                .replace("```", "")
                .trim();

        String searchKey = "\"" + key + "\"";

        int keyIndex = cleaned.indexOf(searchKey);

        if (keyIndex == -1) {
            return "";
        }

        int colonIndex = cleaned.indexOf(":", keyIndex);

        if (colonIndex == -1) {
            return "";
        }

        int valueStart = colonIndex + 1;

        while (
                valueStart < cleaned.length()
                        && Character.isWhitespace(cleaned.charAt(valueStart))
        ) {
            valueStart++;
        }

        // String value
        if (
                valueStart < cleaned.length()
                        && cleaned.charAt(valueStart) == '"'
        ) {

            StringBuilder value = new StringBuilder();

            boolean escaped = false;

            for (
                    int i = valueStart + 1;
                    i < cleaned.length();
                    i++
            ) {

                char c = cleaned.charAt(i);

                if (escaped) {

                    switch (c) {
                        case 'n' -> value.append('\n');
                        case 'r' -> value.append('\r');
                        case 't' -> value.append('\t');
                        case '"' -> value.append('"');
                        case '\\' -> value.append('\\');
                        default -> value.append(c);
                    }

                    escaped = false;

                } else if (c == '\\') {

                    escaped = true;

                } else if (c == '"') {

                    return value.toString();

                } else {

                    value.append(c);
                }
            }
        }

        // Non-string value
        int end = valueStart;

        while (
                end < cleaned.length()
                        && cleaned.charAt(end) != ','
                        && cleaned.charAt(end) != '}'
        ) {
            end++;
        }

        return cleaned
                .substring(valueStart, end)
                .trim();
    }
}