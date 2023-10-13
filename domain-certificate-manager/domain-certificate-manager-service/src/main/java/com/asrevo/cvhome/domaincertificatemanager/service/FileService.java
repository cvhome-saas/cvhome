package com.asrevo.cvhome.domaincertificatemanager.service;

import java.io.File;
import java.io.InputStream;

public interface FileService {

    void upload(File file, String fileName);

    InputStream getFile(String fileName);

    boolean exist(String fileName);

}
