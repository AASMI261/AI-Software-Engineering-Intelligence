package com.aasmi.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "findings")
public class Finding {

    @Id
    private String id;

    private String projectId;
    private String analysisId;

    private String type;
    private String severity;

    private String filePath;
    private Integer lineNumber;

    private String message;
    private String rule;

    private String suggestedFix;

    private String status;

    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime lastVerifiedAt;

    public Finding() {
    }

    public Finding(
            String projectId,
            String analysisId,
            String type,
            String severity,
            String filePath,
            Integer lineNumber,
            String message,
            String rule,
            String suggestedFix) {

        this.projectId = projectId;
        this.analysisId = analysisId;
        this.type = type;
        this.severity = severity;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.message = message;
        this.rule = rule;
        this.suggestedFix = suggestedFix;

        this.status = "OPEN";
        this.detectedAt = LocalDateTime.now();
        this.lastVerifiedAt = LocalDateTime.now();
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

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getSuggestedFix() {
        return suggestedFix;
    }

    public void setSuggestedFix(String suggestedFix) {
        this.suggestedFix = suggestedFix;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getLastVerifiedAt() {
        return lastVerifiedAt;
    }

    public void setLastVerifiedAt(LocalDateTime lastVerifiedAt) {
        this.lastVerifiedAt = lastVerifiedAt;
    }
}