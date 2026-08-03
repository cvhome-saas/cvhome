package com.asrevo.cvhome.store.core.modules.cms.product;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.asrevo.cvhome.store.core.entity.catalog.product.file.ProductImageSize;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetDeleteFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetListFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetNotFoundException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetReadFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.ImageSizeMisconfiguredException;
import com.asrevo.cvhome.store.core.modules.cms.errors.ImageUnreadableException;
import com.asrevo.cvhome.store.core.modules.cms.model.CmsProductImage;
import com.asrevo.cvhome.store.core.modules.cms.utils.ProductImageCropUtils;
import com.asrevo.cvhome.store.core.modules.cms.utils.ProductImageSizeUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Slf4j
public class ProductFileManagerImpl extends ProductFileManager {

    private static final String BAD_SIZE_LOG =
            "Product image size configured as [PRODUCT_IMAGE_WIDTH_SIZE] {} x [PRODUCT_IMAGE_HEIGHT_SIZE] {}";

    private final ProductImagePut uploadImage;

    private final ProductImageGet getImage;

    private final ProductImageRemove removeImage;

    private final boolean cropUploads;

    private final String height;

    private final String width;

    public ProductFileManagerImpl(ProductImagePut uploadImage, ProductImageGet getImage, ProductImageRemove removeImage,
                                  boolean cropUploads, String height, String width) {
        this.uploadImage = uploadImage;
        this.getImage = getImage;
        this.removeImage = removeImage;
        this.cropUploads = cropUploads;
        this.height = height;
        this.width = width;
    }

    public void addProductImage(CmsProductImage productImage, ImageContentFile contentImage)
            throws AssetUploadFailedException, ImageUnreadableException, ImageSizeMisconfiguredException {

        try {

            byte[] byteArray = IOUtils.toByteArray(contentImage.getFile());
            InputStream is1 = new ByteArrayInputStream(byteArray);

            contentImage.setFile(is1);

            // upload original -- L
            contentImage.setFileContentType(FileContentType.PRODUCTLG);
            String mimeType = URLConnection.guessContentTypeFromName(contentImage.getFileName());
            contentImage.setMimeType(mimeType);
            uploadImage.addProductImage(productImage, contentImage);

            // Resizes
            if (!StringUtils.isBlank(height) && !StringUtils.isBlank(width)) {
                uploadResizedImage(productImage, contentImage, byteArray);
            } else {
                uploadOriginalAsProductImage(productImage, contentImage, byteArray);
            }

        } catch (IOException e) {
            // Only the local stream and temp-file handling lands here; a rejected upload already arrives as
            // AssetUploadFailedException from uploadImage and passes through with its own key.
            throw AssetUploadFailedException.of(productImage.getProductImage(), e);
        }
    }

    private void uploadOriginalAsProductImage(CmsProductImage productImage, ImageContentFile contentImage,
                                              byte[] byteArray) throws AssetUploadFailedException, ImageUnreadableException,
            ImageSizeMisconfiguredException {
        contentImage.setFileContentType(FileContentType.PRODUCT);
        InputStream is2 = new ByteArrayInputStream(byteArray);
        contentImage.setFile(is2);
        uploadImage.addProductImage(productImage, contentImage);
    }

    private void uploadResizedImage(CmsProductImage productImage, ImageContentFile contentImage, byte[] byteArray)
            throws AssetUploadFailedException, ImageUnreadableException, ImageSizeMisconfiguredException, IOException {
        String extension = resolveExtension(contentImage);

        int largeImageHeight = parseDimension(height);
        int largeImageWidth = parseDimension(width);

        if (largeImageHeight <= 0 || largeImageWidth <= 0) {
            log.error(BAD_SIZE_LOG, width, height);
            throw ImageSizeMisconfiguredException.of(width, height);
        }

        BufferedImage largeResizedImage = buildResizedImage(productImage, byteArray, largeImageWidth, largeImageHeight);

        File tempLarge = File.createTempFile(
                new StringBuilder().append(productImage.getId()).append("tmpLarge").toString(),
                String.format(".%s", extension));
        ImageIO.write(largeResizedImage, extension, tempLarge);

        try (FileInputStream isLarge = new FileInputStream(tempLarge)) {

            ImageContentFile largeContentImage = new ImageContentFile();
            largeContentImage.setFileContentType(FileContentType.PRODUCT);
            largeContentImage.setFileName(productImage.getProductImage());
            largeContentImage.setFile(isLarge);

            uploadImage.addProductImage(productImage, largeContentImage);

            Files.delete(tempLarge.toPath());

        }
    }

    private String resolveExtension(ImageContentFile contentImage) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        String contentType = fileNameMap.getContentTypeFor(contentImage.getFileName());
        String extension = null;
        if (contentType != null) {
            extension = contentType.substring(contentType.indexOf('/') + 1);
        }

        if (extension == null) {
            extension = "jpeg";
        }
        return extension;
    }

    /**
     * Reads the configured dimension, treating a non-numeric value the same as a non-positive one. It used to reach the
     * caller as a raw {@code NumberFormatException} through the old {@code catch (Exception)}, which is to say as an
     * unexplained 500.
     */
    private int parseDimension(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException _) {
            return 0;
        }
    }

    private BufferedImage buildResizedImage(CmsProductImage productImage, byte[] byteArray, int largeImageWidth,
                                            int largeImageHeight) throws ImageUnreadableException, IOException {
        InputStream is2 = new ByteArrayInputStream(byteArray);

        BufferedImage bufferedImage = ImageIO.read(is2);

        if (bufferedImage == null) {
            log.error("Cannot read image format for {}", productImage.getProductImage());
            throw ImageUnreadableException.of(productImage.getProductImage());
        }

        // crop image
        ProductImageCropUtils utils = new ProductImageCropUtils(bufferedImage, largeImageWidth, largeImageHeight);
        if (cropUploads && utils.isCropeable()) {
            bufferedImage = utils.getCroppedImage();
        }

        if (bufferedImage.getWidth() > largeImageWidth || bufferedImage.getHeight() > largeImageHeight) {
            return ProductImageSizeUtils.resizeWithRatio(bufferedImage, largeImageWidth, largeImageHeight);
        }
        return bufferedImage;
    }

    public OutputContentFile getProductImage(CmsProductImage productImage)
            throws AssetNotFoundException, AssetReadFailedException {
        return getImage.getProductImage(productImage);
    }

    @Override
    public List<OutputContentFile> getImages(final String merchantStoreCode, FileContentType imageContentType)
            throws AssetListFailedException {
        // will return original
        return getImage.getImages(merchantStoreCode, FileContentType.PRODUCT);
    }

    @Override
    public void removeProductImage(CmsProductImage productImage) throws AssetDeleteFailedException {
        this.removeImage.removeProductImage(productImage);
    }

    @Override
    public void removeImages(final String merchantStoreCode) throws AssetDeleteFailedException {

        this.removeImage.removeImages(merchantStoreCode);
    }

    @Override
    public OutputContentFile getProductImage(String merchantStoreCode, String productCode, String imageName)
            throws AssetNotFoundException, AssetReadFailedException {
        return getImage.getProductImage(merchantStoreCode, productCode, imageName);
    }

    @Override
    public OutputContentFile getProductImage(String merchantStoreCode, String productCode, String imageName,
                                             ProductImageSize size) throws AssetNotFoundException, AssetReadFailedException {
        return getImage.getProductImage(merchantStoreCode, productCode, imageName, size);
    }

}
