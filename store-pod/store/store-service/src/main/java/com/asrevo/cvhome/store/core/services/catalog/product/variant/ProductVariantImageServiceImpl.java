package com.asrevo.cvhome.store.core.services.catalog.product.variant;

import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantImage;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.repositories.catalog.product.variant.ProductVariantImageRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;


@Service("productVariantImageService")
public class ProductVariantImageServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductVariantImage> implements ProductVariantImageService {

    @Autowired
    private ProductVariantImageRepository productVariantImageRepository;

    public ProductVariantImageServiceImpl(ProductVariantImageRepository productVariantImageRepository) {
        super(productVariantImageRepository);
        this.productVariantImageRepository = productVariantImageRepository;
    }

    @Override
    public List<ProductVariantImage> list(Long productVariantId, MerchantStore store) {
        Assert.notNull(store, "MerchantStore cannot be null");
        return productVariantImageRepository.finByProductVariant(productVariantId, store.getCode());
    }

    @Override
    public List<ProductVariantImage> listByProduct(Long productId, MerchantStore store) {
        Assert.notNull(store, "MerchantStore cannot be null");
        return productVariantImageRepository.finByProduct(productId, store.getCode());
    }

    @Override
    public List<ProductVariantImage> listByProductVariantGroup(Long productVariantGroupId, MerchantStore store) {
        Assert.notNull(store, "MerchantStore cannot be null");
        return productVariantImageRepository.finByProductVariantGroup(productVariantGroupId, store.getCode());
    }

}
