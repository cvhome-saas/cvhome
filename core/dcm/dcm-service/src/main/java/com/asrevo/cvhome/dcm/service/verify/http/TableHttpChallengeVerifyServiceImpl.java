package com.asrevo.cvhome.dcm.service.verify.http;

import com.asrevo.cvhome.dcm.domain.HttpValidationToken;
import com.asrevo.cvhome.dcm.domain.challenges.HttpChallenge;
import com.asrevo.cvhome.dcm.repository.FilesRepository;
import com.asrevo.cvhome.dcm.service.storage.FileService;
import com.asrevo.cvhome.dcm.service.storage.impl.TableFileServiceImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;

public class TableHttpChallengeVerifyServiceImpl implements HttpChallengeVerifyService {
    private final FileService fileService;

    public TableHttpChallengeVerifyServiceImpl(FilesRepository filesRepository) {
        fileService = new TableFileServiceImpl(filesRepository);
    }


    @Override
    public boolean createHttpVerifyFile(HttpChallenge challenge) {
        try {
            File http01Temp = Files.createTempFile("domain", ".http01").toFile();
            FileWriter http01FileWriter = new FileWriter(http01Temp);
            http01FileWriter.append(challenge.authorization());
            http01FileWriter.flush();
            http01FileWriter.close();
            fileService.upload(http01Temp, challenge.token());
            http01Temp.delete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public InputStream readVerifyFile(HttpValidationToken token) {
        return fileService.getFile(token.encoded());
    }

    @Override
    public boolean clean(HttpChallenge challenge) {
        return false;
    }
}