package com.asrevo.cvhome.domaincertificatemanager.service;

import com.asrevo.cvhome.domaincertificatemanager.config.DcmChallengesConfigProperties;
import com.asrevo.cvhome.domaincertificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.ChallengeInstall;
import com.asrevo.cvhome.domaincertificatemanager.service.impl.HttpChallengeVerifyServiceProvider;
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
    private final HttpChallengeVerifyServiceProvider httpVerifyProvider;
    private final DcmChallengesConfigProperties properties;


    public boolean install(ChallengeInstall challenge) {

        List<ChallengeInstaller> installed = installers.stream()
                .filter(it -> it.type().equals(challenge.challengeType()))
                .filter(it -> properties.providers(it.type()).contains(it.provider()))
                .toList();

        ChallengeInstaller appliedInstaller = null;

        for (ChallengeInstaller installer : installed) {
            boolean isSetup = installer.setup(challenge);
            if (isSetup) {
                appliedInstaller = installer;
                break;
            }
        }

        if (appliedInstaller != null) {
            log.info("successfully applied installer {} {}", appliedInstaller.provider(), challenge.domain());
            return true;
        } else {
            log.warn("did not find any installer for {} , {}", challenge.challengeType(), challenge.domain());
            return false;
        }
    }

    public void clean(ChallengeInstall challenge) {
        List<ChallengeInstaller> installed = installers.stream()
                .filter(it -> it.type().equals(challenge.challengeType())).toList();
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
        return httpVerifyProvider.getInstance().getValidationFile(validationToken);
    }
}
