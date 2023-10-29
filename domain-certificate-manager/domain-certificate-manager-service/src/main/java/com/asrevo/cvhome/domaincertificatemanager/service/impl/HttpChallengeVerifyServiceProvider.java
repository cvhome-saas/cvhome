package com.asrevo.cvhome.domaincertificatemanager.service.impl;

import com.asrevo.cvhome.domaincertificatemanager.config.FileServiceConfigProperties;
import com.asrevo.cvhome.domaincertificatemanager.service.HttpChallengeVerifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.asrevo.cvhome.domaincertificatemanager.config.FileServiceConfigProperties.FileProvider.LOCAL;
import static com.asrevo.cvhome.domaincertificatemanager.config.FileServiceConfigProperties.FileProvider.S3;

@Service
@Slf4j
public class HttpChallengeVerifyServiceProvider {

    private final FileServiceConfigProperties configProperties;
    private HttpChallengeVerifyService instance;

    public HttpChallengeVerifyServiceProvider(FileServiceConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public HttpChallengeVerifyService getInstance() {
        if (instance == null) {
            this.instance = build();
        }
        return this.instance;
    }

    private HttpChallengeVerifyService build() {
        return build(configProperties.getProviders());
    }

    private HttpChallengeVerifyService build(Set<FileServiceConfigProperties.FileProvider> providers) {
        if (providers.contains(S3)) {
            log.info("will use s3 as file service for http verify");
            return new S3HttpChallengeVerifyServiceImpl(configProperties.getS3Config().getTokenBucket());
        } else if (providers.contains(LOCAL)) {
            log.info("will use local as file service for http verify");
            return new LocalHttpChallengeVerifyServiceImpl();
        } else {
            throw new RuntimeException("providers not implemented");
        }
    }

    public HttpChallengeVerifyService create(FileServiceConfigProperties.FileProvider provider) {
        return build(Set.of(provider));
    }

}
