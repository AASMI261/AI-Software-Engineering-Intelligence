package com.aasmi.backend.service;

import com.aasmi.backend.model.Analysis;
import com.aasmi.backend.model.CodePatch;
import com.aasmi.backend.model.Finding;
import com.aasmi.backend.model.FixPlan;
import com.aasmi.backend.repository.FindingRepository;
import com.aasmi.backend.repository.FixPlanRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class FixAgentService {

    private final FixPlanRepository fixPlanRepository;
    private final CodePatchService codePatchService;
    private final ProjectAnalyzerService projectAnalyzerService;
    private final StaticAnalysisService staticAnalysisService;
    private final FindingRepository findingRepository;

    public FixAgentService(
            FixPlanRepository fixPlanRepository,
            CodePatchService codePatchService,
            ProjectAnalyzerService projectAnalyzerService,
            StaticAnalysisService staticAnalysisService,
            FindingRepository findingRepository) {
        this.fixPlanRepository = fixPlanRepository;
        this.codePatchService = codePatchService;
        this.projectAnalyzerService = projectAnalyzerService;
        this.staticAnalysisService = staticAnalysisService;
        this.findingRepository = findingRepository;
    }

    public FixPlan applyFix(String fixPlanId) {

        FixPlan fixPlan = fixPlanRepository.findById(fixPlanId)
                .orElseThrow(() -> new RuntimeException("Fix plan not found."));

        // Only approved fixes can be applied
        if (!"APPROVED".equals(fixPlan.getStatus())) {
            throw new RuntimeException("Fix plan must be APPROVED before applying.");
        }

        Path backupFile = null;

        try {
            // 1. Locate project directory safely
            Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
            Path projectDirectory = uploadRoot
                    .resolve(fixPlan.getProjectId())
                    .normalize();

            if (!projectDirectory.startsWith(uploadRoot) || projectDirectory.equals(uploadRoot)) {
                throw new RuntimeException("Invalid project directory path.");
            }

            // 2. Locate target source file safely
            Path file = projectDirectory
                    .resolve(fixPlan.getFilePath())
                    .normalize();

            if (!file.startsWith(projectDirectory)) {
                throw new RuntimeException("Invalid file path.");
            }

            if (!Files.exists(file)) {
                throw new RuntimeException("Source file not found.");
            }

            // 3. Read original source
            String originalCode = Files.readString(file);

            // 4. Generate AI patch
            CodePatch patch = codePatchService.generatePatch(
                    fixPlan.getProjectId(),
                    fixPlan.getFindingId()
            );

            // 5. Validate patch target
            if (!fixPlan.getFilePath().equals(patch.getFilePath())) {
                throw new RuntimeException("AI patch targets a different file.");
            }

            if (patch.getOldCode() == null || patch.getOldCode().isBlank()) {
                throw new RuntimeException("AI patch did not provide valid old code.");
            }

            if (patch.getNewCode() == null || patch.getNewCode().isBlank()) {
                throw new RuntimeException("AI patch did not provide valid new code.");
            }

            // 6. Make a unique backup
            Path backupCandidate = Files.createTempFile(
                    file.getParent(),
                    "fix-agent-",
                    ".backup"
            );

            try {
                Files.writeString(backupCandidate, originalCode);
                backupFile = backupCandidate;
                System.out.println("Backup created: " + backupFile);
            } catch (IOException e) {
                Files.deleteIfExists(backupCandidate);
                throw e;
            }

            // 7. Apply exact code replacement (must match once)
            String oldCode = patch.getOldCode();
            int firstIndex = originalCode.indexOf(oldCode);

            if (firstIndex == -1) {
                throw new RuntimeException("Original code from AI patch was not found.");
            }

            int secondIndex = originalCode.indexOf(oldCode, firstIndex + oldCode.length());
            if (secondIndex != -1) {
                throw new RuntimeException("Unsafe patch: old code appears multiple times.");
            }

            String updatedCode = originalCode.replaceFirst(
                    Pattern.quote(oldCode),
                    Matcher.quoteReplacement(patch.getNewCode())
            );

            if (originalCode.equals(updatedCode)) {
                throw new RuntimeException("Source code was not changed.");
            }

            Files.writeString(file, updatedCode);
            System.out.println("AI patch applied successfully.");

            // 8. Verify source modification
            String modifiedCode = Files.readString(file);

            if (modifiedCode.contains(oldCode)) {
                throw new RuntimeException("Old code still exists after patch.");
            }

            if (!modifiedCode.contains(patch.getNewCode())) {
                throw new RuntimeException("New code was not found after patch.");
            }

            // 9. Compile check across project Java sources
            boolean compilationPassed = verifyJavaProject(projectDirectory, file);

            if (!compilationPassed) {
                System.out.println("Compilation verification FAILED. Rolling back...");
                rollback(file, backupFile);
                Files.deleteIfExists(backupFile);

                fixPlan.setStatus("ROLLED_BACK");
                return fixPlanRepository.save(fixPlan);
            }

            System.out.println("Compilation verification PASSED.");

            // 10. Fresh analysis and rule verification
            Analysis freshAnalysis = projectAnalyzerService.analyzeProject(
                    fixPlan.getProjectId()
            );

            List<Finding> currentFindings = staticAnalysisService.analyzeJavaCode(
                    fixPlan.getProjectId(),
                    freshAnalysis.getId()
            );

            Finding originalFinding = findingRepository.findById(fixPlan.getFindingId())
                    .orElseThrow(() -> new RuntimeException("Original finding not found."));

            String originalRule = originalFinding.getRule();
            String originalFilePath = originalFinding.getFilePath();

            boolean issueStillExists = currentFindings.stream()
                    .anyMatch(finding ->
                            originalRule.equals(finding.getRule())
                                    && originalFilePath.equals(finding.getFilePath())
                    );

            if (issueStillExists) {
                System.out.println("Original finding still present after fix. Rolling back...");
                rollback(file, backupFile);
                Files.deleteIfExists(backupFile);

                fixPlan.setStatus("ROLLED_BACK");
                FixPlan rolledBackPlan = fixPlanRepository.save(fixPlan);

                Analysis restoredAnalysis = projectAnalyzerService.analyzeProject(
                        fixPlan.getProjectId()
                );
                staticAnalysisService.analyzeJavaCode(
                        fixPlan.getProjectId(),
                        restoredAnalysis.getId()
                );

                return rolledBackPlan;
            }

            fixPlan.setStatus("VERIFIED");
            FixPlan savedPlan = fixPlanRepository.save(fixPlan);
            Files.deleteIfExists(backupFile);

            return savedPlan;

        } catch (Exception e) {
            System.err.println("Fix application failed: " + e.getMessage());

            if (backupFile != null) {
                try {
                    Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
                    Path projectDirectory = uploadRoot.resolve(fixPlan.getProjectId()).normalize();
                    Path file = projectDirectory.resolve(fixPlan.getFilePath()).normalize();

                    if (projectDirectory.startsWith(uploadRoot)
                            && file.startsWith(projectDirectory)
                            && Files.exists(backupFile)) {
                        rollback(file, backupFile);
                        Files.deleteIfExists(backupFile);

                        fixPlan.setStatus("ROLLED_BACK");
                        fixPlanRepository.save(fixPlan);
                        System.out.println("Rollback completed.");
                    }
                } catch (Exception rollbackError) {
                    System.err.println("Rollback failed: " + rollbackError.getMessage());
                }
            }

            throw new RuntimeException("Fix application failed: " + e.getMessage(), e);
        }
    }

    // ==========================================================
    // Java Project Verification
    // ==========================================================

    private boolean verifyJavaProject(Path projectDirectory, Path targetJavaFile) {

        Path tempDir = null;

        try {
            Path sourceRoot = resolveSourceRoot(projectDirectory, targetJavaFile);
            List<String> javaSourcePaths = collectJavaFiles(sourceRoot);

            if (javaSourcePaths.isEmpty()) {
                javaSourcePaths = List.of(targetJavaFile.toAbsolutePath().toString());
            }

            tempDir = Files.createTempDirectory("fix-agent-compile-");
            Path classesDir = Files.createDirectory(tempDir.resolve("classes"));
            Path outputFile = tempDir.resolve("compiler-output.txt");

            List<String> command = new ArrayList<>();
            command.add("javac");
            command.add("-d");
            command.add(classesDir.toString());
            command.add("-sourcepath");
            command.add(sourceRoot.toString());

            List<String> jarPaths = collectJarFiles(projectDirectory);
            if (!jarPaths.isEmpty()) {
                command.add("-cp");
                command.add(String.join(File.pathSeparator, jarPaths));
            }

            command.addAll(javaSourcePaths);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(outputFile.toFile());

            Process process = processBuilder.start();
            boolean completed = process.waitFor(60, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                process.waitFor();
                System.err.println("Java compilation timed out.");
                return false;
            }

            String compilerOutput = Files.exists(outputFile)
                    ? Files.readString(outputFile)
                    : "";

            if (!compilerOutput.isBlank()) {
                System.out.println("Compiler Output:\n" + compilerOutput);
            }

            boolean success = process.exitValue() == 0;
            System.out.println(
                    success
                            ? "Java compilation successful."
                            : "Java compilation failed."
            );

            return success;

        } catch (IOException e) {
            System.err.println("Compilation error: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Compilation interrupted.");
            return false;
        } finally {
            if (tempDir != null) {
                try (var paths = Files.walk(tempDir)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    System.err.println(
                                            "Temporary file cleanup failed: "
                                                    + e.getMessage()
                                    );
                                }
                            });
                } catch (IOException e) {
                    System.err.println(
                            "Temporary directory cleanup failed: "
                                    + e.getMessage()
                    );
                }
            }
        }
    }

    private Path resolveSourceRoot(Path projectDirectory, Path targetJavaFile) {
        Path standardSrcMainJava = projectDirectory.resolve("src/main/java").normalize();
        if (Files.isDirectory(standardSrcMainJava) && targetJavaFile.startsWith(standardSrcMainJava)) {
            return standardSrcMainJava;
        }
        return projectDirectory;
    }

    private List<String> collectJavaFiles(Path root) throws IOException {
        if (!Files.exists(root)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(root)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(p -> p.toAbsolutePath().toString())
                    .toList();
        }
    }

    private List<String> collectJarFiles(Path projectDirectory) throws IOException {
        try (Stream<Path> walk = Files.walk(projectDirectory)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".jar"))
                    .map(p -> p.toAbsolutePath().toString())
                    .toList();
        }
    }

    // ==========================================================
    // Rollback
    // ==========================================================

    private void rollback(Path file, Path backupFile) throws IOException {

        if (!Files.exists(backupFile)) {
            throw new IOException("Backup file not found. Cannot rollback.");
        }

        Files.copy(
                backupFile,
                file,
                StandardCopyOption.REPLACE_EXISTING
        );

        System.out.println("Original source restored successfully.");
    }
}