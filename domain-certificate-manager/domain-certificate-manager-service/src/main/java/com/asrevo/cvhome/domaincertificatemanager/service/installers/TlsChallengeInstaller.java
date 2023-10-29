package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.config.InstallersConfigProperties.InstallerProvider;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ChallengeInstall;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.TlsAlpnChallenge;
import com.asrevo.cvhome.domaincertificatemanager.service.ChallengeInstaller;
import com.asrevo.cvhome.domaincertificatemanager.service.impl.AcmFileServiceProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.util.CertificateUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

@AllArgsConstructor
@Slf4j
public abstract class TlsChallengeInstaller implements ChallengeInstaller {
    private final AcmFileServiceProvider provider;

    @Override
    public synchronized boolean setup(ChallengeInstall c) {
        return c.challenges()
                .stream()
                .map(it -> ((TlsAlpnChallenge) it))
                .allMatch(this::installTlsAlpnChallenge);
    }

    private boolean installTlsAlpnChallenge(TlsAlpnChallenge challenge) {
        try {
            KeyPair keyPair = provider.getInstance().generateOrGetKeyPair(challenge.domain());
            byte[] decode = challenge.decode();
            assert decode != null;
            X509Certificate cert = CertificateUtils.createTlsAlpn01Certificate(keyPair, challenge.identifier(), decode);
            provider.getInstance().storeCertificate(challenge.domain(), cert);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean clean(ChallengeInstall challenge) {
        return false;
    }

    @Override
    public InstallerProvider provider() {
        return InstallerProvider.LOCAL;
    }

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.TlsAlpn01;
    }
}
