package com.asrevo.cvhome.domaincertificatemanager.service.verify.http;

import com.asrevo.cvhome.domaincertificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.HttpChallenge;
import com.asrevo.cvhome.domaincertificatemanager.service.storage.FileService;
import com.asrevo.cvhome.domaincertificatemanager.service.storage.impl.LocalFileServiceImpl;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalHttpChallengeVerifyServiceImpl implements HttpChallengeVerifyService {
    private final FileService fileService;
    private final Path tokenRoot;

    public LocalHttpChallengeVerifyServiceImpl() {
        fileService = new LocalFileServiceImpl();
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
    public boolean createHttpVerifyFile(HttpChallenge challenge) {
        try {
            File http01Temp = Files.createTempFile("domain", ".http01").toFile();
            FileWriter http01FileWriter = new FileWriter(http01Temp);
            http01FileWriter.append(challenge.authorization());
            http01FileWriter.flush();
            http01FileWriter.close();
            Path tokenPath = tokenRoot.resolve(challenge.token());
            fileService.upload(http01Temp, tokenPath.toString());
            http01Temp.delete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public InputStream readVerifyFile(HttpValidationToken token) {
        return fileService.getFile(tokenRoot.resolve(token.encoded()).toString());
    }

    @Override
    public boolean clean(HttpChallenge challenge) {
        return false;
    }
}
