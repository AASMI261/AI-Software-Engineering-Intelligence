package com.aasmi.backend.service;

import com.aasmi.backend.model.Finding;
import com.aasmi.backend.repository.FindingRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StaticAnalysisService {

    private final FindingRepository findingRepository;

    public StaticAnalysisService(FindingRepository findingRepository) {
        this.findingRepository = findingRepository;
    }

    public List<Finding> analyzeJavaCode(
            String projectId,
            String analysisId) throws IOException {

        Path projectDirectory = Paths.get("uploads")
                .toAbsolutePath()
                .normalize()
                .resolve(projectId)
                .normalize();

        if (!Files.exists(projectDirectory)) {
            throw new IOException("Project files not found.");
        }

        List<Finding> detectedFindings = new ArrayList<>();

        try (var paths = Files.walk(projectDirectory)) {

            paths.filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString()
                                    .toLowerCase()
                                    .endsWith(".java"))
                    .forEach(path -> {

                        try {
                            analyzeFile(
                                    projectId,
                                    analysisId,
                                    projectDirectory,
                                    path,
                                    detectedFindings
                            );
                        } catch (IOException e) {
                            // Ignore files that cannot be read
                        }
                    });
        }

        /*
         * Reconcile current scan with existing findings.
         *
         * Existing issue -> update it
         * New issue -> create it
         * Old issue not detected anymore -> mark RESOLVED
         */
        List<Finding> finalFindings =
                reconcileFindings(
                        projectId,
                        analysisId,
                        detectedFindings
                );

        return finalFindings;
    }

    private void analyzeFile(
            String projectId,
            String analysisId,
            Path projectDirectory,
            Path file,
            List<Finding> findings) throws IOException {

        List<String> lines = Files.readAllLines(file);

        String relativePath =
                projectDirectory
                        .relativize(file)
                        .toString()
                        .replace("\\", "/");

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i).trim();

            int lineNumber = i + 1;

            // Rule 1: System.out.println
            if (line.contains("System.out.println")) {

                findings.add(new Finding(
                        projectId,
                        analysisId,
                        "CODE_QUALITY",
                        "LOW",
                        relativePath,
                        lineNumber,
                        "System.out.println detected in source code.",
                        "JAVA-001",
                        "Use a proper logging framework instead of System.out.println."
                ));
            }

            // Rule 2: TODO
            if (line.contains("TODO")) {

                findings.add(new Finding(
                        projectId,
                        analysisId,
                        "MAINTAINABILITY",
                        "LOW",
                        relativePath,
                        lineNumber,
                        "TODO comment detected.",
                        "JAVA-002",
                        "Resolve the TODO or create a tracked development task."
                ));
            }

            // Rule 3: Empty catch block
            if (line.equals("catch (Exception e) {")) {

                if (i + 1 < lines.size()) {

                    String nextLine = lines.get(i + 1).trim();

                    if (nextLine.equals("}")) {

                        findings.add(new Finding(
                                projectId,
                                analysisId,
                                "ERROR_HANDLING",
                                "MEDIUM",
                                relativePath,
                                lineNumber,
                                "Empty catch block detected.",
                                "JAVA-003",
                                "Handle the exception properly or log the error."
                        ));
                    }
                }
            }

            // Rule 4: Hard-coded password
            String lowerLine = line.toLowerCase();

            if (lowerLine.contains("password")
                    && lowerLine.contains("=")
                    && lowerLine.contains("\"")) {

                findings.add(new Finding(
                        projectId,
                        analysisId,
                        "SECURITY",
                        "HIGH",
                        relativePath,
                        lineNumber,
                        "Possible hard-coded password detected.",
                        "SECURITY-001",
                        "Do not store passwords directly in source code. Use environment variables or a secure secret manager."
                ));
            }

            // Rule 5: Possible hard-coded API key / secret
            if ((lowerLine.contains("apikey")
                    || lowerLine.contains("api_key")
                    || lowerLine.contains("secret"))
                    && lowerLine.contains("=")
                    && lowerLine.contains("\"")) {

                findings.add(new Finding(
                        projectId,
                        analysisId,
                        "SECURITY",
                        "HIGH",
                        relativePath,
                        lineNumber,
                        "Possible hard-coded API key or secret detected.",
                        "SECURITY-002",
                        "Move secrets to environment variables or a secure secret manager."
                ));
            }

            // Rule 6: Very long line
            if (line.length() > 120) {

                findings.add(new Finding(
                        projectId,
                        analysisId,
                        "CODE_QUALITY",
                        "LOW",
                        relativePath,
                        lineNumber,
                        "Very long line detected.",
                        "JAVA-004",
                        "Break the line into smaller, readable parts."
                ));
            }

            // Rule 7: Deeply nested code
            int openingBraces = countCharacter(line, '{');
            int closingBraces = countCharacter(line, '}');

            if (openingBraces - closingBraces >= 4) {

                findings.add(new Finding(
                        projectId,
                        analysisId,
                        "MAINTAINABILITY",
                        "MEDIUM",
                        relativePath,
                        lineNumber,
                        "Potentially deep code nesting detected.",
                        "JAVA-005",
                        "Reduce nesting by extracting logic into smaller methods."
                ));
            }
        }
    }

    private List<Finding> reconcileFindings(
            String projectId,
            String analysisId,
            List<Finding> detectedFindings) {

        List<Finding> existingFindings =
                findingRepository.findByProjectId(projectId);

        Set<String> currentFindingKeys = new HashSet<>();

        List<Finding> finalFindings = new ArrayList<>();

        for (Finding detected : detectedFindings) {

            String key = buildFindingKey(
                    detected.getRule(),
                    detected.getFilePath()
            );

            currentFindingKeys.add(key);

            List<Finding> matchingFindings =
                    findingRepository.findByProjectIdAndRuleAndFilePath(
                            projectId,
                            detected.getRule(),
                            detected.getFilePath()
                    );

            Finding finding;

            if (!matchingFindings.isEmpty()) {

                /*
                 * Reuse the existing finding instead of
                 * creating another duplicate.
                 */
                finding = matchingFindings.get(
                        matchingFindings.size() - 1
                );

                finding.setAnalysisId(analysisId);
                finding.setType(detected.getType());
                finding.setSeverity(detected.getSeverity());
                finding.setLineNumber(detected.getLineNumber());
                finding.setMessage(detected.getMessage());
                finding.setSuggestedFix(detected.getSuggestedFix());

            } else {

                /*
                 * Completely new finding.
                 */
                finding = detected;
            }

            finding.setStatus("OPEN");
            finding.setResolvedAt(null);
            finding.setLastVerifiedAt(LocalDateTime.now());

            Finding saved = findingRepository.save(finding);

            finalFindings.add(saved);
        }

        /*
         * Check previous findings.
         *
         * If an old finding is not present in the current
         * analysis, mark it as RESOLVED.
         */
        for (Finding existing : existingFindings) {

            String key = buildFindingKey(
                    existing.getRule(),
                    existing.getFilePath()
            );

            if (!currentFindingKeys.contains(key)
                    && !"RESOLVED".equals(existing.getStatus())) {

                existing.setStatus("RESOLVED");
                existing.setResolvedAt(LocalDateTime.now());
                existing.setLastVerifiedAt(LocalDateTime.now());

                findingRepository.save(existing);
            }
        }

        return finalFindings;
    }

    private String buildFindingKey(
            String rule,
            String filePath) {

        return rule + "::" + filePath;
    }

    private int countCharacter(
            String text,
            char character) {

        int count = 0;

        for (char current : text.toCharArray()) {

            if (current == character) {
                count++;
            }
        }

        return count;
    }
}