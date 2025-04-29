package com.asrevo.cvhome.order.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class MinIOContainer extends GenericContainer<MinIOContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("bitnami/minio:latest");
    private static final int MINIO_PORT = 9000;
    private static final int MINIO_API_PORT = 9000;
    private static final int MINIO_UI_PORT = 9001;

    private String accessKey = "testaccesskey";
    private String secretKey = "testsecretkey";
    private String bucket;

    public MinIOContainer() {
        this(DEFAULT_IMAGE_NAME);
    }

    public MinIOContainer(String defaultBucket) {
        this(DEFAULT_IMAGE_NAME, defaultBucket);
    }

    public MinIOContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        withExposedPorts(MINIO_PORT, MINIO_UI_PORT);
        withEnv("MINIO_ROOT_USER", accessKey);
        withEnv("MINIO_ROOT_PASSWORD", secretKey);
        waitingFor(Wait.forHttp("/minio/health/live")
                .forPort(MINIO_PORT)
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofSeconds(60)));
    }

    public MinIOContainer(DockerImageName dockerImageName, String defaultBucket) {
        this(dockerImageName);
        this.bucket = defaultBucket;
        if (defaultBucket != null && !defaultBucket.isEmpty()) {
            withEnv("MINIO_DEFAULT_BUCKETS", defaultBucket);
        }
    }


    public MinIOContainer withAccessKey(String accessKey) {
        this.accessKey = accessKey;
        withEnv("MINIO_ROOT_USER", this.accessKey);
        return this;
    }

    public MinIOContainer withSecretKey(String secretKey) {
        this.secretKey = secretKey;
        withEnv("MINIO_ROOT_PASSWORD", this.secretKey);
        return this;
    }

    public String getS3URL() {
        return String.format("http://%s:%s", getHost(), getMappedPort(MINIO_PORT));
    }

    public String getUiURL() {
        return String.format("http://%s:%s", getHost(), getMappedPort(MINIO_UI_PORT));
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getBucket() {
        return bucket;
    }
}