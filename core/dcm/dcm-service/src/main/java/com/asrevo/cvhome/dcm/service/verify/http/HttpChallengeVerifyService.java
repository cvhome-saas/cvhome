package com.asrevo.cvhome.dcm.service.verify.http;

import com.asrevo.cvhome.dcm.domain.HttpValidationToken;
import com.asrevo.cvhome.dcm.domain.challenges.HttpChallenge;

import java.io.InputStream;

public interface HttpChallengeVerifyService {
    boolean createHttpVerifyFile(HttpChallenge challenge);

    InputStream readVerifyFile(HttpValidationToken token);

    boolean clean(HttpChallenge challenge);
}