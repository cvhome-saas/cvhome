package com.asrevo.cvhome.domaincertificatemanager.service;


import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ChallengeInstall;

public interface ChallengeInstaller {
    boolean setup(ChallengeInstall challenge);

    boolean clean(ChallengeInstall challenge);

    String provider();

    ChallengeValidationType type();
}
