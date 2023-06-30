package com.asrevo.cvhome.certificatemanager.service;

import java.io.File;
import java.io.InputStream;

public interface S3Service {

    void uploadFile(File file, String fileName);

    InputStream getFile(String fileName);

    boolean checkExist(String domainPrivateKey);
}
