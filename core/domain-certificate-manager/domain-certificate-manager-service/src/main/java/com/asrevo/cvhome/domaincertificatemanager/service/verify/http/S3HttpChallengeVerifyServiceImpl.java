package com.asrevo.cvhome.domaincertificatemanager.service.verify.http;

import com.asrevo.cvhome.domaincertificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.domaincertificatemanager.domain.challenges.HttpChallenge;
import com.asrevo.cvhome.domaincertificatemanager.service.storage.FileService;
import com.asrevo.cvhome.domaincertificatemanager.service.storage.impl.S3FileServiceImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.asrevo.cvhome.domaincertificatemanager.domain.S3KeyPath.resolveS3Uri;

public class S3HttpChallengeVerifyServiceImpl implements HttpChallengeVerifyService {
    private final FileService fileService;
    private final String tokenStorageBucket;
    private Path tokenRoot;

    public S3HttpChallengeVerifyServiceImpl(String tokenStorageBucket) {
        this.tokenStorageBucket = tokenStorageBucket;
        fileService = new S3FileServiceImpl();
        this.init();
    }

    private void init() {
        tokenRoot = Paths.get(tokenStorageBucket, "token");
    }

    @Override
    public boolean createHttpVerifyFile(HttpChallenge challenge) {
        try {
            File http01Temp = Files.createTempFile("domain", ".http01").toFile();
            FileWriter http01FileWriter = new FileWriter(http01Temp);
            http01FileWriter.append(challenge.authorization());
            http01FileWriter.flush();
            http01FileWriter.close();
            String fileName = resolveS3Uri(challenge.token(), this.tokenRoot);
            fileService.upload(http01Temp, fileName);
            http01Temp.delete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public InputStream readVerifyFile(HttpValidationToken token) {
        String fileName = resolveS3Uri(token.encoded(), this.tokenRoot);
        return fileService.getFile(fileName);
    }

    @Override
    public boolean clean(HttpChallenge challenge) {
        return false;
    }
}
