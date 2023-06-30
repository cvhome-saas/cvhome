package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.InputStream;

@Service
public class S3ServiceImpl implements S3Service {

    private final String bucketName = "uxplore-acm-storage-dev";

    @Autowired
    private S3Client s3Client;

    @Override
    public void uploadFile(File file, String fileName) {
        PutObjectRequest request =
                PutObjectRequest.builder().bucket(bucketName).key(fileName).build();
        s3Client.putObject(request, file.toPath());
    }

    @Override
    public InputStream getFile(String fileName) {
        GetObjectRequest request =
                GetObjectRequest.builder().bucket(bucketName).key(fileName).build();
        return s3Client.getObjectAsBytes(request).asInputStream();
    }

    @Override
    public boolean checkExist(String fileName) {
        try {
            HeadObjectRequest request =
                    HeadObjectRequest.builder().bucket(bucketName).key(fileName).build();
            return s3Client.headObject(request).contentLength() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
