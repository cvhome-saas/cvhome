package com.asrevo.cvhome.domaincertificatemanager.service;

import com.asrevo.cvhome.domaincertificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.HttpChallenge;

import java.io.InputStream;

public interface HttpChallengeVerifyService {
    boolean create(HttpChallenge challenge);

    InputStream getValidationFile(HttpValidationToken token);
}