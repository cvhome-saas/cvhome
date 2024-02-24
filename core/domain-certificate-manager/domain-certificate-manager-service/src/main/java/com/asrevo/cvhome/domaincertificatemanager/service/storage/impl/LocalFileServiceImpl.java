package com.asrevo.cvhome.domaincertificatemanager.service.storage.impl;

import com.asrevo.cvhome.domaincertificatemanager.service.storage.FileService;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalFileServiceImpl implements FileService {

    @SneakyThrows
    private static void createParentDirectory(Path destination) {
        if (!destination.getParent().toFile().exists()) {
            Files.createDirectories(destination.getParent());
        }
    }

    @SneakyThrows
    @Override
    public void upload(File file, String fileName) {
        Path destination = Paths.get(fileName);
        if (destination.toFile().exists()) {
            destination.toFile().delete();
        }
        createParentDirectory(destination);
        Files.copy(new FileInputStream(file), destination);
    }

    @SneakyThrows
    @Override
    public InputStream getFile(String fileName) {
        return new FileInputStream(Paths.get(fileName).toFile());
    }

    @Override
    public boolean exist(String fileName) {
        return Paths.get(fileName).toFile().exists();
    }
}
