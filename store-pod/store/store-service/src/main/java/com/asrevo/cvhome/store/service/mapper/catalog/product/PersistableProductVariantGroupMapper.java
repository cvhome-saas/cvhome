package com.asrevo.cvhome.store.service.mapper.catalog.product;

import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantImage;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup.PersistableProductVariantGroup;
import com.asrevo.cvhome.store.core.services.catalog.product.variant.ProductVariantImageService;
import com.asrevo.cvhome.store.core.services.catalog.product.variant.ProductVariantService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;

@Component
public class PersistableProductVariantGroupMapper implements Mapper<PersistableProductVariantGroup, ProductVariantGroup> {


    private final ProductVariantService productVariantService;

    private final ProductVariantImageService productVariantImageService;

    public PersistableProductVariantGroupMapper(ProductVariantService productVariantService, ProductVariantImageService productVariantImageService) {
        this.productVariantService = productVariantService;
        this.productVariantImageService = productVariantImageService;
    }

    @Override
    public ProductVariantGroup convert(PersistableProductVariantGroup source, MerchantStore store,
                                       Language language) {
        Assert.notNull(source, "PersistableproductVariantGroup cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        return this.merge(source, new ProductVariantGroup(), store, language);
    }

    @Override
    public ProductVariantGroup merge(PersistableProductVariantGroup source, ProductVariantGroup destination,
                                     MerchantStore store, Language language) {

        Assert.notNull(source, "PersistableproductVariantGroup cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        Assert.notNull(source.getProductVariants(), "Product instances cannot be null");

        if (destination == null) {
            destination = new ProductVariantGroup();
        }

        destination.setId(source.getId());


        List<ProductVariant> productVariants = productVariantService.getByIds(source.getProductVariants(), store);

        for (ProductVariant p : productVariants) {
            p.setProductVariantGroup(destination);
        }

        //images are not managed from this object
        if (source.getId() != null) {
            List<ProductVariantImage> images = productVariantImageService.listByProductVariantGroup(source.getId(), store);
            destination.setImages(images);
        }
        destination.setMerchantStore(store);
        destination.setProductVariants(new HashSet<>(productVariants));
        return destination;
    }

    private ProductVariant instance(ProductVariant instance, ProductVariantGroup group, MerchantStore store) {

        instance.setProductVariantGroup(group);
        return instance;

    }

}
