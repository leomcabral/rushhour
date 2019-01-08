package com.leomcabral.rushhour;

import java.nio.file.Path;

public class FileContext {
    private String contextName;
    private Path path;

    private FileContext(String contextName, Path path) {
        this.contextName = contextName;
        this.path = path;
    }

    public static FileContext of(String contextName, Path path) {
        return new FileContext(contextName, path);
    }

    public String getContextName() {
        return this.contextName;
    }

    public Path getPath() {
        return this.path;
    }
}
