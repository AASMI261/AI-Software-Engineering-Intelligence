package com.aasmi.backend.service;

import com.aasmi.backend.model.Project;
import com.aasmi.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> getProjectById(String id) {
        return projectRepository.findById(id);
    }

    public Project updateProject(String id, Project updatedProject) {

        Optional<Project> existingProject = projectRepository.findById(id);

        if (existingProject.isEmpty()) {
            return null;
        }

        Project project = existingProject.get();

        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());
        project.setTechnologies(updatedProject.getTechnologies());
        project.setUpdatedAt(java.time.LocalDateTime.now());

        return projectRepository.save(project);
    }

    public boolean deleteProject(String id) {

        if (!projectRepository.existsById(id)) {
            return false;
        }

        projectRepository.deleteById(id);
        return true;
    }
}