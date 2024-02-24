package com.asrevo.cvhome.domaincertificatemanager.service.storage.impl;

import com.asrevo.cvhome.domaincertificatemanager.entity.FileEntity;
import com.asrevo.cvhome.domaincertificatemanager.repository.FilesRepository;
import com.asrevo.cvhome.domaincertificatemanager.service.storage.FileService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

@Service
@AllArgsConstructor
public class TableFileServiceImpl implements FileService {
    private final FilesRepository filesRepository;

    @SneakyThrows
    @Override
    public void upload(File file, String fileName) {
        FileEntity entity = FileEntity.create(fileName, new String(Files.readAllBytes(file.toPath())));
        filesRepository.save(entity);
    }

    @Override
    public InputStream getFile(String fileName) {
        return filesRepository.findFirstByPath(fileName).map(it -> new ByteArrayInputStream(it.getContent().getBytes())).orElse(null);
    }

    @Override
    public boolean exist(String fileName) {
        return filesRepository.findFirstByPath(fileName).map(it -> Objects.nonNull(it.getContent())).orElse(false);
    }
}