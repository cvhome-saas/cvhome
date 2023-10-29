package com.asrevo.cvhome.domaincertificatemanager.domain.challenges;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;

import java.util.List;

public record ChallengeInstall(List<Challenge> challenges) {

    public ChallengeValidationType challengeType() {
        if (challenges.isEmpty()) {
            return ChallengeValidationType.Dns01;
        } else if (challenges.size() == 1) {
            return challenges.get(0).type();
        } else {
            return challenges.stream()
                    .filter(Challenge::isWildCard)
                    .findFirst()
                    .map(Challenge::type)
                    .orElseGet(() -> challenges.get(0).type());
        }
    }

    public String domains() {
        return this.challenges.stream().findFirst().map(it -> it.domain().domain()).orElse("");
    }
}