package com.asrevo.cvhome.domaincertificatemanager.service.impl;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.LocalFileProviderConfig;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.S3FileProviderConfig;
import com.asrevo.cvhome.domaincertificatemanager.service.AcmFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AcmFileServiceProvider {
    private final DcmChallengesConfigProperties configProperties;
    private AcmFileService instance;

    public AcmFileServiceProvider(DcmChallengesConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public AcmFileService getInstance() {
        if (instance == null) {
            this.instance = build();
        }
        return this.instance;
    }


    private AcmFileService build() {
        DcmChallengesConfigProperties.HttpChallengeConfig httpConfig = this.configProperties.getHttpConfig();
        DcmChallengesConfigProperties.FileProvider provider = httpConfig.getProvider();

        switch (provider) {
            case S3 -> {
                S3FileProviderConfig s3Config = httpConfig.getS3Config();
                log.info("will use local as file service for cert");
                return new S3AcmFileServiceImpl(s3Config.getBucket());
            }
            case LOCAL -> {
                LocalFileProviderConfig localConfig = httpConfig.getLocalConfig();
                log.info("will use local as file service for cert");
                return new LocalAcmFileServiceImpl();
            }
            default -> throw new RuntimeException("providers not implemented");
        }
    }
}