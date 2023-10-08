package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.certificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.certificatemanager.domain.challenges.Challenge;
import com.asrevo.cvhome.certificatemanager.service.installers.HttpInstaller;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ChallengeInstallerManager {
    private final List<ChallengeInstaller> installers;

    public void install(Challenge challenge) {
        List<ChallengeInstaller> installed = installers.stream().filter(it -> it.type().equals(challenge.type())).toList();
        ChallengeInstaller appliedInstaller = null;
        for (ChallengeInstaller installer : installed) {
            boolean isSetup = installer.setup(challenge);
            if (isSetup) {
                appliedInstaller = installer;
                break;
            }
        }
        if (appliedInstaller != null) {
            log.info(appliedInstaller.provider());
        } else {
            log.info("");
        }
    }

    public void clean(Challenge challenge) {
        List<ChallengeInstaller> installed = installers.stream().filter(it -> it.type().equals(challenge.type())).toList();
        ChallengeInstaller appliedInstaller = null;
        for (ChallengeInstaller installer : installed) {
            boolean isCleaned = installer.clean(challenge);
            if (isCleaned) {
                appliedInstaller = installer;
                break;
            }
        }
        if (appliedInstaller != null) {
            log.info(appliedInstaller.provider());
        } else {
            log.info("");
        }
    }

    public InputStream getHttpValidationToken(HttpValidationToken validationToken) {
        List<HttpInstaller> httpInstallers = installers.stream().filter(it -> it.type().equals(ChallengeValidationType.Http01)).map(it -> ((HttpInstaller) it)).toList();
        InputStream stream = null;
        for (HttpInstaller httpInstaller : httpInstallers) {
            stream = httpInstaller.getHttpValidationFile(validationToken);
            if (stream != null) {
                break;
            }
        }
        return stream;
    }
}
