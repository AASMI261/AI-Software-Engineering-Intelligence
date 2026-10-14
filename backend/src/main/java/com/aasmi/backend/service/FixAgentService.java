package com.aasmi.backend.service;

import com.aasmi.backend.model.CodePatch;
import com.aasmi.backend.model.FixPlan;
import com.aasmi.backend.repository.FixPlanRepository;
import org.springframework.stereotype.Service;
import com.aasmi.backend.model.Analysis;
import com.aasmi.backend.model.Finding;
import com.aasmi.backend.repository.FindingRepository;
import java.util.List;
import com.aasmi.backend.service.ProjectAnalyzerService;
import com.aasmi.backend.service.StaticAnalysisService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                .orElseThrow(() ->
                        new RuntimeException("Fix plan not found."));

        // Only approved fixes can be applied
        if (!"APPROVED".equals(fixPlan.getStatus())) {
            throw new RuntimeException(
                    "Fix plan must be APPROVED before applying.");
        }

        Path backupFile = null;

        try {

            // --------------------------------------------------
            // 1. Locate project directory
            // --------------------------------------------------

            Path projectDirectory = Paths.get("uploads")
                    .toAbsolutePath()
                    .normalize()
                    .resolve(fixPlan.getProjectId())
                    .normalize();

            // --------------------------------------------------
            // 2. Locate target source file
            // --------------------------------------------------

            Path file = projectDirectory
                    .resolve(fixPlan.getFilePath())
                    .normalize();

            // Security check
            if (!file.startsWith(projectDirectory)) {
                throw new RuntimeException("Invalid file path.");
            }

            if (!Files.exists(file)) {
                throw new RuntimeException("Source file not found.");
            }

            // --------------------------------------------------
            // 3. Read original source
            // --------------------------------------------------

            String originalCode = Files.readString(file);

            // --------------------------------------------------
            // 4. Generate AI patch
            // --------------------------------------------------

            CodePatch patch = codePatchService.generatePatch(
                    fixPlan.getProjectId(),
                    fixPlan.getFindingId()
            );

            // --------------------------------------------------
            // 5. Validate patch target
            // --------------------------------------------------

            if (!fixPlan.getFilePath().equals(patch.getFilePath())) {
                throw new RuntimeException(
                        "AI patch targets a different file.");
            }

            if (patch.getOldCode() == null ||
                    patch.getOldCode().isBlank()) {

                throw new RuntimeException(
                        "AI patch did not provide valid old code.");
            }

            if (patch.getNewCode() == null ||
                    patch.getNewCode().isBlank()) {

                throw new RuntimeException(
                        "AI patch did not provide valid new code.");
            }

            // --------------------------------------------------
            // 6. Make backup
            // --------------------------------------------------

            backupFile = Paths.get(file.toString() + ".backup");

            Files.writeString(
                    backupFile,
                    originalCode
            );

            System.out.println(
                    "Backup created: " + backupFile);

            // --------------------------------------------------
            // 7. Apply exact code replacement
            // --------------------------------------------------

            if (!originalCode.contains(patch.getOldCode())) {

                throw new RuntimeException(
                        "Original code from AI patch was not found.");
            }

            String updatedCode = originalCode.replaceFirst(
                    Pattern.quote(patch.getOldCode()),
                    Matcher.quoteReplacement(patch.getNewCode())
            );

            if (originalCode.equals(updatedCode)) {

                throw new RuntimeException(
                        "Source code was not changed.");
            }

            Files.writeString(file, updatedCode);

            System.out.println(
                    "AI patch applied successfully.");

            // --------------------------------------------------
            // 8. Verify source modification
            // --------------------------------------------------

            String modifiedCode = Files.readString(file);

            if (modifiedCode.contains(patch.getOldCode())) {

                throw new RuntimeException(
                        "Old code still exists after patch.");
            }

            if (!modifiedCode.contains(patch.getNewCode())) {

                throw new RuntimeException(
                        "New code was not found after patch.");
            }

            // --------------------------------------------------
            // 9. Automated verification
            // --------------------------------------------------

            boolean verificationPassed =
                    verifyJavaFile(file);

            if (!verificationPassed) {

                System.out.println(
                        "Verification FAILED. Rolling back...");

                rollback(file, backupFile);

                fixPlan.setStatus("ROLLED_BACK");

                return fixPlanRepository.save(fixPlan);
            }

            // --------------------------------------------------
            // 10. Verification successful
            // --------------------------------------------------

            System.out.println(
                    "Verification PASSED.");

            fixPlan.setStatus("VERIFIED");

            FixPlan saved = fixPlanRepository.save(fixPlan);

            // Backup is no longer required after successful verification
      // Run a fresh project analysis after // --------------------------------------------------
// 10. Fresh analysis and verification
// --------------------------------------------------

Analysis freshAnalysis =
        projectAnalyzerService.analyzeProject(
                fixPlan.getProjectId()
        );

List<Finding> currentFindings =
        staticAnalysisService.analyzeJavaCode(
                fixPlan.getProjectId(),
                freshAnalysis.getId()
        );

// Check whether the original finding still exists.
Finding originalFinding =
        findingRepository.findById(
                fixPlan.getFindingId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Original finding not found."
                )
        );

