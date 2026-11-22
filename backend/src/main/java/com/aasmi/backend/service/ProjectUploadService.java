package com.aasmi.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProjectUploadService {

    private static final int MAX_ENTRIES = 2000;
    private static final long MAX_SINGLE_ENTRY_BYTES = 10L * 1024 * 1024; // 10 MB per file
    private static final long MAX_TOTAL_EXTRACTED_BYTES = 100L * 1024 * 1024; // 100 MB total

    private final Path uploadDirectory =
            Paths.get("uploads").toAbsolutePath().normalize();

    public String uploadProject(String projectId, MultipartFile file) throws IOException {

        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("Project ID is required.");
        }

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

        if (!projectDirectory.startsWith(uploadDirectory) || projectDirectory.equals(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid project directory path.");
        }

        Files.createDirectories(projectDirectory);

        try {
            extractZip(file, projectDirectory);
        } catch (IOException | RuntimeException ex) {
            deleteQuietly(projectDirectory);
            throw ex;
        }

        return "Project uploaded and extracted successfully.";
    }

    private void extractZip(MultipartFile file, Path targetDirectory)
            throws IOException {

        Path normalizedTargetDir = targetDirectory.toAbsolutePath().normalize();
        int entryCount = 0;
        long totalExtractedBytes = 0L;

        try (InputStream inputStream = file.getInputStream();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {

                entryCount++;
                if (entryCount > MAX_ENTRIES) {
                    throw new IOException("ZIP archive exceeds maximum allowed entries (" + MAX_ENTRIES + ").");
                }

                String entryName = entry.getName();
                if (entryName == null || entryName.isBlank()) {
                    throw new IOException("ZIP contains an invalid empty entry name.");
                }

                Path targetPath = normalizedTargetDir
                        .resolve(entryName)
                        .normalize();

                // Security check: prevents ZIP Slip / path traversal
                if (!targetPath.startsWith(normalizedTargetDir)) {
                    throw new IOException("Invalid ZIP entry path detected: " + entryName);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Path parent = targetPath.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }

                    long entryBytes = copyEntryWithLimit(zipInputStream, targetPath);
                    totalExtractedBytes += entryBytes;

                    if (totalExtractedBytes > MAX_TOTAL_EXTRACTED_BYTES) {
                        throw new IOException("ZIP archive exceeds maximum extracted size limit.");
                    }
                }

                zipInputStream.closeEntry();
            }
        }
    }

    private long copyEntryWithLimit(ZipInputStream zipInputStream, Path targetPath) throws IOException {
        byte[] buffer = new byte[8192];
        long written = 0L;

        try (var outputStream = Files.newOutputStream(
                targetPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            int bytesRead;
            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                written += bytesRead;
                if (written > MAX_SINGLE_ENTRY_BYTES) {
                    throw new IOException("Single file inside ZIP exceeds size limit: " + targetPath.getFileName());
                }
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return written;
    }

    private void deleteQuietly(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }
        try (var walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Best-effort cleanup on failed extraction
                        }
                    });
        } catch (IOException ignored) {
            // Best-effort cleanup
        }
    }
}