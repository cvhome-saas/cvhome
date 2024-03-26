package com.asrevo.cvhome.dcm.service.installers.http;

import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.dcm.domain.challenges.ChallengeInstall;
import com.asrevo.cvhome.dcm.domain.challenges.HttpChallenge;
import com.asrevo.cvhome.dcm.service.ChallengeInstaller;
import com.asrevo.cvhome.dcm.service.verify.http.HttpChallengeVerifyServiceProvider;
import org.springframework.stereotype.Service;

@Service
public class HttpChallengeInstaller implements ChallengeInstaller {
    private final HttpChallengeVerifyServiceProvider provider;


    public HttpChallengeInstaller(HttpChallengeVerifyServiceProvider provider) {
        this.provider = provider;
    }

    @Override
    public synchronized boolean setup(ChallengeInstall c) {
        return c.challenges()
                .stream()
                .map(it -> ((HttpChallenge) it))
                .allMatch(it -> provider.getInstance(it).createHttpVerifyFile(it));
    }

    @Override
    public boolean clean(ChallengeInstall c) {
        return c.challenges()
                .stream()
                .map(it -> ((HttpChallenge) it))
                .allMatch(it -> provider.getInstance(it).clean(it));
    }


    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.Http01;
    }
}
