package com.asrevo.cvhome.domaincertificatemanager.domain;

import lombok.SneakyThrows;

import java.net.URI;
import java.nio.file.Path;

public record S3KeyPath(String bucket, String key) {
    public S3KeyPath(String fileName) {
        this(getUri(fileName));
    }

    S3KeyPath(URI uri) {
        this(uri.getHost(), removeLeftSlash(uri.getPath()));
    }

    @SneakyThrows
    private static URI getUri(String fileName) {
        return new URI(fileName);
    }

    private static String removeLeftSlash(String path) {
        if (!path.isEmpty()) {
            return path.substring(1);
        } else {
            return path;
        }
    }

    public static String resolveS3Uri(String path, Path acmRoot) {
        return "s3://" + acmRoot.resolve(path);
    }
}
