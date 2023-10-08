package com.asrevo.cvhome.certificatemanager.service.installers;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.certificatemanager.domain.challenges.Challenge;
import com.asrevo.cvhome.certificatemanager.domain.challenges.TlsAlpnChallenge;
import com.asrevo.cvhome.certificatemanager.service.ChallengeInstaller;
import com.asrevo.cvhome.certificatemanager.service.impl.LocalAcmFileServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AllArgsConstructor
@Slf4j
public class LocalTlsChallengeInstaller implements ChallengeInstaller {
    private final LocalAcmFileServiceImpl localAcmFileService;

    @Override
    public boolean setup(Challenge challenge) {
        try {
            localAcmFileService.generateCertificate(((TlsAlpnChallenge) challenge));
            return true;
        } catch (IOException e) {
            return false;
        }
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
