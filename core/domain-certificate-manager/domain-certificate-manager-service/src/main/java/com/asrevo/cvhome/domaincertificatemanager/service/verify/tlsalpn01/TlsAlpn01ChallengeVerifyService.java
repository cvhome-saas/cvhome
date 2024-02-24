package com.asrevo.cvhome.domaincertificatemanager.service.verify.tlsalpn01;

import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.TlsAlpnChallenge;

public interface TlsAlpn01ChallengeVerifyService {
    boolean createCertificateVerifyFile(TlsAlpnChallenge challenge);

    boolean clean(TlsAlpnChallenge challenge);
}