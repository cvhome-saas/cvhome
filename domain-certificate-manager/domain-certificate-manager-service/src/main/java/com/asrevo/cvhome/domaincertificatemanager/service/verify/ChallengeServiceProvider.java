package com.asrevo.cvhome.domaincertificatemanager.service.verify;

import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.Challenge;

public interface ChallengeServiceProvider<T extends Challenge, R> {
    R getInstance(T t);
}