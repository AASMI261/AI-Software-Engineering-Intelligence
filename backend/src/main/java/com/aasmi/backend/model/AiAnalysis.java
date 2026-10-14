package com.aasmi.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "ai_analyses")
public class AiAnalysis {

    @Id
    private String id;

    private String projectId;
    private String findingId;

    private String rootCause;
    private String explanation;
    private String impact;
    private String recommendedFix;
    private String saferApproach;
    private String confidence;

    private LocalDateTime analyzedAt;

    public AiAnalysis() {
    }

    public AiAnalysis(
            String projectId,
            String findingId,
            String rootCause,
            String explanation,
            String impact,
            String recommendedFix,
            String saferApproach,
            String confidence) {

        this.projectId = projectId;
        this.findingId = findingId;
        this.rootCause = rootCause;
        this.explanation = explanation;
        this.impact = impact;
        this.recommendedFix = recommendedFix;
        this.saferApproach = saferApproach;
        this.confidence = confidence;
        this.analyzedAt = LocalDateTime.now();
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

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getRecommendedFix() {
        return recommendedFix;
    }

    public void setRecommendedFix(String recommendedFix) {
        this.recommendedFix = recommendedFix;
    }

    public String getSaferApproach() {
        return saferApproach;
    }

    public void setSaferApproach(String saferApproach) {
        this.saferApproach = saferApproach;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}