package com.aasmi.backend.service;

import com.aasmi.backend.model.AiAnalysis;
import com.aasmi.backend.model.Finding;
import com.aasmi.backend.repository.AiAnalysisRepository;
import com.aasmi.backend.repository.FindingRepository;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiAnalysisService {

    private final ChatClient chatClient;
    private final FindingRepository findingRepository;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final CodeContextService codeContextService;

    public AiAnalysisService(
            ChatClient.Builder chatClientBuilder,
            FindingRepository findingRepository,
            AiAnalysisRepository aiAnalysisRepository,
            CodeContextService codeContextService) {

        this.chatClient = chatClientBuilder.build();
        this.findingRepository = findingRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.codeContextService = codeContextService;
    }

    public String askAI(String message) {

        return chatClient
                .prompt()
                .user(message)
                .call()
                .content();
    }

    public AiAnalysis analyzeFinding(
            String projectId,
            String findingId) {

        Finding finding = findingRepository
                .findById(findingId)
                .orElseThrow(() ->
                        new RuntimeException("Finding not found."));

        try {

            String codeContext =
                    codeContextService.getCodeContext(
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

            /*
             * Temporary parsing.
             * We will make this fully structured with
             * Spring AI structured output after testing.
             */

            String rootCause =
                    extractJsonValue(response, "rootCause");

            String explanation =
                    extractJsonValue(response, "explanation");

            String impact =
                    extractJsonValue(response, "impact");

            String recommendedFix =
                    extractJsonValue(response, "recommendedFix");

            String saferApproach =
                    extractJsonValue(response, "saferApproach");

            String confidence =
                    extractJsonValue(response, "confidence");

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

            throw new RuntimeException(
                    "AI analysis failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private String extractJsonValue(
            String json,
            String key) {

        String searchKey = "\"" + key + "\"";

        int keyIndex = json.indexOf(searchKey);

        if (keyIndex == -1) {
            return "Not available";
        }

        int colonIndex =
                json.indexOf(":", keyIndex);

        if (colonIndex == -1) {
            return "Not available";
        }

        int firstQuote =
                json.indexOf("\"", colonIndex + 1);

        if (firstQuote == -1) {
            return "Not available";
        }

        int secondQuote =
                json.indexOf("\"", firstQuote + 1);

        if (secondQuote == -1) {
            return "Not available";
        }

        return json.substring(
                firstQuote + 1,
                secondQuote
        );
    }
}