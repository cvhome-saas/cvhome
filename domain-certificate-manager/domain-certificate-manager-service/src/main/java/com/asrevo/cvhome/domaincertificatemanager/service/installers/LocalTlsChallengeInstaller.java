package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.Challenge;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.TlsAlpnChallenge;
import com.asrevo.cvhome.domaincertificatemanager.service.ChallengeInstaller;
import com.asrevo.cvhome.domaincertificatemanager.service.impl.LocalAcmFileServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.util.CertificateUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

@Service
@AllArgsConstructor
@Slf4j
public class LocalTlsChallengeInstaller implements ChallengeInstaller {
    private final LocalAcmFileServiceImpl localAcmFileService;

    @Override
    public boolean setup(Challenge c) {
        try {
            TlsAlpnChallenge challenge = (TlsAlpnChallenge) c;
            KeyPair keyPair = localAcmFileService.generateOrGetKeyPair(challenge.domain());
            byte[] decode = challenge.decode();
            assert decode != null;
            X509Certificate cert = CertificateUtils.createTlsAlpn01Certificate(keyPair, challenge.identifier(), decode);
            localAcmFileService.storeCertificate(challenge.domain(), cert);
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
