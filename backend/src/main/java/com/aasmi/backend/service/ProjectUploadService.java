package com.aasmi.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProjectUploadService {

    private final Path uploadDirectory =
            Paths.get("uploads").toAbsolutePath().normalize();

    public String uploadProject(String projectId, MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Project file is empty.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Only ZIP files are allowed.");
        }

        Path projectDirectory = uploadDirectory
                .resolve(projectId)
                .normalize();

        Files.createDirectories(projectDirectory);

        extractZip(file, projectDirectory);

        return "Project uploaded and extracted successfully.";
    }

    private void extractZip(MultipartFile file, Path targetDirectory)
            throws IOException {

        targetDirectory = targetDirectory.toAbsolutePath().normalize();

        try (InputStream inputStream = file.getInputStream();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {

                Path targetPath = targetDirectory
                        .resolve(entry.getName())
                        .normalize();

                // Security check: prevents ZIP Slip / path traversal
                if (!targetPath.startsWith(targetDirectory)) {
                    throw new IOException("Invalid ZIP entry detected.");
                }

                if (entry.isDirectory()) {

                    Files.createDirectories(targetPath);

                } else {

                    Path parent = targetPath.getParent();

                    if (parent != null) {
                        Files.createDirectories(parent);
                    }

                    Files.copy(
                            zipInputStream,
                            targetPath,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }

                zipInputStream.closeEntry();
            }
        }
    }
}