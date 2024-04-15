package com.asrevo.cvhome.store.core.modules.cms.product;

import com.asrevo.cvhome.s2s.model.StoreProductImageProperties;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.file.ProductImageSize;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.utils.ProductImageCropUtils;
import com.asrevo.cvhome.store.utils.ProductImageSizeUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.List;


@Setter
@Getter
@Slf4j
public class ProductFileManagerImpl extends ProductFileManager {
    private final ProductImagePut uploadImage;
    private final ProductImageGet getImage;
    private final ProductImageRemove removeImage;
    private final StoreProductImageProperties storeProductImageProperties;

    public ProductFileManagerImpl(ProductImagePut uploadImage, ProductImageGet getImage, ProductImageRemove removeImage, StoreProductImageProperties storeProductImageProperties) {
        this.uploadImage = uploadImage;
        this.getImage = getImage;
        this.removeImage = removeImage;
        this.storeProductImageProperties = storeProductImageProperties;
    }


    public void addProductImage(ProductImage productImage, ImageContentFile contentImage)
            throws ServiceException {


        try {

            /** copy to input stream **/
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Fake code simulating the copy
            // You can generally do better with nio if you need...
            // And please, unlike me, do something about the Exceptions :D
            byte[] buffer = new byte[1024];
            int len;
            while ((len = contentImage.getFile().read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();

            // Open new InputStreams using the recorded bytes
            // Can be repeated as many times as you wish
            InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
            InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

            BufferedImage bufferedImage = ImageIO.read(is2);


            if (bufferedImage == null) {
                log.error("Cannot read image format for {}", productImage.getProductImage());
                throw new Exception("Cannot read image format " + productImage.getProductImage());
            }

            // contentImage.setBufferedImage(bufferedImage);
            contentImage.setFile(is1);


            // upload original -- L
            contentImage.setFileContentType(FileContentType.PRODUCTLG);
            uploadImage.addProductImage(productImage, contentImage);

            /*
             * //default large InputContentImage largeContentImage = new
             * InputContentImage(ImageContentType.PRODUCT);
             * largeContentImage.setFile(contentImage.getFile());
             * largeContentImage.setDefaultImage(productImage.isDefaultImage());
             * largeContentImage.setImageName(new
             * StringBuilder().append("L-").append(productImage.getProductImage()).toString());
             *
             *
             * uploadImage.uploadProductImage(configuration, productImage, largeContentImage);
             */

            /*
             * //default small InputContentImage smallContentImage = new
             * InputContentImage(ImageContentType.PRODUCT);
             * smallContentImage.setFile(contentImage.getFile());
             * smallContentImage.setDefaultImage(productImage.isDefaultImage());
             * smallContentImage.setImageName(new
             * StringBuilder().append("S-").append(productImage.getProductImage()).toString());
             *
             * uploadImage.uploadProductImage(configuration, productImage, smallContentImage);
             */


            // get template properties file

            String slargeImageHeight = storeProductImageProperties.height();
            String slargeImageWidth = storeProductImageProperties.width();

            // String ssmallImageHeight = configuration.getProperty("SMALL_IMAGE_HEIGHT_SIZE");
            // String ssmallImageWidth = configuration.getProperty("SMALL_IMAGE_WIDTH_SIZE");

            //Resizes
            if (!StringUtils.isBlank(slargeImageHeight) && !StringUtils.isBlank(slargeImageWidth)) { // &&
                // !StringUtils.isBlank(ssmallImageHeight)
                // &&
                // !StringUtils.isBlank(ssmallImageWidth))
                // {


                FileNameMap fileNameMap = URLConnection.getFileNameMap();

                String contentType = fileNameMap.getContentTypeFor(contentImage.getFileName());
                String extension = null;
                if (contentType != null) {
                    extension = contentType.substring(contentType.indexOf('/') + 1);
                }

                if (extension == null) {
                    extension = "jpeg";
                }


                int largeImageHeight = Integer.parseInt(slargeImageHeight);
                int largeImageWidth = Integer.parseInt(slargeImageWidth);

                if (largeImageHeight <= 0 || largeImageWidth <= 0) {
                    String sizeMsg =
                            "Image configuration set to an invalid value [PRODUCT_IMAGE_HEIGHT_SIZE] "
                                    + largeImageHeight + " , [PRODUCT_IMAGE_WIDTH_SIZE] " + largeImageWidth;
                    log.error(sizeMsg);
                    throw new ServiceException(sizeMsg);
                }

                if (storeProductImageProperties.cropUploads()) {
                    // crop image
                    ProductImageCropUtils utils =
                            new ProductImageCropUtils(bufferedImage, largeImageWidth, largeImageHeight);
                    if (utils.isCropeable()) {
                        bufferedImage = utils.getCroppedImage();
                    }
                }

                // do not keep a large image for now, just take care of the regular image and a small image

                // resize large
                // ByteArrayOutputStream output = new ByteArrayOutputStream();
                BufferedImage largeResizedImage;
                if (bufferedImage.getWidth() > largeImageWidth || bufferedImage.getHeight() > largeImageHeight) {
                    largeResizedImage = ProductImageSizeUtils.resizeWithRatio(bufferedImage, largeImageWidth, largeImageHeight);
                } else {
                    largeResizedImage = bufferedImage;
                }


                File tempLarge =
                        File.createTempFile(new StringBuilder().append(productImage.getProduct().getId())
                                .append("tmpLarge").toString(), "." + extension);
                ImageIO.write(largeResizedImage, extension, tempLarge);

                try (FileInputStream isLarge = new FileInputStream(tempLarge)) {


                    // IOUtils.copy(isLarge, output);


                    ImageContentFile largeContentImage = new ImageContentFile();
                    largeContentImage.setFileContentType(FileContentType.PRODUCT);
                    largeContentImage.setFileName(productImage.getProductImage());
                    largeContentImage.setFile(isLarge);


                    // largeContentImage.setBufferedImage(bufferedImage);

                    // largeContentImage.setFile(output);
                    // largeContentImage.setDefaultImage(false);
                    // largeContentImage.setImageName(new
                    // StringBuilder().append("L-").append(productImage.getProductImage()).toString());


                    uploadImage.addProductImage(productImage, largeContentImage);

                    // output.flush();
                    // output.close();

                    tempLarge.delete();

                    // now upload original



                    /*
                     * //resize small BufferedImage smallResizedImage = ProductImageSizeUtils.resize(cropped,
                     * smallImageWidth, smallImageHeight); File tempSmall = File.createTempFile(new
                     * StringBuilder().append(productImage.getProduct().getId()).append("tmpSmall").toString(),
                     * "." + extension ); ImageIO.write(smallResizedImage, extension, tempSmall);
                     *
                     * //byte[] is = IOUtils.toByteArray(new FileInputStream(tempSmall));
                     *
                     * FileInputStream isSmall = new FileInputStream(tempSmall);
                     *
                     * output = new ByteArrayOutputStream(); IOUtils.copy(isSmall, output);
                     *
                     *
                     * smallContentImage = new InputContentImage(ImageContentType.PRODUCT);
                     * smallContentImage.setFile(output); smallContentImage.setDefaultImage(false);
                     * smallContentImage.setImageName(new
                     * StringBuilder().append("S-").append(productImage.getProductImage()).toString());
                     *
                     * uploadImage.uploadProductImage(configuration, productImage, smallContentImage);
                     *
                     * output.flush(); output.close();
                     *
                     * tempSmall.delete();
                     */


                }
            } else {
                // small will be the same as the original
                contentImage.setFileContentType(FileContentType.PRODUCT);
                uploadImage.addProductImage(productImage, contentImage);
            }


        } catch (Exception e) {
            throw new ServiceException(e);
        } finally {
            try {
                productImage.getImage().close();
            } catch (Exception ignore) {
            }
        }

    }


    public OutputContentFile getProductImage(ProductImage productImage) throws ServiceException {
        // will return original
        return getImage.getProductImage(productImage);
    }


    @Override
    public List<OutputContentFile> getImages(final String merchantStoreCode,
                                             FileContentType imageContentType) throws ServiceException {
        // will return original
        return getImage.getImages(merchantStoreCode, FileContentType.PRODUCT);
    }

    @Override
    public List<OutputContentFile> getImages(Product product) throws ServiceException {
        return getImage.getImages(product);
    }


    @Override
    public void removeProductImage(ProductImage productImage) throws ServiceException {

        this.removeImage.removeProductImage(productImage);

        /*
         * ProductImage large = new ProductImage(); large.setProduct(productImage.getProduct());
         * large.setProductImage("L" + productImage.getProductImage());
         *
         * this.removeImage.removeProductImage(large);
         *
         * ProductImage small = new ProductImage(); small.setProduct(productImage.getProduct());
         * small.setProductImage("S" + productImage.getProductImage());
         *
         * this.removeImage.removeProductImage(small);
         */

    }


    @Override
    public void removeProductImages(Product product) throws ServiceException {

        this.removeImage.removeProductImages(product);

    }


    @Override
    public void removeImages(final String merchantStoreCode) throws ServiceException {

        this.removeImage.removeImages(merchantStoreCode);

    }


    @Override
    public OutputContentFile getProductImage(String merchantStoreCode, String productCode,
                                             String imageName) throws ServiceException {
        return getImage.getProductImage(merchantStoreCode, productCode, imageName);
    }


    @Override
    public OutputContentFile getProductImage(String merchantStoreCode, String productCode,
                                             String imageName, ProductImageSize size) throws ServiceException {
        return getImage.getProductImage(merchantStoreCode, productCode, imageName, size);
    }


}
