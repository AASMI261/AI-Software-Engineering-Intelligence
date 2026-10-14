package com.aasmi.backend.repository;

import com.aasmi.backend.model.AiAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AiAnalysisRepository
        extends MongoRepository<AiAnalysis, String> {

    List<AiAnalysis> findByFindingId(String findingId);
}