package com.asrevo.cvhome.domaincertificatemanager.service.verify.http;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.*;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.HttpChallenge;
import com.asrevo.cvhome.domaincertificatemanager.repository.FilesRepository;
import com.asrevo.cvhome.domaincertificatemanager.service.verify.ChallengeServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HttpChallengeVerifyServiceProvider implements ChallengeServiceProvider<HttpChallenge, HttpChallengeVerifyService> {

    private final DcmChallengesConfigProperties configProperties;
    private final FilesRepository filesRepository;
    private HttpChallengeVerifyService instance;

    public HttpChallengeVerifyServiceProvider(DcmChallengesConfigProperties configProperties, FilesRepository filesRepository) {
        this.configProperties = configProperties;
        this.filesRepository = filesRepository;
    }

    @Override
    public HttpChallengeVerifyService getInstance(HttpChallenge challenge) {
        if (instance == null) {
            this.instance = build();
        }
        return this.instance;
    }

    private HttpChallengeVerifyService build() {
        HttpChallengeInstallerInstallerConfig httpConfig = this.configProperties.getHttpInstallerConfig();
        FileProvider provider = httpConfig.getInstallerFileProvider();

        switch (provider) {
            case S3 -> {
                S3FileProviderConfig s3Config = httpConfig.getInstallerS3Config();
                log.info("will use s3 as file service for http verify");
                return new S3HttpChallengeVerifyServiceImpl(s3Config.getBucket());
            }
            case LOCAL -> {
                LocalFileProviderConfig localConfig = httpConfig.getInstallerLocalConfig();
                log.info("will use local as file service for http verify");
                return new LocalHttpChallengeVerifyServiceImpl();
            }
            case TABLE -> {
                TableFileProviderConfig tableConfig = httpConfig.getInstallerTableConfig();
                log.info("will use table as file service for http verify");
                return new TableHttpChallengeVerifyServiceImpl(filesRepository);
            }
            default -> throw new RuntimeException("providers not implemented");
        }
    }
}
