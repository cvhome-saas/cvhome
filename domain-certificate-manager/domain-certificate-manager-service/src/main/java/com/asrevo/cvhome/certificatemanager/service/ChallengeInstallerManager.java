package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.domain.challenges.Challenge;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ChallengeInstallerManager {
    private final List<ChallengeInstaller> installers;

    public void install(Challenge challenge) {
        List<ChallengeInstaller> installed = installers.stream().filter(it -> it.type().equals(challenge.type())).filter(it -> it.setup(challenge)).toList();
        if (!installed.isEmpty()) {
            List<String> setupOnProviders = installed.stream().map(ChallengeInstaller::provider).toList();
            log.info(setupOnProviders.toString());
        } else {
            log.info("");
        }
    }

    public void clean(Challenge challenge) {
        List<ChallengeInstaller> installed = installers.stream().filter(it -> it.type().equals(challenge.type())).filter(it -> it.clean(challenge)).toList();
        if (!installed.isEmpty()) {
            List<String> cleanOnProviders = installed.stream().map(ChallengeInstaller::provider).toList();
            log.info(cleanOnProviders.toString());
        } else {
            log.info("");
        }
    }
}
