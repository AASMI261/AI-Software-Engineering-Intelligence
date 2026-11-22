package com.aasmi.backend.service;
import org.springframework.stereotype.Service;

@Service
public class RepositoryAnalyzer {

    public String validateRepositoryUrl(String repositoryUrl) {

        if (repositoryUrl == null || repositoryUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Repository URL cannot be empty."
            );
        }

        String url = repositoryUrl.trim();

        if (!url.matches(
                "^https://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/?$")) {
            throw new IllegalArgumentException(
                    "Enter a valid public GitHub repository URL."
            );
        }

        return url.replaceAll("/+$", "");
    }
}