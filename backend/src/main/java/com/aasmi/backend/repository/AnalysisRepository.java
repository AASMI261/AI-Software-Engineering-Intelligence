package com.aasmi.backend.repository;

import com.aasmi.backend.model.Analysis;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AnalysisRepository
        extends MongoRepository<Analysis, String> {

    List<Analysis> findByProjectId(String projectId);
}