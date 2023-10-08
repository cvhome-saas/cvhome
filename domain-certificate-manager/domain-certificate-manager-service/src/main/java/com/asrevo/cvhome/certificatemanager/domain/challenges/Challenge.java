package com.asrevo.cvhome.certificatemanager.domain.challenges;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;

public interface Challenge {
    Domain domain();

    ChallengeValidationType type();

    boolean validate();

    boolean isWildCard();
}

