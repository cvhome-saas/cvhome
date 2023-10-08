package com.asrevo.cvhome.certificatemanager.service.installers;

import com.asrevo.cvhome.certificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.certificatemanager.service.ChallengeInstaller;

import java.io.InputStream;

public interface HttpInstaller extends ChallengeInstaller {
    InputStream getHttpValidationFile(HttpValidationToken token);
}
