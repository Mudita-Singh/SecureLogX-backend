package com.securelogx.securelogx_backend;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LogReader {

    public List<String> readLogs(String path) {
        try {
            return Files.readAllLines(Path.of(path));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read log file", e);
        }
    }
}
