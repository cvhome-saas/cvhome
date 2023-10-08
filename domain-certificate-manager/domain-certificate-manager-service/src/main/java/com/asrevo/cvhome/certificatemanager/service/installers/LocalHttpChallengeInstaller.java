package com.asrevo.cvhome.certificatemanager.service.installers;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.certificatemanager.domain.challenges.Challenge;
import com.asrevo.cvhome.certificatemanager.domain.challenges.HttpChallenge;
import com.asrevo.cvhome.certificatemanager.service.ChallengeInstaller;
import com.asrevo.cvhome.certificatemanager.service.impl.LocalAcmFileServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LocalHttpChallengeInstaller implements ChallengeInstaller {
    private final LocalAcmFileServiceImpl acmFileService;

    @Override
    public boolean setup(Challenge challenge) {
        try {
            acmFileService.generateValidationFile((HttpChallenge) challenge);
            return true;
        } catch (Exception ignored) {
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
        return ChallengeValidationType.Http01;
    }
}
