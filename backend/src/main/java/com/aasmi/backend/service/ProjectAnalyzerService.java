package com.aasmi.backend.service;

import com.aasmi.backend.model.Analysis;
import com.aasmi.backend.repository.AnalysisRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectAnalyzerService {

    private static final Set<String> IGNORED_DIRS = Set.of(
            ".git", "node_modules", "target", "build", ".idea", ".vscode"
    );

    private final AnalysisRepository analysisRepository;

    public ProjectAnalyzerService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    public Analysis analyzeProject(String projectId) throws IOException {

        Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
        Path projectDirectory = uploadRoot.resolve(projectId).normalize();

        if (!projectDirectory.startsWith(uploadRoot) || projectDirectory.equals(uploadRoot)) {
            throw new IOException("Invalid project directory path.");
        }

        if (!Files.exists(projectDirectory)) {
            throw new IOException("Project files not found.");
        }

        Set<String> detectedTechnologies = new LinkedHashSet<>();

        if (containsFileExtension(projectDirectory, ".java")) {
            detectedTechnologies.add("Java");
        }

        if (Files.exists(projectDirectory.resolve("pom.xml"))) {
            detectedTechnologies.add("Maven");
        }

        if (Files.exists(projectDirectory.resolve("build.gradle"))
                || Files.exists(projectDirectory.resolve("build.gradle.kts"))) {
            detectedTechnologies.add("Gradle");
        }

        if (Files.exists(projectDirectory.resolve("package.json"))) {
            detectedTechnologies.add("Node.js");
        }

        if (containsFileExtension(projectDirectory, ".jsx")
                || containsFileExtension(projectDirectory, ".tsx")) {
            detectedTechnologies.add("React");
        }

        if (Files.exists(projectDirectory.resolve("pom.xml"))
                && containsTextInFiles(projectDirectory, "spring-boot")) {
            detectedTechnologies.add("Spring Boot");
        }

        if (Files.exists(projectDirectory.resolve("Dockerfile"))) {
            detectedTechnologies.add("Docker");
        }

        Analysis analysis = new Analysis(
                projectId,
                new ArrayList<>(detectedTechnologies)
        );

        return analysisRepository.save(analysis);
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

    private boolean containsFileExtension(Path directory, String extension) throws IOException {
        try (var paths = Files.walk(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !isIgnoredPath(directory, path))
                    .anyMatch(path -> path.toString().toLowerCase().endsWith(extension.toLowerCase()));
        }
    }

    private boolean containsTextInFiles(Path directory, String text) throws IOException {
        try (var paths = Files.walk(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !isIgnoredPath(directory, path))
                    .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                    .anyMatch(path -> {
                        try {
                            String content = Files.readString(path);
                            return content.toLowerCase().contains(text.toLowerCase());
                        } catch (IOException e) {
                            return false;
                        }
                    });
        }
    }
}