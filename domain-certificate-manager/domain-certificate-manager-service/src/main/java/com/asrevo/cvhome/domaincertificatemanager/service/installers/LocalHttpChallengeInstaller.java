package com.asrevo.cvhome.domaincertificatemanager.service.installers;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.Challenge;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.HttpChallenge;
import com.asrevo.cvhome.domaincertificatemanager.service.impl.LocalFileServiceImpl;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalHttpChallengeInstaller implements HttpInstaller {
    private final Path tokenRoot;
    private final LocalFileServiceImpl localFileService;

    public LocalHttpChallengeInstaller(LocalFileServiceImpl localFileService) {
        this.localFileService = localFileService;
        this.tokenRoot = createTokenBaseDirectory();
    }

    @SneakyThrows
    private static void createParentDirectory(Path destination) {
        if (!destination.getParent().toFile().exists()) {
            Files.createDirectories(destination.getParent());
        }
    }

    private Path createTokenBaseDirectory() {
        Path root = Paths.get(System.getProperty("user.home"), "cvhome/domain-certificate-manager/token");
        createParentDirectory(root);
        return root;
    }

    @Override
    public boolean setup(Challenge c) {
        try {
            HttpChallenge challenge = (HttpChallenge) c;
            File http01Temp = Files.createTempFile("domain", ".http01").toFile();
            FileWriter http01FileWriter = new FileWriter(http01Temp);
            http01FileWriter.append(challenge.authorization());
            http01FileWriter.flush();
            http01FileWriter.close();
            Path tokenPath = tokenRoot.resolve(challenge.token());
            localFileService.upload(http01Temp, tokenPath.toString());
            http01Temp.delete();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public boolean clean(Challenge challenge) {
        return false;
    }

    @Override
    public String provider() {
        return "LOCAL";
    }

    @Override
    public ChallengeValidationType type() {
        return ChallengeValidationType.Http01;
    }

    @Override
    public InputStream getHttpValidationFile(HttpValidationToken token) {
        return localFileService.getFile(tokenRoot.resolve(token.encoded()).toString());
    }
}
