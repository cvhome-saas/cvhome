package com.asrevo.cvhome.domaincertificatemanager.service.acm;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.LocalFileProviderConfig;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.S3FileProviderConfig;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.TableFileProviderConfig;
import com.asrevo.cvhome.domaincertificatemanager.repository.FilesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AcmFileServiceProvider {
    private final DcmChallengesConfigProperties configProperties;
    private final FilesRepository filesRepository;
    private AcmFileService instance;

    public AcmFileServiceProvider(DcmChallengesConfigProperties configProperties, FilesRepository filesRepository) {
        this.configProperties = configProperties;
        this.filesRepository = filesRepository;
    }

    public AcmFileService getInstance() {
        if (instance == null) {
            this.instance = build();
        }
        return this.instance;
    }


    private AcmFileService build() {
        switch (this.configProperties.getAcmFileProvider()) {
            case S3 -> {
                S3FileProviderConfig s3Config = this.configProperties.getAcmS3Config();
                log.info("will use s3 as file service for cert");
                return new S3AcmFileServiceImpl(s3Config.getBucket());
            }
            case LOCAL -> {
                LocalFileProviderConfig localConfig = this.configProperties.getAcmLocalConfig();
                log.info("will use local as file service for cert");
                return new LocalAcmFileServiceImpl();
            }
            case TABLE -> {
                TableFileProviderConfig tableConfig = this.configProperties.getAcmTableConfig();
                log.info("will use table as file service for cert");
                return new TableAcmFileServiceImpl(filesRepository);
            }
            default -> throw new RuntimeException("providers not implemented");
        }
    }
}