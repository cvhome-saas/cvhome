package com.asrevo.cvhome.domaincertificatemanager.domain.challenges;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;

public interface Challenge {
    Domain domain();

    ChallengeValidationType type();

    boolean validate();

    boolean isWildCard();
}

