package com.asrevo.cvhome.content.config;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.content.service.ObjectStorage;
import com.asrevo.cvhome.s2s.model.CdnStorageProperties;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3ObjectStorage implements ObjectStorage {
    private final S3Client client;
    private final CdnStorageProperties properties;

    public S3ObjectStorage(S3Client client, CdnStorageProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void put(String key, Path file, String contentType) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .build();
        try {
            client.putObject(request, RequestBody.fromFile(file));
        } catch (SdkException exception) {
            throw new IOException("Object storage put failed", exception);
        }
    }

    @Override
    public void delete(String key) {
        client.deleteObject(DeleteObjectRequest.builder().bucket(properties.bucket()).key(key).build());
    }
}
