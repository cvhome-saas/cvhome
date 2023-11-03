package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties.FileProvider;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ChallengeInstall;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.HttpChallenge;
import com.asrevo.cvhome.domaincertificatemanager.service.ChallengeInstaller;
import com.asrevo.cvhome.domaincertificatemanager.service.impl.HttpChallengeVerifyServiceProvider;
import org.springframework.stereotype.Service;

@Service
public class LocalHttpChallengeInstaller implements ChallengeInstaller {
    private final HttpChallengeVerifyServiceProvider provider;

    public LocalHttpChallengeInstaller(HttpChallengeVerifyServiceProvider provider) {
        this.provider = provider;
    }

    @Override
    public synchronized boolean setup(ChallengeInstall c) {
        return c.challenges()
                .stream()
                .map(it -> ((HttpChallenge) it))
                .allMatch(it -> provider.getInstance().create(it));
    }

    @Override
    public boolean clean(ChallengeInstall challenge) {
        return false;
    }


    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.Http01;
    }

    @Override
    public String provider() {
        return FileProvider.LOCAL.name();
    }
}