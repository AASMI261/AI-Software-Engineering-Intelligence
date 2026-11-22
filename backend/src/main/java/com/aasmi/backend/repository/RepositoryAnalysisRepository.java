
package com.aasmi.backend.repository;

import com.aasmi.backend.model.RepositoryAnalysis;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RepositoryAnalysisRepository
        extends MongoRepository<RepositoryAnalysis, String> {

    List<RepositoryAnalysis> findByProjectIdOrderByCreatedAtDesc(
            String projectId
    );

    List<RepositoryAnalysis> findByRepositoryUrlOrderByCreatedAtDesc(
            String repositoryUrl
    );
}
