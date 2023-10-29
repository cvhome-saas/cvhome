package com.asrevo.cvhome.domaincertificatemanager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Getter
@Setter
@ConfigurationProperties("com.asrevo.file-config")
public class FileServiceConfigProperties {
    private Set<FileProvider> providers = Set.of(FileProvider.LOCAL);
    private S3Config s3Config;
    private LocalConfig localConfig;

    public enum FileProvider {
        LOCAL, S3
    }

    @Getter
    @Setter
    public static class S3Config {
        private String certBucket;
        private String tokenBucket;
    }

    @Getter
    @Setter
    public static class LocalConfig {
        private String directory;
    }
}
