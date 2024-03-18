package com.asrevo.cvhome.dcm.service.verify.tlsalpn01;

import com.asrevo.cvhome.dcm.domain.challenges.TlsAlpnChallenge;
import com.asrevo.cvhome.dcm.service.acm.AcmFileService;
import org.shredzone.acme4j.util.CertificateUtils;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

public class TlsAlpn01ChallengeVerifyServiceImpl implements TlsAlpn01ChallengeVerifyService {
    private final AcmFileService acmFileService;

    public TlsAlpn01ChallengeVerifyServiceImpl(AcmFileService acmFileService) {
        this.acmFileService = acmFileService;
    }

    @Override
    public boolean createCertificateVerifyFile(TlsAlpnChallenge it) {
        try {
            KeyPair keyPair = acmFileService.generateOrGetKeyPair(it.domain());
            byte[] decode = it.decode();
            assert decode != null;
            X509Certificate cert = CertificateUtils.createTlsAlpn01Certificate(keyPair, it.identifier(), decode);
            acmFileService.storeCertificate(it.domain(), cert);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean clean(TlsAlpnChallenge challenge) {
        return false;
    }
}