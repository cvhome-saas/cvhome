package com.asrevo.cvhome.domaincertificatemanager.service.installers.tlsalpn01;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ChallengeInstall;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.TlsAlpnChallenge;
import com.asrevo.cvhome.domaincertificatemanager.service.ChallengeInstaller;
import com.asrevo.cvhome.domaincertificatemanager.service.verify.tlsalpn01.TlsAlpn01ChallengeVerifyServiceProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@AllArgsConstructor
@Slf4j
@Service
public class TlsAlpn01ChallengeInstaller implements ChallengeInstaller {
    private final TlsAlpn01ChallengeVerifyServiceProvider provider;

    @Override
    public synchronized boolean setup(ChallengeInstall c) {
        return c.challenges()
                .stream()
                .map(it -> ((TlsAlpnChallenge) it))
                .allMatch(it -> provider.getInstance(it).createCertificateVerifyFile(it));
    }

    @Override
    public boolean clean(ChallengeInstall c) {
        return c.challenges()
                .stream()
                .map(it -> ((TlsAlpnChallenge) it))
                .allMatch(it -> provider.getInstance(it).clean(it));
    }

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.TlsAlpn01;
    }
}
