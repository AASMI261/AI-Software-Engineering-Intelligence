package com.aasmi.backend.model;

public class AiAnalysisResponse {

    private String rootCause;
    private String explanation;
    private String impact;
    private String recommendedFix;
    private String saferApproach;
    private String confidence;

    public AiAnalysisResponse() {
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
}