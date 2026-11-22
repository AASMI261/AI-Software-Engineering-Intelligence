package demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileService {
    private final Path storage = Path.of("uploads");

    // INTENTIONAL FLAW: filename is not normalized/constrained to storage.
    public String readUpload(String filename) throws IOException {
        Path requested = storage.resolve(filename);
        return Files.readString(requested);
    }
}
