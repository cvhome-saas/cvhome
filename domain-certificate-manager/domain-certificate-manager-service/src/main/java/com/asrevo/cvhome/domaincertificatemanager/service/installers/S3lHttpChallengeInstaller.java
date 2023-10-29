package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.config.InstallersConfigProperties;
import com.asrevo.cvhome.domaincertificatemanager.service.impl.HttpChallengeVerifyServiceProvider;
import org.springframework.stereotype.Service;

@Service
public class S3lHttpChallengeInstaller extends HttpInstaller {
    public S3lHttpChallengeInstaller(HttpChallengeVerifyServiceProvider provider) {
        super(provider);
    }

    @Override
    public InstallersConfigProperties.InstallerProvider provider() {
        return InstallersConfigProperties.InstallerProvider.S3;
    }
}
