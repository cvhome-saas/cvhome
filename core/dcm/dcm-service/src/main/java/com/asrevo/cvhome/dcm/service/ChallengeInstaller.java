package com.asrevo.cvhome.dcm.service;


import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.dcm.domain.challenges.ChallengeInstall;

public interface ChallengeInstaller {
    boolean setup(ChallengeInstall challenge);

    boolean clean(ChallengeInstall challenge);

    ChallengeValidationType type();
}
