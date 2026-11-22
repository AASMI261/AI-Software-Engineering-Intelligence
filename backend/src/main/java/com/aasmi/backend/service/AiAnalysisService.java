package com.aasmi.backend.service;

import com.aasmi.backend.model.AiAnalysis;
import com.aasmi.backend.model.Finding;
import com.aasmi.backend.repository.AiAnalysisRepository;
import com.aasmi.backend.repository.FindingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class AiAnalysisService {

    private static final Set<String> VALID_CONFIDENCE = Set.of("HIGH", "MEDIUM", "LOW");

    private final ChatClient chatClient;
    private final FindingRepository findingRepository;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final CodeContextService codeContextService;
    private final ObjectMapper objectMapper;

    public AiAnalysisService(
            ChatClient.Builder chatClientBuilder,
            FindingRepository findingRepository,
            AiAnalysisRepository aiAnalysisRepository,
            CodeContextService codeContextService,
            ObjectMapper objectMapper) {

        this.chatClient = chatClientBuilder.build();
        this.findingRepository = findingRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.codeContextService = codeContextService;
        this.objectMapper = objectMapper;
    }

    public String askAI(String message) {
        return chatClient
                .prompt()
                .user(message)
                .call()
                .content();
    }

    public AiAnalysis analyzeFinding(String projectId, String findingId) {

        Finding finding = findingRepository
                .findById(findingId)
                .orElseThrow(() -> new RuntimeException("Finding not found."));

        if (projectId != null && !projectId.isBlank()
                && finding.getProjectId() != null
                && !projectId.equals(finding.getProjectId())) {
            throw new RuntimeException("Finding does not belong to this project.");
        }

        try {
            System.out.println("Requested projectId: " + projectId);
            System.out.println("Finding projectId: " + finding.getProjectId());
            System.out.println("Finding filePath: " + finding.getFilePath());
            System.out.println("Finding lineNumber: " + finding.getLineNumber());

            String codeContext = codeContextService.getCodeContext(
                    projectId,
                    finding.getFilePath(),
                    finding.getLineNumber()
            );

            String prompt = """
                    You are an AI Software Engineering Analysis Agent.

                    Analyze the following software engineering finding.

                    FINDING
                    Type: %s
                    Severity: %s
                    File: %s
                    Line: %s
                    Rule: %s
                    Message: %s
                    Existing Suggested Fix: %s

                    CODE CONTEXT
                    %s

                    Return ONLY valid JSON.

                    Use exactly this structure:

                    {
                      "rootCause": "Explain the probable root cause.",
                      "explanation": "Explain the issue technically.",
                      "impact": "Explain the impact of this issue.",
                      "recommendedFix": "Give a practical recommended fix.",
                      "saferApproach": "Explain a safer implementation approach.",
                      "confidence": "HIGH"
                    }

                    The confidence value must be exactly one of:
                    HIGH
                    MEDIUM
                    LOW

                    Rules:
                    - Do not use Markdown.
                    - Do not use ```json.
                    - Do not add text before or after the JSON.
                    - Do not invent information.
                    - Base the analysis only on the finding and code context.
                    """.formatted(
                    finding.getType(),
                    finding.getSeverity(),
                    finding.getFilePath(),
                    finding.getLineNumber(),
                    finding.getRule(),
                    finding.getMessage(),
                    finding.getSuggestedFix(),
                    codeContext
            );

            String response = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                throw new IllegalStateException("Ollama returned an empty AI analysis response.");
            }

            JsonNode root = parseJsonObject(response);

            String rootCause = getTextOrDefault(root, "rootCause", "Not available");
            String explanation = getTextOrDefault(root, "explanation", "Not available");
            String impact = getTextOrDefault(root, "impact", "Not available");
            String recommendedFix = getTextOrDefault(root, "recommendedFix", "Not available");
            String saferApproach = getTextOrDefault(root, "saferApproach", "Not available");
            String confidence = normalizeConfidence(getTextOrDefault(root, "confidence", "MEDIUM"));

            AiAnalysis analysis = new AiAnalysis(
                    projectId,
                    findingId,
                    rootCause,
                    explanation,
                    impact,
                    recommendedFix,
                    saferApproach,
                    confidence
            );

            return aiAnalysisRepository.save(analysis);

        } catch (Exception e) {
            throw new RuntimeException("AI analysis failed: " + e.getMessage(), e);
        }
    }

    private JsonNode parseJsonObject(String raw) throws Exception {
        String cleaned = stripMarkdownFences(raw);
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start != -1 && end != -1 && end >= start) {
            cleaned = cleaned.substring(start, end + 1);
        }
        JsonNode root = objectMapper.readTree(cleaned);
        if (root == null || !root.isObject()) {
            throw new IllegalStateException("AI analysis response is not a valid JSON object.");
        }
        return root;
    }

    private String stripMarkdownFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            if (firstNewLine != -1) {
                trimmed = trimmed.substring(firstNewLine + 1);
            }
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence != -1) {
                trimmed = trimmed.substring(0, lastFence);
            }
        }
        return trimmed.trim();
    }

    private String getTextOrDefault(JsonNode root, String field, String fallback) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            return fallback;
        }
        String text = node.isTextual() ? node.asText() : node.toString();
        return text.isBlank() ? fallback : text.trim();
    }

    private String normalizeConfidence(String value) {
        String upper = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return VALID_CONFIDENCE.contains(upper) ? upper : "MEDIUM";
    }
}