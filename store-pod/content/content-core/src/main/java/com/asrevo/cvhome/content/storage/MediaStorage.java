package com.asrevo.cvhome.content.storage;

import java.util.Map;

import com.asrevo.cvhome.content.errors.MediaStorageException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Object storage for the media library: one pod bucket, keys prefixed per store
 * ({@code files/{storeId}/media/{assetId}/{filename}}), public-read through the bucket policy the service installs
 * on MinIO (and the CDN in front of S3). Writes the real {@code Content-Type} so browsers render PDFs and videos.
 */
@Slf4j
public class MediaStorage {

    private final S3Client s3;

    private final String bucket;

    private final String basePath;

    public MediaStorage(S3Client s3, String bucket, String basePath) {
        this.s3 = s3;
        this.bucket = bucket;
        this.basePath = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
    }

    public static String key(String storeId, Long assetId, String filename) {
        return String.format("files/%s/media/%d/%s", storeId, assetId, filename);
    }

    public String url(String key) {
        return String.format("%s/%s", basePath, key);
    }

    public void put(String key, byte[] bytes, String contentType) throws MediaStorageException {
        try {
            s3.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .metadata(Map.of("content-type", contentType))
                    .build(), RequestBody.fromBytes(bytes));
        } catch (RuntimeException e) {
            log.error("Media upload failed for {}", key, e);
            throw MediaStorageException.of("upload", key, e);
        }
    }

    public void delete(String key) throws MediaStorageException {
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (RuntimeException e) {
            log.error("Media delete failed for {}", key, e);
            throw MediaStorageException.of("delete", key, e);
        }
    }

}
