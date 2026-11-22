
package com.aasmi.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Base64;

@Service
public class RepositoryCodeFetcher {

    private static final int MAX_FILES = 200;
    private static final int MAX_FILE_SIZE = 200_000;
    private static final int MAX_TOTAL_SIZE = 2_000_000;

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            ".java", ".js", ".jsx", ".ts", ".tsx",
            ".py", ".php", ".html", ".css",
            ".xml", ".json", ".yml", ".yaml",
            ".properties", ".sql"
    );

    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            "node_modules", "vendor", "dist", "build",
            "target", ".git", ".next", "coverage"
    );

   private final ObjectMapper objectMapper;
private final HttpClient httpClient;

public RepositoryCodeFetcher() {
    this.objectMapper = new ObjectMapper();

    this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
}

    public List<Map<String, String>> fetchSourceFiles(String repositoryUrl) {
        String[] repository = parseRepositoryUrl(repositoryUrl);

        String owner = repository[0];
        String repo = repository[1];

        try {
            JsonNode repoInfo = getJson(
                    "https://api.github.com/repos/" + owner + "/" + repo
            );

            String branch = repoInfo.path("default_branch").asText();
 System.out.println("GitHub default branch: " + branch);
            if (branch.isBlank()) {
                throw new IllegalArgumentException(
                        "Could not determine the repository's default branch."
                );
            }

            String encodedBranch = java.net.URLEncoder
                    .encode(branch, java.nio.charset.StandardCharsets.UTF_8);

            JsonNode tree = getJson(
                    "https://api.github.com/repos/" + owner + "/" + repo
                    + "/git/trees/" + encodedBranch + "?recursive=1"
            );

            if (tree.path("truncated").asBoolean()) {
                throw new IllegalArgumentException(
                        "Repository tree is too large. Try a smaller repository."
                );
            }

            JsonNode entries = tree.path("tree");
System.out.println("GitHub tree entries: " + entries.size());
            if (!entries.isArray()) {
                throw new IOException("GitHub did not return a valid file tree.");
            }

            List<Map<String, String>> files = new ArrayList<>();
            int totalSize = 0;

            for (JsonNode entry : entries) {
                if (!"blob".equals(entry.path("type").asText())) {
                    continue;
                }

                String path = entry.path("path").asText();
                String lowerPath = path.toLowerCase(Locale.ROOT);
System.out.println("Repository file found: " + path);
System.out.println(
"Checking file: " + path
+ " | Supported: " + isSupportedFile(lowerPath)
+ " | Ignored: " + isIgnoredPath(lowerPath)
);

if (!isSupportedFile(lowerPath) || isIgnoredPath(lowerPath)) {
continue;
}

System.out.println("Selected for download: " + path);


                long size = entry.path("size").asLong(0);

                if (size > MAX_FILE_SIZE) {
                    continue;
                }

                if (files.size() >= MAX_FILES) {
                    break;
                }

                if (totalSize + size > MAX_TOTAL_SIZE) {
                    break;
                }

                String fileUrl = entry.path("url").asText();

                if (fileUrl.isBlank()) {
                    continue;
                }

                JsonNode fileData = getJson(fileUrl);
System.out.println("GitHub file response type: "
        + fileData.path("type").asText());

System.out.println("Downloaded content length: "
        + fileData.path("content").asText("").length());
                if (!"file".equals(fileData.path("type").asText())) {
                    continue;
                }

                String encodedContent = fileData.path("content").asText("");

                if (encodedContent.isBlank()) {
                    continue;
                }

                String content = new String(
                        Base64.getMimeDecoder().decode(encodedContent),
                        java.nio.charset.StandardCharsets.UTF_8
                );

                if (content.length() > MAX_FILE_SIZE) {
                    continue;
                }

                Map<String, String> sourceFile = new LinkedHashMap<>();
                sourceFile.put("path", path);
                sourceFile.put("content", content);

                files.add(sourceFile);
                totalSize += content.length();
                System.out.println("TOTAL DOWNLOADED FILES: " + files.size());
                System.out.println(
"Checking file: " + path
+ " | Supported: " + isSupportedFile(lowerPath)
+ " | Ignored: " + isIgnoredPath(lowerPath)
);

if (!isSupportedFile(lowerPath) || isIgnoredPath(lowerPath)) {
continue;
}

System.out.println("Selected for download: " + path);

            }

            return files;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Repository retrieval was interrupted.", e
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not retrieve repository files: " + e.getMessage(), e
            );
        }
    }

    private JsonNode getJson(String url)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "DevRecovery-Repository-Analyzer")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

     if (response.statusCode() == 403 || response.statusCode() == 429) {
    String remaining = response.headers()
            .firstValue("X-RateLimit-Remaining")
            .orElse("unknown");

    String reset = response.headers()
            .firstValue("X-RateLimit-Reset")
            .orElse("unknown");

    String rateLimitMessage;

    if ("0".equals(remaining)) {
        rateLimitMessage =
                "GitHub API rate limit reached. Reset timestamp: " + reset;
    } else {
        rateLimitMessage =
                "GitHub API access denied (HTTP " + response.statusCode()
                + "). Remaining requests: " + remaining
                + ". Check repository access or GitHub API permissions. "
                + response.body();
    }

    throw new IOException(rateLimitMessage);
}

        if (response.statusCode() == 404) {
            throw new IOException(
                    "Repository or source file not found. Check that it is public."
            );
        }

        if (response.statusCode() != 200) {
            throw new IOException(
                    "GitHub API returned HTTP " + response.statusCode()
            );
        }

        return objectMapper.readTree(response.body());
    }

    private String[] parseRepositoryUrl(String repositoryUrl) {
        if (repositoryUrl == null || repositoryUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "GitHub repository URL is required."
            );
        }

        String url = repositoryUrl.trim();

        Pattern pattern = Pattern.compile(
                "^https://github\\.com/([A-Za-z0-9_.-]+)/([A-Za-z0-9_.-]+)/?$",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Enter a repository URL like https://github.com/owner/repository"
            );
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2).replaceFirst("\\.git$", "");

        if (owner.equals(".") || owner.equals("..")
                || repo.equals(".") || repo.equals("..")
                || repo.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid GitHub owner or repository name."
            );
        }

        return new String[]{owner, repo};
    }

    private boolean isSupportedFile(String path) {
        int slash = path.lastIndexOf('/');
        int dot = path.lastIndexOf('.');

        if (dot <= slash) {
            return false;
        }

        String extension = path.substring(dot);

        return SUPPORTED_EXTENSIONS.contains(extension);
    }

    private boolean isIgnoredPath(String path) {
        String[] parts = path.split("/");

        for (String part : parts) {
            if (IGNORED_DIRECTORIES.contains(part)) {
                return true;
            }
        }

        return false;
    }
}
