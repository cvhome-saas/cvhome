package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.service.FileService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

@Service
public class FileServiceImpl implements FileService {


/*
    private final String bucketName = "uxplore-acm-storage-dev";
    @Autowired
    private S3Client s3Client;
*/

    @Override
    public void uploadFile(File file, String fileName) {
/*
        PutObjectRequest request =
                PutObjectRequest.builder().bucket(bucketName).key(fileName).build();
        s3Client.putObject(request, file.toPath());
*/
    }

    @Override
    public InputStream getFile(String fileName) {
/*
        GetObjectRequest request =
                GetObjectRequest.builder().bucket(bucketName).key(fileName).build();
        return s3Client.getObjectAsBytes(request).asInputStream();
*/
        return null;
    }

    @Override
    public boolean checkExist(String fileName) {
/*
        try {
            HeadObjectRequest request =
                    HeadObjectRequest.builder().bucket(bucketName).key(fileName).build();
            return s3Client.headObject(request).contentLength() > 0;
        } catch (Exception e) {
            return false;
        }
*/
        return false;
    }
}
