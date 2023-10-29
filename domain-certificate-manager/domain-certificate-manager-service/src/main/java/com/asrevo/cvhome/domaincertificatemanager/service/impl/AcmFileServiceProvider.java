package com.asrevo.cvhome.domaincertificatemanager.service.impl;

import com.asrevo.cvhome.domaincertificatemanager.config.FileServiceConfigProperties;
import com.asrevo.cvhome.domaincertificatemanager.config.FileServiceConfigProperties.FileProvider;
import com.asrevo.cvhome.domaincertificatemanager.service.AcmFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.asrevo.cvhome.domaincertificatemanager.config.FileServiceConfigProperties.FileProvider.LOCAL;
import static com.asrevo.cvhome.domaincertificatemanager.config.FileServiceConfigProperties.FileProvider.S3;

@Service
@Slf4j
public class AcmFileServiceProvider {
    private final FileServiceConfigProperties configProperties;
    private AcmFileService instance;

    public AcmFileServiceProvider(FileServiceConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public AcmFileService getInstance() {
        if (instance == null) {
            this.instance = build();
        }
        return this.instance;
    }

    private AcmFileService build() {
        return build(configProperties.getProviders());
    }

    private AcmFileService build(Set<FileProvider> providers) {
        if (providers.contains(S3)) {
            log.info("will use s3 as file service for cert");
            return new S3AcmFileServiceImpl(configProperties.getS3Config().getCertBucket());
        } else if (providers.contains(LOCAL)) {
            log.info("will use local as file service for cert");
            return new LocalAcmFileServiceImpl();
        } else {
            throw new RuntimeException("providers not implemented");
        }
    }

    public AcmFileService create(FileProvider provider) {
        return build(Set.of(provider));
    }
}