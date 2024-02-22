package com.asrevo.cvhome.domaincertificatemanager.service.verify.tlsalpn01;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.LocalFileProviderConfig;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.S3FileProviderConfig;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.TableFileProviderConfig;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.TlsAlpn01ChallengeInstallerInstallerConfig;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.TlsAlpnChallenge;
import com.asrevo.cvhome.domaincertificatemanager.repository.FilesRepository;
import com.asrevo.cvhome.domaincertificatemanager.service.acm.LocalAcmFileServiceImpl;
import com.asrevo.cvhome.domaincertificatemanager.service.acm.S3AcmFileServiceImpl;
import com.asrevo.cvhome.domaincertificatemanager.service.acm.TableAcmFileServiceImpl;
import com.asrevo.cvhome.domaincertificatemanager.service.verify.ChallengeServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TlsAlpn01ChallengeVerifyServiceProvider implements ChallengeServiceProvider<TlsAlpnChallenge, TlsAlpn01ChallengeVerifyService> {

    private final DcmChallengesConfigProperties configProperties;
    private final FilesRepository filesRepository;
    private TlsAlpn01ChallengeVerifyService instance;

    public TlsAlpn01ChallengeVerifyServiceProvider(DcmChallengesConfigProperties configProperties, FilesRepository filesRepository) {
        this.configProperties = configProperties;
        this.filesRepository = filesRepository;
    }

    @Override
    public TlsAlpn01ChallengeVerifyService getInstance(TlsAlpnChallenge challenge) {
        if (instance == null) {
            this.instance = build();
        }
        return this.instance;
    }

    private TlsAlpn01ChallengeVerifyService build() {
        TlsAlpn01ChallengeInstallerInstallerConfig installerConfig = this.configProperties.getTlsAlpn01InstallerConfig();
        switch (installerConfig.getInstallerFileProvider()) {
            case S3 -> {
                S3FileProviderConfig s3Config = installerConfig.getInstallerS3Config();
                log.info("will use s3 as file service for http verify");
                return new TlsAlpn01ChallengeVerifyServiceImpl(new S3AcmFileServiceImpl(s3Config.getBucket()));
            }
            case LOCAL -> {
                LocalFileProviderConfig localConfig = installerConfig.getInstallerLocalConfig();
                log.info("will use local as file service for http verify");
                return new TlsAlpn01ChallengeVerifyServiceImpl(new LocalAcmFileServiceImpl());
            }
            case TABLE -> {
                TableFileProviderConfig tableConfig = installerConfig.getInstallerTableConfig();
                log.info("will use table as file service for http verify");
                return new TlsAlpn01ChallengeVerifyServiceImpl(new TableAcmFileServiceImpl(filesRepository));
            }
            default -> throw new RuntimeException("providers not implemented");
        }
    }
}
