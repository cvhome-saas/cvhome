package com.asrevo.cvhome.store.core.modules.cms.product;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import com.asrevo.cvhome.store.core.exception.ServiceException;
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

    public void addProductImage(CmsProductImage productImage, ImageContentFile contentImage) throws ServiceException {

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

        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    private void uploadOriginalAsProductImage(CmsProductImage productImage, ImageContentFile contentImage,
            byte[] byteArray) throws Exception {
        contentImage.setFileContentType(FileContentType.PRODUCT);
        InputStream is2 = new ByteArrayInputStream(byteArray);
        contentImage.setFile(is2);
        uploadImage.addProductImage(productImage, contentImage);
    }

    private void uploadResizedImage(CmsProductImage productImage, ImageContentFile contentImage, byte[] byteArray)
            throws Exception {
        String extension = resolveExtension(contentImage);

        int largeImageHeight = Integer.parseInt(height);
        int largeImageWidth = Integer.parseInt(width);

        if (largeImageHeight <= 0 || largeImageWidth <= 0) {
            String sizeMsg = String.format(
                    "Image configuration set to an invalid value [PRODUCT_IMAGE_HEIGHT_SIZE] %s , [PRODUCT_IMAGE_WIDTH_SIZE] %s",
                    largeImageHeight, largeImageWidth);
            log.error(sizeMsg);
            throw new ServiceException(sizeMsg);
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

    private BufferedImage buildResizedImage(CmsProductImage productImage, byte[] byteArray, int largeImageWidth,
            int largeImageHeight) throws Exception {
        InputStream is2 = new ByteArrayInputStream(byteArray);

        BufferedImage bufferedImage = ImageIO.read(is2);

        if (bufferedImage == null) {
            log.error("Cannot read image format for {}", productImage.getProductImage());
            throw new Exception(
                    String.format("Cannot read image format %s", productImage.getProductImage()));
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

    public OutputContentFile getProductImage(CmsProductImage productImage) throws ServiceException {
        return getImage.getProductImage(productImage);
    }

    @Override
    public List<OutputContentFile> getImages(final String merchantStoreCode, FileContentType imageContentType)
            throws ServiceException {
        // will return original
        return getImage.getImages(merchantStoreCode, FileContentType.PRODUCT);
    }

    @Override
    public void removeProductImage(CmsProductImage productImage) throws ServiceException {
        this.removeImage.removeProductImage(productImage);
    }

    @Override
    public void removeImages(final String merchantStoreCode) throws ServiceException {

        this.removeImage.removeImages(merchantStoreCode);
    }

    @Override
    public OutputContentFile getProductImage(String merchantStoreCode, String productCode, String imageName)
            throws ServiceException {
        return getImage.getProductImage(merchantStoreCode, productCode, imageName);
    }

    @Override
    public OutputContentFile getProductImage(String merchantStoreCode, String productCode, String imageName,
                                             ProductImageSize size) throws ServiceException {
        return getImage.getProductImage(merchantStoreCode, productCode, imageName, size);
    }

}
