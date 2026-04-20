package com.asrevo.cvhome.store.core.modules.cms.s3;

import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.content.ContentAssetsManager;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import lombok.extern.slf4j.Slf4j;

/**
 * Static content management with S3
 *
 * @author carlsamson
 */
@Slf4j
public class S3StaticContentAssetsManagerImpl implements ContentAssetsManager {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient S3Client s3;

    private final String bucket;

    public S3StaticContentAssetsManagerImpl(S3Client s3Client, String bucket) {
        this.s3 = s3Client;
        this.bucket = bucket;
    }

    @Override
    public OutputContentFile getFile(String merchantStoreCode, Optional<String> folderPath,
                                     FileContentType fileContentType, String contentName) throws ServiceException {
        try {
            // get buckets
            String bucketName = bucketName();
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(nodePath(merchantStoreCode, fileContentType) + contentName)
                    .build();
            ResponseInputStream<GetObjectResponse> o = s3.getObject(getObjectRequest);

            log.info("Content getFile");
            return getOutputContentFile(IOUtils.toByteArray(o));
        } catch (final Exception e) {
            log.error("Error while getting file", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public List<String> getFileNames(String merchantStoreCode, Optional<String> folderPath,
                                     FileContentType fileContentType) throws ServiceException {
        try {
            // get buckets
            String bucketName = bucketName();

            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(nodePath(merchantStoreCode, fileContentType))
                    .build();

            List<String> fileNames = null;

            ListObjectsV2Response listObjectsV2Response = s3.listObjectsV2(listObjectsRequest);

            List<S3Object> contents = listObjectsV2Response.contents();
            for (S3Object os : contents) {
                if (isInsideSubFolder(os.key())) {
                    continue;
                }
                if (fileNames == null) {
                    fileNames = new ArrayList<>();
                }
                String mimetype = URLConnection.guessContentTypeFromName(os.key());
                if (!StringUtils.isBlank(mimetype)) {
                    fileNames.add(getName(os.key()));
                }
            }

            log.info("Content get file names");
            return fileNames;
        } catch (final Exception e) {
            log.error("Error while getting file names", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public List<OutputContentFile> getFiles(String merchantStoreCode, Optional<String> folderPath,
                                            FileContentType fileContentType) throws ServiceException {
        try {
            // get buckets
            String bucketName = bucketName();

            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(nodePath(merchantStoreCode, fileContentType))
                    .build();

            List<OutputContentFile> files = null;

            ListObjectsV2Response listObjectsV2Response = s3.listObjectsV2(listObjectsRequest);
            List<S3Object> objects = listObjectsV2Response.contents();
            for (S3Object os : objects) {
                if (files == null) {
                    files = new ArrayList<>();
                }
                String mimetype = URLConnection.guessContentTypeFromName(os.key());
                if (!StringUtils.isBlank(mimetype)) {
                    ResponseInputStream<GetObjectResponse> o = s3
                            .getObject(GetObjectRequest.builder().bucket(bucketName).key(os.key()).build());
                    byte[] byteArray = IOUtils.toByteArray(o);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(byteArray.length);
                    baos.write(byteArray, 0, byteArray.length);
                    OutputContentFile ct = new OutputContentFile();
                    ct.setFile(baos);
                    files.add(ct);
                }
            }

            log.info("Content getFiles");
            return files;
        } catch (final Exception e) {
            log.error("Error while getting files", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void addFile(String merchantStoreCode, Optional<String> folderPath, InputContentFile inputStaticContentData)
            throws ServiceException {

        try {
            // get buckets
            String bucketName = bucketName();

            String nodePath = nodePath(merchantStoreCode, inputStaticContentData.getFileContentType());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(nodePath + inputStaticContentData.getFileName())
                    .metadata(Map.of("content-type", inputStaticContentData.getMimeType()))
                    .build();

            s3.putObject(putObjectRequest,
                    RequestBody.fromBytes(IOUtils.toByteArray(inputStaticContentData.getFile())));

            log.info("Content add file");
        } catch (final Exception e) {
            log.error("Error while adding file", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void addFiles(String merchantStoreCode, Optional<String> folderPath,
                         List<InputContentFile> inputStaticContentDataList) throws ServiceException {

        if (inputStaticContentDataList != null && !inputStaticContentDataList.isEmpty()) {
            for (InputContentFile inputFile : inputStaticContentDataList) {
                this.addFile(merchantStoreCode, folderPath, inputFile);
            }
        }
    }

    @Override
    public void removeFile(String merchantStoreCode, FileContentType staticContentType, String fileName,
                           Optional<String> folderPath) throws ServiceException {

        try {
            // get buckets
            String bucketName = bucketName();

            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(nodePath(merchantStoreCode, staticContentType) + fileName)
                    .build());

            log.info("Remove file");
        } catch (final Exception e) {
            log.error("Error while removing file", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void removeFiles(String merchantStoreCode, Optional<String> folderPath) throws ServiceException {

        try {
            // get buckets
            String bucketName = bucketName();

            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(nodePath(merchantStoreCode)).build());

            log.info("Remove folder");
        } catch (final Exception e) {
            log.error("Error while removing folder", e);
            throw new ServiceException(e);
        }
    }

    private Bucket getBucket(String bucketName) {

        Bucket namedBucket = null;
        ListBucketsResponse listBucketsResponse = s3.listBuckets();
        for (Bucket b : listBucketsResponse.buckets()) {
            if (b.name().equals(bucketName)) {
                namedBucket = b;
            }
        }

        if (namedBucket == null) {
            namedBucket = createBucket(bucketName);
        }

        return namedBucket;
    }

    private Bucket createBucket(String bucketName) {
        try {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("error creating bucket", e);
        }
        return getBucket(bucketName);
    }

    @Override
    public void addFolder(String merchantStoreCode, String folderName, Optional<String> folderPath) {


    }

    @Override
    public void removeFolder(String merchantStoreCode, String folderName, Optional<String> folderPath) {


    }

    @Override
    public List<String> listFolders(String merchantStoreCode, Optional<String> path) {
        return List.of();
    }

    private String bucketName() {
        return bucket;
    }

}
