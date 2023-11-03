package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.FileProvider;
import com.asrevo.cvhome.domaincertificatemanager.service.impl.AcmFileServiceProvider;
import org.springframework.stereotype.Service;


@Service
public class S3TlsChallengeInstaller extends TlsChallengeInstaller {
    public S3TlsChallengeInstaller(AcmFileServiceProvider provider) {
        super(provider);
    }

    @Override
    public String provider() {
        return FileProvider.S3.name();
    }
}
