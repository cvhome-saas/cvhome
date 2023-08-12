package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.service.FileService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalFileServiceImpl implements FileService {
    private Path root;

    @SneakyThrows
    public LocalFileServiceImpl() {
        root = createTempDirectory();
    }

    private static void createParentDirectory(Path destination) {
        if (!destination.getParent().toFile().exists()) {
            destination.getParent().toFile().mkdir();
        }
    }

/*
    private final String bucketName = "uxplore-acm-storage-dev";
    @Autowired
    private S3Client s3Client;
*/

    private Path createTempDirectory() throws IOException {
        this.root = Paths.get(System.getProperty("java.io.tmpdir"), "cvhome-acm");
        if (!this.root.toFile().exists()) {
            Files.createDirectory(this.root);
        }
        return this.root;
    }

    @SneakyThrows
    @Override
    public void upload(File file, String fileName) {
        Path destination = root.resolve(fileName);
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
    public InputStream getFile(String fileName) {
        File file = root.resolve(fileName).toFile();
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
        return root.resolve(fileName).toFile().exists();
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
