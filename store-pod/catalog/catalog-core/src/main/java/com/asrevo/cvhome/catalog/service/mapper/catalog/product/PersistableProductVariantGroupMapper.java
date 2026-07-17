package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantImage;
import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariantGroup;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantImageService;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;

@Component
public class PersistableProductVariantGroupMapper
        implements Mapper<PersistableProductVariantGroup, ProductVariantGroup> {

    private final ProductVariantService productVariantService;

    private final ProductVariantImageService productVariantImageService;

    public PersistableProductVariantGroupMapper(ProductVariantService productVariantService,
                                                ProductVariantImageService productVariantImageService) {
        this.productVariantService = productVariantService;
        this.productVariantImageService = productVariantImageService;
    }

    @Override
    public ProductVariantGroup convert(PersistableProductVariantGroup source, StoreMerchantId store,
                                       LanguageCode language) {
        return this.merge(source, new ProductVariantGroup(), store, language);
    }

    @Override
    public ProductVariantGroup merge(PersistableProductVariantGroup source, ProductVariantGroup destination,
                                     StoreMerchantId store, LanguageCode language) {

        if (destination == null) {
            destination = new ProductVariantGroup();
        }

        destination.setId(source.getId());

        List<ProductVariant> productVariants = productVariantService.getByIds(source.getProductVariants(), store);

        for (ProductVariant p : productVariants) {
            p.setProductVariantGroup(destination);
        }

        // images are not managed from this object
        if (source.getId() != null) {
            List<ProductVariantImage> images = productVariantImageService.listByProductVariantGroup(source.getId(),
                    store);
            destination.setImages(images);
        }
        destination.setStoreMerchantId(store);
        destination.setProductVariants(new HashSet<>(productVariants));
        return destination;
    }

}
