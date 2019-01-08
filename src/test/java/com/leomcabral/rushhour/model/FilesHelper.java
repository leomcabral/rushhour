package com.leomcabral.rushhour.model;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class FilesHelper {

    static final Path TEST_DIR = Paths.get("./test");

    static void createTestDirIfNeeded() throws IOException {
        if (!Files.exists(TEST_DIR)) {
            Files.createDirectories(TEST_DIR);
        }
    }

    static void removeTestDirIfEmpty() throws IOException {
        try {
            Files.deleteIfExists(TEST_DIR);
        } catch (DirectoryNotEmptyException ex) {
            // just ignore
        }

    }

}
