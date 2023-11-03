package com.asrevo.cvhome.domaincertificatemanager.service.impl;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.FileProvider;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.HttpChallengeConfig;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.LocalFileProviderConfig;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.S3FileProviderConfig;
import com.asrevo.cvhome.domaincertificatemanager.service.HttpChallengeVerifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HttpChallengeVerifyServiceProvider {

    private final DcmChallengesConfigProperties configProperties;
    private HttpChallengeVerifyService instance;

    public HttpChallengeVerifyServiceProvider(DcmChallengesConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public HttpChallengeVerifyService getInstance() {
        if (instance == null) {
            this.instance = build();
        }
        return this.instance;
    }

    private HttpChallengeVerifyService build() {
        HttpChallengeConfig httpConfig = this.configProperties.getHttpConfig();
        FileProvider provider = httpConfig.getProvider();

        switch (provider) {
            case S3 -> {
                S3FileProviderConfig s3Config = httpConfig.getS3Config();
                log.info("will use s3 as file service for http verify");
                return new S3HttpChallengeVerifyServiceImpl(s3Config.getBucket());
            }
            case LOCAL -> {
                LocalFileProviderConfig localConfig = httpConfig.getLocalConfig();
                log.info("will use local as file service for http verify");
                return new LocalHttpChallengeVerifyServiceImpl();
            }
            default -> throw new RuntimeException("providers not implemented");
        }
    }
}
