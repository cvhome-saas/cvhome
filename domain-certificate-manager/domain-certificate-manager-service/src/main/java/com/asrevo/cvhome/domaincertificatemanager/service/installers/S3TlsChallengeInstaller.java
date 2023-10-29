package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.config.InstallersConfigProperties.InstallerProvider;
import com.asrevo.cvhome.domaincertificatemanager.service.impl.AcmFileServiceProvider;
import org.springframework.stereotype.Service;

@Service
public class S3TlsChallengeInstaller extends TlsChallengeInstaller {
    public S3TlsChallengeInstaller(AcmFileServiceProvider provider) {
        super(provider);
    }

    @Override
    public InstallerProvider provider() {
        return InstallerProvider.S3;
    }
}
