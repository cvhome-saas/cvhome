package com.asrevo.cvhome.certificatemanager.domain.challenges;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;

public interface Challenge {
    ChallengeValidationType type();

    boolean validate();

    boolean isWildCard();
}

