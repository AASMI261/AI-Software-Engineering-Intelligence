package com.aasmi.backend.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class CodeContextService {

    public String getCodeContext(
            String projectId,
            String filePath,
            int lineNumber) throws IOException {

        Path projectDirectory = Paths.get("uploads")
                .toAbsolutePath()
                .normalize()
                .resolve(projectId)
                .normalize();

        Path file = projectDirectory
                .resolve(filePath)
                .normalize();

        // Security check
        if (!file.startsWith(projectDirectory)) {
            throw new IOException("Invalid file path.");
        }
System.out.println("Project directory: " + projectDirectory);
System.out.println("Resolved source file: " + file);
System.out.println("File exists: " + Files.exists(file));

if (!Files.exists(file)) {
    throw new IOException("Source file not found: " + file);
}

        List<String> lines = Files.readAllLines(file);

        int contextBefore = 3;
        int contextAfter = 3;

        int start = Math.max(0, lineNumber - 1 - contextBefore);
        int end = Math.min(
                lines.size(),
                lineNumber + contextAfter
        );

        StringBuilder context = new StringBuilder();

        for (int i = start; i < end; i++) {

            context.append(i + 1)
                    .append(": ")
                    .append(lines.get(i))
                    .append("\n");
        }

        return context.toString();
    }
}