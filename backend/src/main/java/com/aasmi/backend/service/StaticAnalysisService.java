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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StaticAnalysisService {

    private static final Set<String> IGNORED_DIRS = Set.of(
            ".git", "node_modules", "target", "build", ".idea", ".vscode"
    );

    private final FindingRepository findingRepository;

    public StaticAnalysisService(FindingRepository findingRepository) {
        this.findingRepository = findingRepository;
    }

    public List<Finding> analyzeJavaCode(String projectId, String analysisId) throws IOException {

        Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
        Path projectDirectory = uploadRoot.resolve(projectId).normalize();

        if (!projectDirectory.startsWith(uploadRoot) || projectDirectory.equals(uploadRoot)) {
            throw new IOException("Invalid project directory path.");
        }

        if (!Files.exists(projectDirectory)) {
            throw new IOException("Project files not found.");
        }

        List<Finding> detectedFindings = new ArrayList<>();

        try (var paths = Files.walk(projectDirectory)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> !isIgnoredPath(projectDirectory, path))
                    .filter(path -> path.toString().toLowerCase().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            analyzeFile(projectId, analysisId, projectDirectory, path, detectedFindings);
                        } catch (IOException e) {
                            // Ignore files that cannot be read
                        }
                    });
        }

        return reconcileFindings(projectId, analysisId, detectedFindings);
    }

    private boolean isIgnoredPath(Path root, Path candidate) {
        Path relative = root.relativize(candidate);
        for (Path part : relative) {
            if (IGNORED_DIRS.contains(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private void analyzeFile(
            String projectId,
            String analysisId,
            Path projectDirectory,
            Path file,
            List<Finding> findings) throws IOException {

        List<String> lines = Files.readAllLines(file);
        String relativePath = projectDirectory.relativize(file).toString().replace("\\", "/");

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
            if (line.startsWith("catch (") && line.endsWith("{") && i + 1 < lines.size()) {
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

        List<Finding> existingFindings = findingRepository.findByProjectId(projectId);

        Map<String, List<Finding>> poolByKey = new HashMap<>();
        for (Finding existing : existingFindings) {
            String key = buildFindingKey(existing.getRule(), existing.getFilePath());
            poolByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(existing);
        }

        Set<String> touchedIds = new HashSet<>();
        List<Finding> finalFindings = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Finding detected : detectedFindings) {
            String key = buildFindingKey(detected.getRule(), detected.getFilePath());
            List<Finding> candidates = poolByKey.getOrDefault(key, List.of());

            Finding target = null;
            for (Finding candidate : candidates) {
                if (candidate.getId() != null && !touchedIds.contains(candidate.getId())) {
                    target = candidate;
                    break;
                }
            }

            if (target != null) {
                target.setAnalysisId(analysisId);
                target.setType(detected.getType());
                target.setSeverity(detected.getSeverity());
                target.setLineNumber(detected.getLineNumber());
                target.setMessage(detected.getMessage());
                target.setSuggestedFix(detected.getSuggestedFix());
                target.setStatus("OPEN");
                target.setResolvedAt(null);
                target.setLastVerifiedAt(now);

                Finding saved = findingRepository.save(target);
                if (saved.getId() != null) {
                    touchedIds.add(saved.getId());
                }
                finalFindings.add(saved);
            } else {
                detected.setStatus("OPEN");
                detected.setResolvedAt(null);
                detected.setLastVerifiedAt(now);

                Finding saved = findingRepository.save(detected);
                if (saved.getId() != null) {
                    touchedIds.add(saved.getId());
                }
                finalFindings.add(saved);
            }
        }

        for (Finding existing : existingFindings) {
            boolean wasTouched = existing.getId() != null && touchedIds.contains(existing.getId());
            if (!wasTouched && !"RESOLVED".equals(existing.getStatus())) {
                existing.setStatus("RESOLVED");
                existing.setResolvedAt(now);
                existing.setLastVerifiedAt(now);
                findingRepository.save(existing);
            }
        }

        return finalFindings;
    }

    private String buildFindingKey(String rule, String filePath) {
        return rule + "::" + filePath;
    }

    private int countCharacter(String text, char character) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == character) {
                count++;
            }
        }
        return count;
    }
}