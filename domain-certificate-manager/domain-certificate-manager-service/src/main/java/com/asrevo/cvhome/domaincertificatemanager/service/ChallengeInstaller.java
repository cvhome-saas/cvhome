package com.asrevo.cvhome.domaincertificatemanager.service;


import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.Challenge;

public interface ChallengeInstaller {
    boolean setup(Challenge challenge);

    boolean clean(Challenge challenge);

    String provider();

    ChallengeValidationType type();
}
