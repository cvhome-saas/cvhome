package com.asrevo.cvhome.content.service;

import java.io.IOException;
import java.nio.file.Path;

public interface ObjectStorage {
    void put(String key, Path file, String contentType) throws IOException;

    void delete(String key);
}
