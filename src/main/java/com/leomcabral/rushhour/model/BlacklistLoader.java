package com.leomcabral.rushhour.model;

import io.vavr.collection.SortedSet;
import io.vavr.collection.Stream;
import io.vavr.collection.TreeSet;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class BlacklistLoader {

    /**
     * Loads all black listed dates.
     * @return
     */
    public SortedSet<LocalDate> load(Path blacklistPath) throws IOException {
        Objects.requireNonNull(blacklistPath);
        verifyFile(blacklistPath);
        return TreeSet.ofAll(readContent(blacklistPath))
                .flatMap(this::makeLocalDate);
    }

    private Stream<LocalDate> makeLocalDate(String s) {
        String[] split = s.split("-");
        int day = Integer.valueOf(split[1]);
        int month = Integer.valueOf(split[0]);

        return Stream.from(2016, 1)
                .take(20)
                .map(year -> LocalDate.of(year, month, day));

    }

    private void verifyFile(Path blacklistPath) throws IOException {
        if (!Files.exists(blacklistPath)) {
            throw new IOException(String.format("File %s was not found", blacklistPath.getFileName()));
        }
        if (!Files.isRegularFile(blacklistPath)) {
            throw new IOException(String.format("%s is not a file", blacklistPath.getFileName()));
        }
        if (!Files.isReadable(blacklistPath)) {
            throw new IOException(String.format("%s is not readable", blacklistPath.getFileName()));
        }
    }

    private Set<String> readContent(Path blacklistPath) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(blacklistPath.toFile()))) {
            Set<String> entries = bufferedReader.lines()
                    .collect(Collectors.toSet());
            log.info("Blacklist loaded with {} entries", entries.size());
            return entries;
        } catch (IOException ex) {
            log.error("Blacklist {} not loaded", blacklistPath.getFileName());
            throw ex;
        }
    }

}
