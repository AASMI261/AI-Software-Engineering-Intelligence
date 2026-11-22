package com.aasmi.backend.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class CodeContextService {

    private static final long MAX_FILE_SIZE = 1_048_576; // 1 MB

    public String getCodeContext(
            String projectId,
            String filePath,
            int lineNumber) throws IOException {

        if (projectId == null || projectId.isBlank()) {
            throw new IOException("Invalid project ID.");
        }

        if (filePath == null || filePath.isBlank()) {
            throw new IOException("Invalid file path.");
        }

        if (lineNumber < 1) {
            throw new IOException("Invalid line number.");
        }

        Path projectDirectory = Paths.get("uploads")
                .toAbsolutePath()
                .normalize()
                .resolve(projectId)
                .normalize();

        if (!Files.isDirectory(projectDirectory)) {
            throw new IOException("Project directory not found.");
        }

        Path realProjectDirectory = projectDirectory.toRealPath();

        Path requestedPath = Paths.get(filePath);

        if (requestedPath.isAbsolute()) {
            throw new IOException("Absolute file paths are not allowed.");
        }

        Path file = realProjectDirectory
                .resolve(requestedPath)
                .normalize();

        if (!file.startsWith(realProjectDirectory)) {
            throw new IOException("Access outside project directory denied.");
        }

        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Invalid source file.");
        }

        Path realFile = file.toRealPath();

        if (!realFile.startsWith(realProjectDirectory)) {
            throw new IOException("Symbolic link access denied.");
        }

        if (Files.size(realFile) > MAX_FILE_SIZE) {
            throw new IOException("Source file is too large.");
        }

        List<String> lines = Files.readAllLines(realFile);

        if (lineNumber > lines.size()) {
            throw new IOException("Line number exceeds file length.");
        }

        int contextBefore = 3;
        int contextAfter = 3;

        int start = Math.max(0, lineNumber - 1 - contextBefore);
        int end = Math.min(lines.size(), lineNumber + contextAfter);

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