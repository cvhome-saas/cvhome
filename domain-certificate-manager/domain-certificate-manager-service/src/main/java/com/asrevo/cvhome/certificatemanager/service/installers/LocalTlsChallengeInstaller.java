package com.asrevo.cvhome.certificatemanager.service.installers;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.certificatemanager.domain.challenges.Challenge;
import com.asrevo.cvhome.certificatemanager.service.ChallengeInstaller;
import org.springframework.stereotype.Service;

@Service
public class LocalTlsChallengeInstaller implements ChallengeInstaller {
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
        return "LOCAL";
    }

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.TlsAlpn01;
    }
}
