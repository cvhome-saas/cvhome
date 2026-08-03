package com.asrevo.cvhome.catalog.services.product.image;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.errors.ProductImageNotPersistedException;
import com.asrevo.cvhome.catalog.repositories.product.image.ProductImageRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.catalog.product.file.ProductImageSize;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetDeleteFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetNotFoundException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetReadFailedException;
import com.asrevo.cvhome.store.core.modules.cms.model.CmsProductImage;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductFileManager;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

@Service("productImage")
public class ProductImageServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductImage>
        implements ProductImageService {

    private final ProductImageRepository productImageRepository;

    private final ProductFileManager productFileManager;

    @Autowired
    public ProductImageServiceImpl(ProductImageRepository productImageRepository,
                                   ProductFileManager productFileManager) {
        super(productImageRepository);
        this.productImageRepository = productImageRepository;
        this.productFileManager = productFileManager;
    }

    public ProductImage getById(Long id) {

        return productImageRepository.findOne(id);
    }

    @Override
    public void addProductImages(Product product, List<ProductImage> productImages)
            throws ProductImageNotPersistedException {

        try {
            for (ProductImage productImage : productImages) {
                InputStream inputStream = productImage.getImage();
                ImageContentFile cmsContentImage = new ImageContentFile();
                cmsContentImage.setFileName(productImage.getProductImage());
                cmsContentImage.setFile(inputStream);
                cmsContentImage.setFileContentType(FileContentType.PRODUCT);

                addProductImage(product, productImage, cmsContentImage);
            }

        } catch (Exception e) {
            throw ProductImageNotPersistedException.of(product.getSku(), e);
        }
    }

    @Override
    public void addProductImage(Product product, ProductImage productImage, ImageContentFile inputImage)
            throws ProductImageNotPersistedException {

        productImage.setProduct(product);

        try {
            if (productImage.getImageType() == 0) {
                CmsProductImage cmsProductImage = new CmsProductImage(productImage.getProduct().getId(),
                        productImage.getProduct().getStore(), productImage.getProduct().getSku(),
                        productImage.getProductImage());
                productFileManager.addProductImage(cmsProductImage, inputImage);
            }

            // insert ProductImage
            saveOrUpdate(productImage);

        } catch (Exception e) {
            throw ProductImageNotPersistedException.of(productImage.getProduct().getSku(), e);
        } finally {
            try {

                if (inputImage.getFile() != null) {
                    inputImage.getFile().close();
                }

            } catch (Exception _) {

            }
        }
    }

    @Override
    public ProductImage saveOrUpdate(ProductImage productImage) {

        return productImageRepository.save(productImage);
    }

    @Override
    public OutputContentFile getProductImage(ProductImage productImage, ProductImageSize size)
            throws AssetNotFoundException, AssetReadFailedException {

        ProductImage pi = new ProductImage();
        String imageName = productImage.getProductImage();
        if (size == ProductImageSize.LARGE) {
            imageName = "L-%s".formatted(imageName);
        }

        if (size == ProductImageSize.SMALL) {
            imageName = "S-%s".formatted(imageName);
        }

        pi.setProductImage(imageName);
        pi.setProduct(productImage.getProduct());
        CmsProductImage cmsProductImage = new CmsProductImage(productImage.getProduct().getId(),
                productImage.getProduct().getStore(), productImage.getProduct().getSku(),
                productImage.getProductImage());

        return productFileManager.getProductImage(cmsProductImage);
    }

    @Override
    public OutputContentFile getProductImage(final String storeCode, final String productCode, final String fileName,
                                             final ProductImageSize size)
            throws AssetNotFoundException, AssetReadFailedException {
        return productFileManager.getProductImage(storeCode, productCode, fileName, size);
    }

    @Override
    public void removeProductImage(ProductImage productImage) throws AssetDeleteFailedException {

        if (!StringUtils.isBlank(productImage.getProductImage())) {
            CmsProductImage cmsProductImage = new CmsProductImage(productImage.getProduct().getId(),
                    productImage.getProduct().getStore(), productImage.getProduct().getSku(),
                    productImage.getProductImage());
            productFileManager.removeProductImage(cmsProductImage); // managed internally
        }
        ProductImage p = getById(productImage.getId());

        delete(p);
    }

    @Override
    public Optional<ProductImage> getProductImage(Long imageId, Long productId, StoreMerchantId store) {

        Optional<ProductImage> image = Optional.empty();

        ProductImage img = productImageRepository.finById(imageId, productId, store);
        if (img != null) {
            image = Optional.of(img);
        }

        return image;
    }

    @Override
    public void updateProductImage(Product product, ProductImage productImage) {
        productImage.setProduct(product);
        productImageRepository.save(productImage);
    }

}
