package com.asrevo.cvhome.store;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class MinIOContainer extends GenericContainer<MinIOContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("bitnami/minio");

    private static final int MINIO_S3_PORT = 9000;

    private static final int MINIO_UI_PORT = 9001;

    private static final String DEFAULT_USER = "minioadmin";

    private static final String DEFAULT_PASSWORD = "minioadmin";

    private static final String DEFAULT_ACCESS_KEY = "minioadmin";

    private static final String DEFAULT_SECRET_KEY = "minioadmin";

    private String userName;

    private String password;

    private String accessKey;

    private String secretKey;

    /**
     * Constructs a MinIO container from the dockerImageName
     *
     * @param dockerImageName the full image name to use
     */
    public MinIOContainer(final String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    /**
     * Constructs a MinIO container from the dockerImageName
     *
     * @param dockerImageName the full image name to use
     */
    public MinIOContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        withExposedPorts(MINIO_S3_PORT, MINIO_UI_PORT);
//        withCommand("server", "--console-address", ":" + MINIO_UI_PORT, "/data");
//        waitingFor(
//                Wait
//                        .forHttp("/minio/health/live")
//                        .forPort(MINIO_S3_PORT)
//                        .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
//        );
    }

    /**
     * Overrides the DEFAULT_USER
     *
     * @param userName the Root user to override
     * @return this
     */
    public MinIOContainer withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Overrides the DEFAULT_PASSWORD
     *
     * @param password the Root user's password to override
     * @return this
     */
    public MinIOContainer withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Configures the MinIO container
     */
    @Override
    public void configure() {
        if (this.userName != null) {
            addEnv("MINIO_ROOT_USER", this.userName);
        } else {
            this.userName = DEFAULT_USER;
            addEnv("MINIO_ROOT_USER", this.userName);
        }
        if (this.password != null) {
            addEnv("MINIO_ROOT_PASSWORD", this.password);
        } else {
            this.password = DEFAULT_PASSWORD;
            addEnv("MINIO_ROOT_PASSWORD", this.password);
        }

        if (this.accessKey != null) {
            addEnv("MINIO_ACCESS_KEY", this.accessKey);
        } else {
            this.accessKey = DEFAULT_ACCESS_KEY;
            addEnv("MINIO_ACCESS_KEY", this.accessKey);
        }

        if (this.secretKey != null) {
            addEnv("MINIO_SECRET_KEY", this.secretKey);
        } else {
            this.secretKey = DEFAULT_SECRET_KEY;
            addEnv("MINIO_SECRET_KEY", this.secretKey);
        }
    }

    /**
     * @return the URL to upload/download objects from
     */
    public String getS3URL() {
        return String.format("http://%s:%s", this.getHost(), getMappedPort(MINIO_S3_PORT));
    }

    public String getUiURL() {
        return String.format("http://%s:%s", this.getHost(), getMappedPort(MINIO_UI_PORT));
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
