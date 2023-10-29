package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.config.InstallersConfigProperties.InstallerProvider;
import com.asrevo.cvhome.domaincertificatemanager.service.impl.AcmFileServiceProvider;
import org.springframework.stereotype.Service;

@Service
public class LocalTlsChallengeInstaller extends TlsChallengeInstaller {
    public LocalTlsChallengeInstaller(AcmFileServiceProvider provider) {
        super(provider);
    }

    @Override
    public InstallerProvider provider() {
        return InstallerProvider.LOCAL;
    }
}
