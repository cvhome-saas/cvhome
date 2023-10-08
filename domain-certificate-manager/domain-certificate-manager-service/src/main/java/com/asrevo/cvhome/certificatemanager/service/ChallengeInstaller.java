package com.asrevo.cvhome.certificatemanager.service;


import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.certificatemanager.domain.challenges.Challenge;

public interface ChallengeInstaller {
    boolean setup(Challenge challenge);

    boolean clean(Challenge challenge);

    String provider();

    ChallengeValidationType type();
}
