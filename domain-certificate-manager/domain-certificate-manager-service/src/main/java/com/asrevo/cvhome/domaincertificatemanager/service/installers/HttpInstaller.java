package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.domaincertificatemanager.service.ChallengeInstaller;

import java.io.InputStream;

public interface HttpInstaller extends ChallengeInstaller {
    InputStream getHttpValidationFile(HttpValidationToken token);
}
