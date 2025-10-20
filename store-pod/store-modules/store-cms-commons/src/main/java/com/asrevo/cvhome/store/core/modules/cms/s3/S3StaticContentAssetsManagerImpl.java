package com.asrevo.cvhome.store.core.modules.cms.s3;

import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.content.ContentAssetsManager;
import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Static content management with S3
 *
 * @author carlsamson
 */
@Slf4j
public class S3StaticContentAssetsManagerImpl implements ContentAssetsManager {

	@Serial
	private static final long serialVersionUID = 1L;

	private final S3Client s3;

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
		}
		catch (final Exception e) {
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
		}
		catch (final Exception e) {
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
		}
		catch (final Exception e) {
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
		}
		catch (final Exception e) {
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
		}
		catch (final Exception e) {
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
		}
		catch (final Exception e) {
			log.error("Error while removing folder", e);
			throw new ServiceException(e);
		}
	}

	private Bucket getBucket(String bucket_name) {

		Bucket named_bucket = null;
		ListBucketsResponse listBucketsResponse = s3.listBuckets();
		for (Bucket b : listBucketsResponse.buckets()) {
			if (b.name().equals(bucket_name)) {
				named_bucket = b;
			}
		}

		if (named_bucket == null) {
			named_bucket = createBucket(bucket_name);
		}

		return named_bucket;
	}

	private Bucket createBucket(String bucket_name) {
		try {
			s3.createBucket(CreateBucketRequest.builder().bucket(bucket_name).build());
		}
		catch (Exception e) {
			log.error("error creating bucket", e);
		}
		return getBucket(bucket_name);
	}

	@Override
	public void addFolder(String merchantStoreCode, String folderName, Optional<String> folderPath) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeFolder(String merchantStoreCode, String folderName, Optional<String> folderPath) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> listFolders(String merchantStoreCode, Optional<String> path) {
		// TODO Auto-generated method stub
		return null;
	}

	private String bucketName() {
		return bucket;
	}

}
