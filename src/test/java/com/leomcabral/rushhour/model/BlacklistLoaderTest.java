package com.leomcabral.rushhour.model;

import com.leomcabral.rushhour.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Blacklist Loader")
class BlacklistLoaderTest {

    private BlacklistLoader underTest;

    @BeforeEach
    void setup() {
        underTest = new BlacklistLoader();
    }

    @Test
    @DisplayName("Null path is not allowed")
    void loadNullValue() {
        assertThrows(NullPointerException.class, () -> underTest.load(null));
    }

    @Test
    @DisplayName("Blacklist file not found")
    void blacklistNotFound() {
        final Path invalidPath = Paths.get("/asdfrrrr8jD");
        assertThrows(IOException.class, () -> underTest.load(invalidPath));
    }

    @Test
    @DisplayName("Only regular files should be used")
    void notAFile() {
        final Path directory = Paths.get("./data");
        assertThrows(IOException.class, () -> underTest.load(directory));
    }

    @Test
    @DisplayName("Only readable files should be used")
    void notReadable() throws IOException {
        FilesHelper.createTestDirIfNeeded();
        Path path = FilesHelper.TEST_DIR.resolve("notreadable.csv");
        try {
            Files.createFile(path);
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("-w-------"));

            assertThrows(IOException.class, () -> underTest.load(path));
        } finally {
            Files.deleteIfExists(path);
            FilesHelper.removeTestDirIfEmpty();
        }
    }

    @Test
    @DisplayName("Can load blacklist CSV file")
    void blacklistLoaded() throws Exception {
        FileManager fileManager = new FileManager();
        File file = fileManager.getFromDataDir("date-blacklist.csv");
        underTest.load(file.toPath());
    }

}
