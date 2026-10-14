package com.aasmi.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "fix_plans")
public class FixPlan {

    @Id
    private String id;

    private String projectId;
    private String findingId;
    private String aiAnalysisId;

    private String filePath;
    private Integer lineNumber;

    private String problem;
    private String changeDescription;
    private String proposedChanges;

    private String testsToRun;
    private String verificationSteps;
    private String risk;

    private String confidence;
    private String status;
    private LocalDateTime generatedAt;

    public FixPlan() {
    }

    public FixPlan(
            String projectId,
            String findingId,
            String aiAnalysisId,
            String filePath,
            Integer lineNumber,
            String problem,
            String changeDescription,
            String proposedChanges,
            String testsToRun,
            String verificationSteps,
            String risk,
            String confidence,
            String status) {

        this.projectId = projectId;
        this.findingId = findingId;
        this.aiAnalysisId = aiAnalysisId;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.problem = problem;
        this.changeDescription = changeDescription;
        this.proposedChanges = proposedChanges;
        this.testsToRun = testsToRun;
        this.verificationSteps = verificationSteps;
        this.risk = risk;
        this.confidence = confidence;
        this.status = status;
        this.generatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFindingId() {
        return findingId;
    }

    public void setFindingId(String findingId) {
        this.findingId = findingId;
    }

    public String getAiAnalysisId() {
        return aiAnalysisId;
    }

    public void setAiAnalysisId(String aiAnalysisId) {
        this.aiAnalysisId = aiAnalysisId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getChangeDescription() {
        return changeDescription;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }

    public String getProposedChanges() {
        return proposedChanges;
    }

    public void setProposedChanges(String proposedChanges) {
        this.proposedChanges = proposedChanges;
    }

    public String getTestsToRun() {
        return testsToRun;
    }

    public void setTestsToRun(String testsToRun) {
        this.testsToRun = testsToRun;
    }

    public String getVerificationSteps() {
        return verificationSteps;
    }

    public void setVerificationSteps(String verificationSteps) {
        this.verificationSteps = verificationSteps;
    }

    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}
}