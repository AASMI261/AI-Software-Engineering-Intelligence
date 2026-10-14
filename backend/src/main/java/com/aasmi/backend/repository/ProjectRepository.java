package com.aasmi.backend.repository;

import com.aasmi.backend.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectRepository extends MongoRepository<Project, String> {
}