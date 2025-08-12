package com.web.utils;

import java.nio.file.Path;

public class DirectoryManager {

    private static DirectoryManager instance;
    private final Directory directory;

    private DirectoryManager(Path urlPath, Path failurePath, Path successPath) {
        this.directory = new Directory(urlPath, failurePath, successPath);
    }

    public static synchronized void setInstance(Path urlPath, Path failurePath, Path successPath){
        if(instance != null) return;
        instance = new DirectoryManager(urlPath, failurePath, successPath);
    }

    public static Path getFailurePath(){
        return instance.directory.failurePath;
    }

    public static Path getUrlPath(){
        return instance.directory.urlPath;
    }

    public static Path getSuccessPath(){
        return instance.directory.successPath;
    }

    public record Directory(Path urlPath, Path failurePath, Path successPath) {
    }
}
