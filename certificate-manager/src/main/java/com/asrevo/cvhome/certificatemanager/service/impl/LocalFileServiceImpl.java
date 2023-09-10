package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.service.FileService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalFileServiceImpl implements FileService {
    private Path acmRoot;
    private Path tokenRoot;

    @SneakyThrows
    public LocalFileServiceImpl() {
        acmRoot = createAcmBaseDirectory();
        tokenRoot = createTokenBaseDirectory();
    }

    @SneakyThrows
    private static void createParentDirectory(Path destination) {
        if (!destination.getParent().toFile().exists()) {
            Files.createDirectories(destination.getParent());
        }
    }

/*
    private final String bucketName = "uxplore-acm-storage-dev";
    @Autowired
    private S3Client s3Client;
*/

    private Path createAcmBaseDirectory() throws IOException {
        this.acmRoot = Paths.get(System.getProperty("user.home"), "cvhome/certificate-manager/acm");
        createParentDirectory(this.acmRoot);
        return this.acmRoot;
    }

    private Path createTokenBaseDirectory() throws IOException {
        this.tokenRoot = Paths.get(System.getProperty("user.home"), "cvhome/certificate-manager/token");
        createParentDirectory(this.tokenRoot);
        return this.tokenRoot;
    }

    @SneakyThrows
    @Override
    public void upload(File file, String fileName) {
        Path destination = acmRoot.resolve(fileName);
        if (destination.toFile().exists()) {
            destination.toFile().delete();
        }
        createParentDirectory(destination);
        Files.copy(new FileInputStream(file), destination);
/*
        PutObjectRequest request =
                PutObjectRequest.builder().bucket(bucketName).key(fileName).build();
        s3Client.putObject(request, file.toPath());
*/
    }

    @SneakyThrows
    @Override
    public void addToken(String token, String content) {
        Path destination = tokenRoot.resolve(token);
        if (destination.toFile().exists()) {
            destination.toFile().delete();
        }
        createParentDirectory(destination);
        Files.copy(new ByteArrayInputStream(content.getBytes()), destination);
/*
        PutObjectRequest request =
                PutObjectRequest.builder().bucket(bucketName).key(fileName).build();
        s3Client.putObject(request, file.toPath());
*/
    }

    @SneakyThrows
    @Override
    public InputStream getTokenValue(String token) {
        Path destination = tokenRoot.resolve(token);
        return new ByteArrayInputStream(Files.readAllBytes(destination));
    }


    @SneakyThrows
    @Override
    public InputStream getFile(String fileName) {
        File file = acmRoot.resolve(fileName).toFile();
        return new FileInputStream(file);
/*
        GetObjectRequest request =
                GetObjectRequest.builder().bucket(bucketName).key(fileName).build();
        return s3Client.getObjectAsBytes(request).asInputStream();
*/
//        return null;
    }

    @Override
    public boolean exist(String fileName) {
        return acmRoot.resolve(fileName).toFile().exists();
/*
        try {
            HeadObjectRequest request =
                    HeadObjectRequest.builder().bucket(bucketName).key(fileName).build();
            return s3Client.headObject(request).contentLength() > 0;
        } catch (Exception e) {
            return false;
        }
*/
    }
}
