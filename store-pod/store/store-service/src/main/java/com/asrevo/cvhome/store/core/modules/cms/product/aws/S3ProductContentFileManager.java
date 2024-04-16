package com.asrevo.cvhome.store.core.modules.cms.product.aws;

import com.asrevo.cvhome.s2s.model.CdnStorageProperties;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.file.ProductImageSize;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductAssetsManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Product content file manager with AWS S3
 *
 * @author carlsamson
 */
@Slf4j
@AllArgsConstructor
@Service
public class S3ProductContentFileManager
        implements ProductAssetsManager {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String ROOT_NAME = "products";
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';
    private final static String SMALL = "SMALL";
    private final static String LARGE = "LARGE";
    private final S3Client s3;
    private final CdnStorageProperties cdnStorageProperties;

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    @Override
    public List<OutputContentFile> getImages(String merchantStoreCode,
                                             FileContentType imageContentType) throws ServiceException {
        try {
            // get buckets
            String bucketName = bucketName();


            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(nodePath(merchantStoreCode))
                    .build();

            List<OutputContentFile> files = null;

            ListObjectsV2Response results = s3.listObjectsV2(listObjectsRequest);
            List<S3Object> objects = results.contents();
            for (S3Object os : objects) {
                if (files == null) {
                    files = new ArrayList<>();
                }
                String mimetype = URLConnection.guessContentTypeFromName(os.key());
                if (!StringUtils.isBlank(mimetype)) {

                    ResponseInputStream<GetObjectResponse> o = s3.getObject(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(os.key())
                            .build());

                    byte[] byteArray = IOUtils.toByteArray(o);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(byteArray.length);
                    baos.write(byteArray, 0, byteArray.length);
                    OutputContentFile ct = new OutputContentFile();
                    ct.setFile(baos);
                    files.add(ct);
                }
            }

            return files;
        } catch (final Exception e) {
            log.error("Error while getting files", e);
            throw new ServiceException(e);

        }
    }

    @Override
    public void removeImages(String merchantStoreCode) throws ServiceException {
        try {
            // get buckets
            String bucketName = bucketName();
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(nodePath(merchantStoreCode))
                    .build());

            log.info("Remove folder");
        } catch (final Exception e) {
            log.error("Error while removing folder", e);
            throw new ServiceException(e);

        }

    }

    @Override
    public void removeProductImage(ProductImage productImage) throws ServiceException {
        try {
            // get buckets
            String bucketName = bucketName();
            String key = nodePath(productImage.getProduct().getMerchantStore().getCode(),
                    productImage.getProduct().getSku()) + productImage.getProductImage();
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

            log.info("Remove file");
        } catch (final Exception e) {
            log.error("Error while removing file", e);
            throw new ServiceException(e);

        }

    }

    @Override
    public void removeProductImages(Product product) throws ServiceException {
        try {
            // get buckets
            String bucketName = bucketName();


            String key = nodePath(product.getMerchantStore().getCode(), product.getSku());

            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

            log.info("Remove file");
        } catch (final Exception e) {
            log.error("Error while removing file", e);
            throw new ServiceException(e);

        }

    }

    @Override
    public OutputContentFile getProductImage(String merchantStoreCode, String productCode,
                                             String imageName) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputContentFile getProductImage(String merchantStoreCode, String productCode,
                                             String imageName, ProductImageSize size) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputContentFile getProductImage(ProductImage productImage) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<OutputContentFile> getImages(Product product) throws ServiceException {
        return null;
    }

    @Override
    public void addProductImage(ProductImage productImage, ImageContentFile contentImage)
            throws ServiceException {


        try {
            // get buckets
            String bucketName = bucketName();


            String nodePath = this.nodePath(productImage.getProduct().getMerchantStore().getCode(),
                    productImage.getProduct().getSku(), contentImage);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(nodePath + contentImage.getFileName())
                    .metadata(Map.of("content-type", contentImage.getMimeType()))
                    .build();
//            putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);

            s3.putObject(putObjectRequest, RequestBody.fromBytes(IOUtils.toByteArray(contentImage.getFile())));


            log.info("Product add file");

        } catch (final Exception e) {
            log.error("Error while removing file", e);
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
            s3.createBucket(CreateBucketRequest.builder()
                    .bucket(bucket_name)
                    .build());
        } catch (Exception e) {
            log.error("error creating bucket", e);
        }
        return getBucket(bucket_name);
    }

    private String bucketName() {
        return this.cdnStorageProperties.bucket();
    }

    private String nodePath(String store) {
        return new StringBuilder().append(ROOT_NAME).append(Constants.SLASH).append(store)
                .append(Constants.SLASH).toString();
    }

    private String nodePath(String store, String product) {

        StringBuilder sb = new StringBuilder();
        // node path
        String nodePath = nodePath(store);
        sb.append(nodePath);

        // product path
        sb.append(product).append(Constants.SLASH);
        return sb.toString();

    }

    private String nodePath(String store, String product, ImageContentFile contentImage) {

        StringBuilder sb = new StringBuilder();
        // node path
        String nodePath = nodePath(store, product);
        sb.append(nodePath);

        // small large
        if (contentImage.getFileContentType().name().equals(FileContentType.PRODUCT.name())) {
            sb.append(SMALL);
        } else if (contentImage.getFileContentType().name().equals(FileContentType.PRODUCTLG.name())) {
            sb.append(LARGE);
        }

        return sb.append(Constants.SLASH).toString();


    }
}
