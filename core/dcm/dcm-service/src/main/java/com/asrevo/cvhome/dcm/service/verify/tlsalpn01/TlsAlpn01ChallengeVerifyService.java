package com.asrevo.cvhome.dcm.service.verify.tlsalpn01;

import com.asrevo.cvhome.dcm.domain.challenges.TlsAlpnChallenge;

public interface TlsAlpn01ChallengeVerifyService {
    boolean createCertificateVerifyFile(TlsAlpnChallenge challenge);

    boolean clean(TlsAlpnChallenge challenge);
}