package com.aasmi.backend.service;

import com.aasmi.backend.model.CodePatch;
import com.aasmi.backend.model.Finding;
import com.aasmi.backend.repository.FindingRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CodePatchService {

    private final ChatClient chatClient;
    private final FindingRepository findingRepository;
    private final CodeContextService codeContextService;

    public CodePatchService(
            ChatClient.Builder chatClientBuilder,
            FindingRepository findingRepository,
            CodeContextService codeContextService) {

        this.chatClient = chatClientBuilder.build();
        this.findingRepository = findingRepository;
        this.codeContextService = codeContextService;
    }

    public CodePatch generatePatch(
            String projectId,
            String findingId) {

        Finding finding =
                findingRepository.findById(findingId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Finding not found."
                                ));

        try {

            String codeContext =
                    codeContextService.getCodeContext(
                            projectId,
                            finding.getFilePath(),
                            finding.getLineNumber()
                    );

            String prompt = """
                    You are a senior Java software engineer responsible
                    for generating a SAFE, MINIMAL and COMPILABLE code patch.

                    IMPORTANT:
                    The patch will be applied automatically to the source file
                    only after human approval.

                    FINDING:
                    Type: %s
                    Severity: %s
                    File: %s
                    Line: %d
                    Message: %s
                    Rule: %s
                    Suggested Fix: %s

                    EXACT SOURCE CODE CONTEXT:
                    %s

                    Your task:
                    Generate ONE minimal patch that fixes the finding.

                    Return ONLY valid JSON in exactly this format:

                    {
                      "filePath": "%s",
                      "lineNumber": %d,
                      "oldCode": "exact complete existing code",
                      "newCode": "complete replacement code",
                      "explanation": "short explanation"
                    }

                    STRICT PATCH RULES:

                    1. oldCode MUST be copied EXACTLY from the provided
                       source code.

                    2. oldCode MUST contain the COMPLETE statement or
                       complete code fragment that will be replaced.

                    3. Do NOT return only a method name or partial fragment.

                    4. Example of INVALID patch:
                       oldCode = "System.out.println"
                       newCode = "logger.info"

                    5. Example of a better minimal patch:
                       oldCode = "System.out.println(\\"Hello\\");"
                       newCode = "logger.info(\\"Hello\\");"

                    6. newCode MUST be syntactically valid Java.

                    7. If the replacement requires an import, declaration,
                       or supporting code, include the required code in the
                       patch ONLY when necessary for compilation.

                    8. Do not invent files.

                    9. Do not invent unrelated classes.

                    10. Do not modify unrelated code.

                    11. Keep the patch as small as possible.

                    12. Preserve the original behavior unless the finding
                        specifically requires behavior to change.

                    13. Do not include markdown.

                    14. Do not include comments outside JSON.

                    15. Return JSON only.
                    """.formatted(
                    finding.getType(),
                    finding.getSeverity(),
                    finding.getFilePath(),
                    finding.getLineNumber(),
                    finding.getMessage(),
                    finding.getRule(),
                    finding.getSuggestedFix(),
                    codeContext,
                    finding.getFilePath(),
                    finding.getLineNumber()
            );

            String response =
                    chatClient.prompt()
                            .user(prompt)
                            .call()
                            .content();

            return parsePatch(response);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Patch generation failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private CodePatch parsePatch(String response) {

        if (response == null || response.isBlank()) {
            throw new RuntimeException(
                    "AI returned an empty response."
            );
        }

        String json = response.trim();

        System.out.println(
                "========== AI PATCH RESPONSE =========="
        );
        System.out.println(json);
        System.out.println(
                "======================================="
        );

        if (json.startsWith("```")) {

            int firstNewLine =
                    json.indexOf('\n');

            if (firstNewLine != -1) {
                json =
                        json.substring(
                                firstNewLine + 1
                        );
            }

            int lastFence =
                    json.lastIndexOf("```");

            if (lastFence != -1) {
                json =
                        json.substring(
                                0,
                                lastFence
                        );
            }

            json = json.trim();
        }

        int firstBrace =
                json.indexOf("{");

        int lastBrace =
                json.lastIndexOf("}");

        if (firstBrace == -1 ||
                lastBrace == -1) {

            throw new RuntimeException(
                    "AI did not return valid JSON."
            );
        }

        json =
                json.substring(
                        firstBrace,
                        lastBrace + 1
                );

        String filePath =
                extractJsonValue(
                        json,
                        "filePath"
                );

        String lineNumberValue =
                extractJsonValue(
                        json,
                        "lineNumber"
                );

        String oldCode =
                extractJsonValue(
                        json,
                        "oldCode"
                );

        String newCode =
                extractJsonValue(
                        json,
                        "newCode"
                );

        String explanation =
                extractJsonValue(
                        json,
                        "explanation"
                );

        System.out.println(
                "filePath = " + filePath
        );

        System.out.println(
                "lineNumber = " + lineNumberValue
        );

        System.out.println(
                "oldCode = " + oldCode
        );

        System.out.println(
                "newCode = " + newCode
        );

        if (filePath == null ||
                oldCode == null ||
                newCode == null) {

            throw new RuntimeException(
                    "AI returned an incomplete code patch."
            );
        }

        if (oldCode.isBlank() ||
                newCode.isBlank()) {

            throw new RuntimeException(
                    "AI returned empty oldCode or newCode."
            );
        }

        int lineNumber = 1;

        if (lineNumberValue != null &&
                !lineNumberValue.isBlank()) {

            try {

                String cleanedLineNumber =
                        lineNumberValue
                                .replaceAll(
                                        "[^0-9]",
                                        ""
                                );

                if (!cleanedLineNumber.isEmpty()) {

                    lineNumber =
                            Integer.parseInt(
                                    cleanedLineNumber
                            );
                }

            } catch (Exception e) {

                System.out.println(
                        "Could not parse AI lineNumber. "
                                + "Using 1."
                );

                lineNumber = 1;
            }
        }

        return new CodePatch(
                filePath,
                lineNumber,
                oldCode,
                newCode,
                explanation
        );
    }

    private String extractJsonValue(
            String json,
            String key) {

        String searchKey =
                "\"" + key + "\"";

        int keyIndex =
                json.indexOf(searchKey);

        if (keyIndex == -1) {
            return null;
        }

        int colonIndex =
                json.indexOf(
                        ":",
                        keyIndex
                );

        if (colonIndex == -1) {
            return null;
        }

        int valueStart =
                colonIndex + 1;

        while (
                valueStart < json.length()
                        && Character.isWhitespace(
                        json.charAt(valueStart))
        ) {

            valueStart++;
        }

        if (valueStart >= json.length()) {
            return null;
        }

        if (json.charAt(valueStart) != '"') {

            int end = valueStart;

            while (
                    end < json.length()
                            && json.charAt(end) != ','
                            && json.charAt(end) != '}'
            ) {

                end++;
            }

            return json.substring(
                    valueStart,
                    end
            ).trim();
        }

        StringBuilder value =
                new StringBuilder();

        boolean escaped = false;

        for (
                int i = valueStart + 1;
                i < json.length();
                i++
        ) {

            char c = json.charAt(i);

            if (escaped) {

                if (c == 'n') {
                    value.append('\n');

                } else if (c == 'r') {
                    value.append('\r');

                } else if (c == 't') {
                    value.append('\t');

                } else if (c == '"') {
                    value.append('"');

                } else if (c == '\\') {
                    value.append('\\');

                } else {
                    value.append(c);
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

        return null;
    }
}