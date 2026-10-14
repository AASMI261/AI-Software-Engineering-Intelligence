package com.aasmi.backend.service;

import com.aasmi.backend.model.Analysis;
import com.aasmi.backend.repository.AnalysisRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectAnalyzerService {

    private final AnalysisRepository analysisRepository;

    public ProjectAnalyzerService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    public Analysis analyzeProject(String projectId) throws IOException {

        Path projectDirectory = Paths.get("uploads")
                .toAbsolutePath()
                .normalize()
                .resolve(projectId)
                .normalize();

        if (!Files.exists(projectDirectory)) {
            throw new IOException("Project files not found.");
        }

        List<String> detectedTechnologies = new ArrayList<>();

        // Detect Java
        if (containsFileExtension(projectDirectory, ".java")) {
            detectedTechnologies.add("Java");
        }

        // Detect Maven
        if (Files.exists(projectDirectory.resolve("pom.xml"))) {
            detectedTechnologies.add("Maven");
        }

        // Detect Gradle
        if (Files.exists(projectDirectory.resolve("build.gradle"))
                || Files.exists(projectDirectory.resolve("build.gradle.kts"))) {
            detectedTechnologies.add("Gradle");
        }

        // Detect Node.js
        if (Files.exists(projectDirectory.resolve("package.json"))) {
            detectedTechnologies.add("Node.js");
        }

        // Detect React
        if (containsFileExtension(projectDirectory, ".jsx")
                || containsFileExtension(projectDirectory, ".tsx")) {
            detectedTechnologies.add("React");
        }

        // Detect Spring Boot
        if (Files.exists(projectDirectory.resolve("pom.xml"))
                && containsTextInFiles(projectDirectory, "spring-boot")) {
            detectedTechnologies.add("Spring Boot");
        }

        // Detect Docker
        if (Files.exists(projectDirectory.resolve("Dockerfile"))) {
            detectedTechnologies.add("Docker");
        }

        // Create analysis object
        Analysis analysis = new Analysis(
                projectId,
                detectedTechnologies
        );

        // Save analysis in MongoDB
        return analysisRepository.save(analysis);
    }

    private boolean containsFileExtension(
            Path directory,
            String extension) throws IOException {

        try (var paths = Files.walk(directory)) {

            return paths
                    .filter(Files::isRegularFile)
                    .anyMatch(path ->
                            path.toString()
                                    .toLowerCase()
                                    .endsWith(extension.toLowerCase())
                    );
        }
    }

    private boolean containsTextInFiles(
            Path directory,
            String text) throws IOException {

        try (var paths = Files.walk(directory)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString().endsWith(".xml")
                    )
                    .anyMatch(path -> {

                        try {
                            String content = Files.readString(path);

                            return content
                                    .toLowerCase()
                                    .contains(text.toLowerCase());

                        } catch (IOException e) {
                            return false;
                        }
                    });
        }
    }
}