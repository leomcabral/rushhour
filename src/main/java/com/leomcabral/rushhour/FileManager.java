package com.leomcabral.rushhour;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FileManager {

    private static final String DATA_DIR = "datadir";

    private final Map<String, FileContext> contexts = new HashMap<>();

    public FileManager() {
        contexts.put(DATA_DIR, FileContext.of(DATA_DIR, Paths.get("./data")));
    }

    public File get(String context, String fileName) throws FileNotFoundException {
        FileContext fileContext = contexts.get(context);
        if (fileContext == null) {
            throw new IllegalArgumentException(String.format("File context %s not found", context));
        }

        Path contextPath = fileContext.getPath();
        Path filePath = contextPath.resolve(fileName);

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException(String.format("File %s doesn't exists", filePath.toAbsolutePath().toString()));
        }

        return filePath.toFile();
    }

    public File getFromDataDir(String filename) throws FileNotFoundException {
        return get(DATA_DIR, filename);
    }

}
