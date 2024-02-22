package com.asrevo.cvhome.domaincertificatemanager.service.verify.http;

import com.asrevo.cvhome.domaincertificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.HttpChallenge;

import java.io.InputStream;

public interface HttpChallengeVerifyService {
    boolean createHttpVerifyFile(HttpChallenge challenge);

    InputStream readVerifyFile(HttpValidationToken token);

    boolean clean(HttpChallenge challenge);
}