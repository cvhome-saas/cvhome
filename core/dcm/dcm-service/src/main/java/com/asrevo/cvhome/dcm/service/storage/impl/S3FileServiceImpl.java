package com.asrevo.cvhome.dcm.service.storage.impl;

import com.asrevo.cvhome.dcm.domain.S3KeyPath;
import com.asrevo.cvhome.dcm.service.storage.FileService;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.InputStream;

public class S3FileServiceImpl implements FileService {
    private final S3Client s3Client;

    public S3FileServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public S3FileServiceImpl() {
        this.s3Client = S3Client.create();
    }

    @Override
    public void upload(File file, String fileName) {
        S3KeyPath s3KeyPath = new S3KeyPath(fileName);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3KeyPath.bucket())
                .key(s3KeyPath.key())
                .build();
        s3Client.putObject(request, file.toPath());
    }

    @Override
    public InputStream getFile(String fileName) {
        S3KeyPath s3KeyPath = new S3KeyPath(fileName);
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3KeyPath.bucket())
                .key(s3KeyPath.key())
                .build();
        return s3Client.getObject(request);
    }

    @Override
    public boolean exist(String fileName) {
        try {
            S3KeyPath s3KeyPath = new S3KeyPath(fileName);
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(s3KeyPath.bucket())
                    .key(s3KeyPath.key())
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}

