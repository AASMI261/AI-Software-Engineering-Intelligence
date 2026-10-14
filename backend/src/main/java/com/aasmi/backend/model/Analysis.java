package com.aasmi.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "analyses")
public class Analysis {

    @Id
    private String id;

    private String projectId;

    private List<String> detectedTechnologies;

    private LocalDateTime analyzedAt;

    public Analysis() {
    }

    public Analysis(String projectId, List<String> detectedTechnologies) {
        this.projectId = projectId;
        this.detectedTechnologies = detectedTechnologies;
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

    public List<String> getDetectedTechnologies() {
        return detectedTechnologies;
    }

    public void setDetectedTechnologies(List<String> detectedTechnologies) {
        this.detectedTechnologies = detectedTechnologies;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}