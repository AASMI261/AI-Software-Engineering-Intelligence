package demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReaderService {
    // Minimal example for testing file-path validation checks.
    public String readText(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
