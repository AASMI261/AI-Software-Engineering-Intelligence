
package com.aasmi.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "repository_analyses")
public class RepositoryAnalysis {

    @Id
    private String id;

    private String projectId;
    private String repositoryUrl;
    private String repositoryName;
    private String status;
    private String message;

    private Integer filesAnalyzed;
    private Integer findingsCount;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public RepositoryAnalysis() {
    }

    public RepositoryAnalysis(
            String projectId,
            String repositoryUrl,
            String repositoryName) {

        this.projectId = projectId;
        this.repositoryUrl = repositoryUrl;
        this.repositoryName = repositoryName;
        this.status = "PENDING";
        this.message = "Repository analysis has been requested.";
        this.filesAnalyzed = 0;
        this.findingsCount = 0;
        this.createdAt = LocalDateTime.now();
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

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getFilesAnalyzed() {
        return filesAnalyzed;
    }

    public void setFilesAnalyzed(Integer filesAnalyzed) {
        this.filesAnalyzed = filesAnalyzed;
    }

    public Integer getFindingsCount() {
        return findingsCount;
    }

    public void setFindingsCount(Integer findingsCount) {
        this.findingsCount = findingsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