String originalRule = originalFinding.getRule();
String originalFilePath = originalFinding.getFilePath();

boolean issueStillExists = currentFindings.stream()
        .anyMatch(finding ->
                originalRule.equals(finding.getRule())
                        && originalFilePath.equals(
                                finding.getFilePath()
                        )
        );

// If the original issue remains, restore the source.
if (issueStillExists) {

    rollback(file, backupFile);

    fixPlan.setStatus("ROLLED_BACK");

    FixPlan rolledBackPlan =
            fixPlanRepository.save(fixPlan);

    // Re-analyze the restored source.
    Analysis restoredAnalysis =
            projectAnalyzerService.analyzeProject(
                    fixPlan.getProjectId()
            );

    staticAnalysisService.analyzeJavaCode(
            fixPlan.getProjectId(),
            restoredAnalysis.getId()
    );

    Files.deleteIfExists(backupFile);

    return rolledBackPlan;
}

// The original issue is no longer detected.
fixPlan.setStatus("VERIFIED");

FixPlan savedPlan =
        fixPlanRepository.save(fixPlan);

// Remove backup after successful verification.
Files.deleteIfExists(backupFile);

return savedPlan;
        } catch (Exception e) {

            System.err.println(
                    "Fix application failed: "
                            + e.getMessage());

            // --------------------------------------------------
            // Automatic rollback on unexpected failure
            // --------------------------------------------------

            if (backupFile != null) {

                try {

                    Path projectDirectory = Paths.get("uploads")
                            .toAbsolutePath()
                            .normalize()
                            .resolve(fixPlan.getProjectId())
                            .normalize();

                    Path file = projectDirectory
                            .resolve(fixPlan.getFilePath())
                            .normalize();

                    if (Files.exists(backupFile)) {

                        Files.copy(
                                backupFile,
                                file,
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING
                        );

                        fixPlan.setStatus("ROLLED_BACK");

                        fixPlanRepository.save(fixPlan);

                        System.out.println(
                                "Rollback completed.");
                    }

                } catch (Exception rollbackError) {

                    System.err.println(
                            "Rollback failed: "
                                    + rollbackError.getMessage());
                }
            }

            throw new RuntimeException(
                    "Fix application failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // ==========================================================
    // Java Verification
    // ==========================================================

    private boolean verifyJavaFile(Path javaFile)
            throws IOException, InterruptedException {

        System.out.println(
                "Running Java compilation verification...");

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        "javac",
                        javaFile.toString()
                );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        String output = new String(
                process.getInputStream().readAllBytes()
        );

        boolean finished = process.waitFor(
                60,
                TimeUnit.SECONDS
        );

        if (!finished) {

            process.destroyForcibly();

            System.err.println(
                    "Compilation timed out.");

            return false;
        }

        System.out.println(
                "Compiler output:");

        System.out.println(output);

        if (process.exitValue() == 0) {

            System.out.println(
                    "Java compilation PASSED.");

            return true;
        }

        System.err.println(
                "Java compilation FAILED.");

        return false;
    }

    // ==========================================================
    // Rollback
    // ==========================================================

    private void rollback(
            Path file,
            Path backupFile) throws IOException {

        if (!Files.exists(backupFile)) {

            throw new IOException(
                    "Backup file not found. Cannot rollback.");
        }

        Files.copy(
                backupFile,
                file,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
        );

        System.out.println(
                "Original source restored successfully.");
    }
}