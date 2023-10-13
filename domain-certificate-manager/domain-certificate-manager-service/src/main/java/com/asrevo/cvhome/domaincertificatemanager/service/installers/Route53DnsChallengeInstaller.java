package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.Challenge;
import com.asrevo.cvhome.domaincertificatemanager.service.ChallengeInstaller;
import org.springframework.stereotype.Service;

@Service
public class Route53DnsChallengeInstaller implements ChallengeInstaller {
    @Override
    public boolean setup(Challenge challenge) {
        return false;
    }

    @Override
    public boolean clean(Challenge challenge) {
        return false;
    }

    @Override
    public String provider() {
        return "ROUTE53";
    }

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.Dns01;
    }
}
