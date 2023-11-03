package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.FileProvider;
import com.asrevo.cvhome.domaincertificatemanager.service.impl.AcmFileServiceProvider;
import org.springframework.stereotype.Service;

@Service
public class LocalTlsChallengeInstaller extends TlsChallengeInstaller {
    public LocalTlsChallengeInstaller(AcmFileServiceProvider provider) {
        super(provider);
    }

    @Override
    public String provider() {
        return FileProvider.LOCAL.name();
    }
}
