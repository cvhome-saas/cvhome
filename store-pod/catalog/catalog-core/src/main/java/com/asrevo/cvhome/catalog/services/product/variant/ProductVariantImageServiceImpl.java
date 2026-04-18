package com.asrevo.cvhome.catalog.services.product.variant;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantImage;
import com.asrevo.cvhome.catalog.repositories.product.variant.ProductVariantImageRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

@Service("productVariantImageService")
public class ProductVariantImageServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductVariantImage>
        implements ProductVariantImageService {

    private final ProductVariantImageRepository productVariantImageRepository;

    public ProductVariantImageServiceImpl(ProductVariantImageRepository productVariantImageRepository) {
        super(productVariantImageRepository);
        this.productVariantImageRepository = productVariantImageRepository;
    }

    @Override
    public List<ProductVariantImage> listByProductVariantGroup(Long productVariantGroupId, StoreMerchantId store) {
        Assert.notNull(store, "Store cannot be null");
        return productVariantImageRepository.finByProductVariantGroup(productVariantGroupId, store);
    }

}
