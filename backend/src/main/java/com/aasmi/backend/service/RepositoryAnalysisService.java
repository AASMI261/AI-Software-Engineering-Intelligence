
package com.aasmi.backend.service;

import com.aasmi.backend.model.Finding;
import com.aasmi.backend.model.RepositoryAnalysis;
import com.aasmi.backend.repository.FindingRepository;
import com.aasmi.backend.repository.RepositoryAnalysisRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RepositoryAnalysisService {

    private final RepositoryAnalysisRepository analysisRepository;
    private final RepositoryAnalyzer repositoryAnalyzer;
    private final RepositoryCodeFetcher repositoryCodeFetcher;
    private final FindingRepository findingRepository;

    public RepositoryAnalysisService(
            RepositoryAnalysisRepository analysisRepository,
            RepositoryAnalyzer repositoryAnalyzer,
            RepositoryCodeFetcher repositoryCodeFetcher,
            FindingRepository findingRepository) {

        this.analysisRepository = analysisRepository;
        this.repositoryAnalyzer = repositoryAnalyzer;
        this.repositoryCodeFetcher = repositoryCodeFetcher;
        this.findingRepository = findingRepository;
    }

    public RepositoryAnalysis createAnalysis(
            String projectId,
            String repositoryUrl) {

        String validatedUrl =
                repositoryAnalyzer.validateRepositoryUrl(repositoryUrl);

        String repositoryName =
                validatedUrl.substring(validatedUrl.lastIndexOf('/') + 1);

        RepositoryAnalysis analysis = new RepositoryAnalysis(
                projectId,
                validatedUrl,
                repositoryName
        );

        analysis.setStatus("RUNNING");
        analysis.setMessage("Retrieving repository source files.");

        analysis = analysisRepository.save(analysis);

        try {
            List<Map<String, String>> sourceFiles =
                    repositoryCodeFetcher.fetchSourceFiles(validatedUrl);

            if (sourceFiles.isEmpty()) {
                analysis.setStatus("COMPLETED");
                analysis.setFilesAnalyzed(0);
                analysis.setFindingsCount(0);
                analysis.setMessage(
                        "No supported source files were found in this repository."
                );
                analysis.setCompletedAt(java.time.LocalDateTime.now());
                return analysisRepository.save(analysis);
            }

            List<Finding> findings = new ArrayList<>();

            for (Map<String, String> sourceFile : sourceFiles) {
                String filePath = sourceFile.get("path");
                String content = sourceFile.get("content");

                if (filePath == null || content == null) {
                    continue;
                }

                findings.addAll(
                        analyzeSourceFile(
                                projectId,
                                analysis.getId(),
                                filePath,
                                content
                        )
                );
            }

            if (!findings.isEmpty()) {
                findingRepository.saveAll(findings);
            }

            analysis.setFilesAnalyzed(sourceFiles.size());
            analysis.setFindingsCount(findings.size());
            analysis.setStatus("COMPLETED");
            analysis.setMessage(
                    "Static scan completed. Review the findings; detected patterns may require manual verification."
            );
            analysis.setCompletedAt(java.time.LocalDateTime.now());

            return analysisRepository.save(analysis);

        } catch (Exception e) {
            analysis.setStatus("FAILED");
            analysis.setMessage(
                    "Repository analysis failed: " + safeErrorMessage(e)
            );
            analysis.setCompletedAt(java.time.LocalDateTime.now());

            analysisRepository.save(analysis);
            return analysis;
        }
    }

    private List<Finding> analyzeSourceFile(
            String projectId,
            String analysisId,
            String filePath,
            String content) {

        List<Finding> findings = new ArrayList<>();
        String[] lines = content.split("\\R", -1);

        String extension = getExtension(filePath);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase(Locale.ROOT);
            int lineNumber = i + 1;

            if (trimmed.isEmpty()) {
                continue;
            }

            if (lower.contains("todo") || lower.contains("fixme")) {
                findings.add(new Finding(
                        projectId,
                        analysisId,
                        "CODE_QUALITY",
                        "LOW",
                        filePath,
                        lineNumber,
                        "TODO/FIXME marker found. Verify whether this unfinished work is still required.",
                        "TODO_MARKER",
                        "Review the marker and implement the required change or remove the marker."
                ));
            }

            if (lower.contains("console.log(")
                    || lower.contains("system.out.println(")
                    || lower.contains("print(")) {

                findings.add(new Finding(
                        projectId,
                        analysisId,
                        "CODE_QUALITY",
                        "LOW",
                        filePath,
                        lineNumber,
                        "A console or print statement was detected. Check whether it should be replaced by structured logging.",
                        "CONSOLE_OUTPUT",
                        "Use the application's logging framework where appropriate."
                ));
            }

            if (Pattern.compile(
                    "(?i)(password|api[_-]?key|secret|access[_-]?token)\\s*[:=]\\s*['\"][^'\"]{4,}['\"]"
            ).matcher(line).find()) {

                findings.add(new Finding(
                        projectId,
                        analysisId,
                        "SECURITY",
                        "HIGH",
                        filePath,
                        lineNumber,
                        "A possible hardcoded credential or secret was detected. This is a pattern match and may be a false positive.",
                        "POSSIBLE_HARDCODED_SECRET",
                        "Verify the value. If it is a real secret, remove it from source control and rotate it. Use environment variables or a secrets manager."
                ));
            }

            if ((extension.equals(".java")
                    || extension.equals(".js")
                    || extension.equals(".jsx")
                    || extension.equals(".ts")
                    || extension.equals(".tsx")
                    || extension.equals(".php"))
                    && lower.contains("== null")) {

                findings.add(new Finding(
                        projectId,
                        analysisId,
                        "CODE_QUALITY",
                        "LOW",
                        filePath,
                        lineNumber,
                        "A null comparison was detected. Verify whether the comparison is appropriate for this language and variable type.",
                        "NULL_COMPARISON",
                        "Review the null-check and use the idiomatic comparison for the language."
                ));
            }

            if (lower.contains("eval(")) {
                findings.add(new Finding(
                        projectId,
                        analysisId,
                        "SECURITY",
                        "MEDIUM",
                        filePath,
                        lineNumber,
                        "A call containing eval( was detected. Review whether untrusted input can reach dynamic evaluation.",
                        "DYNAMIC_EVALUATION",
                        "Avoid evaluating untrusted strings as code. Prefer a safer parser or explicit logic."
                ));
            }
        }

        return findings;
    }

    private String getExtension(String filePath) {
        int dot = filePath.lastIndexOf('.');
        int slash = filePath.lastIndexOf('/');

        if (dot <= slash) {
            return "";
        }

        return filePath.substring(dot).toLowerCase(Locale.ROOT);
    }

    private String safeErrorMessage(Exception e) {
        String message = e.getMessage();

        if (message == null || message.isBlank()) {
            return "An unexpected error occurred.";
        }

        return message.length() > 300
                ? message.substring(0, 300)
                : message;
    }

    public List<RepositoryAnalysis> getProjectAnalyses(String projectId) {
        return analysisRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public RepositoryAnalysis getAnalysisById(String analysisId) {
        return analysisRepository.findById(analysisId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Repository analysis not found."
                        ));
    }
}
