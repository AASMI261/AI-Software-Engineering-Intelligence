
package com.aasmi.backend.repository;

import com.aasmi.backend.model.FixPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FixPlanRepository extends MongoRepository<FixPlan, String> {

    Optional<FixPlan> findByFindingId(String findingId);

    List<FixPlan> findByProjectId(String projectId);
}
