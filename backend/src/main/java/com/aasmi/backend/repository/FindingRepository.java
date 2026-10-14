package com.aasmi.backend.repository;

import com.aasmi.backend.model.Finding;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FindingRepository extends MongoRepository<Finding, String> {

    List<Finding> findByProjectId(String projectId);

    List<Finding> findByAnalysisId(String analysisId);

    List<Finding> findByProjectIdAndStatus(
            String projectId,
            String status
    );

    List<Finding> findByProjectIdAndRuleAndFilePath(
            String projectId,
            String rule,
            String filePath
    );
}