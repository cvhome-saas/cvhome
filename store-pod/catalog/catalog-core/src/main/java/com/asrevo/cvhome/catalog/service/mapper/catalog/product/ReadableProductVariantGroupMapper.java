package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariantGroup;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;

@Component
public class ReadableProductVariantGroupMapper implements Mapper<ProductVariantGroup, ReadableProductVariantGroup> {

    private final ReadableProductVariantMapper readableProductVariantMapper;

    public ReadableProductVariantGroupMapper(ReadableProductVariantMapper readableProductVariantMapper) {
        this.readableProductVariantMapper = readableProductVariantMapper;
    }

    @Override
    public ReadableProductVariantGroup convert(ProductVariantGroup source, StoreMerchantId store,
                                               LanguageCode language) {
        return this.merge(source, new ReadableProductVariantGroup(), store, language);
    }

    @Override
    public ReadableProductVariantGroup merge(ProductVariantGroup source, ReadableProductVariantGroup destination,
                                             StoreMerchantId store, LanguageCode language) {
        if (destination == null) {
            destination = new ReadableProductVariantGroup();
        }

        destination.setId(source.getId());

        Set<ProductVariant> instances = source.getProductVariants();
        destination.setProductVariants(
                instances.stream().map(i -> this.instance(i, store, language)).toList());


        return destination;
    }

    private ReadableProductVariant instance(ProductVariant instance, StoreMerchantId store, LanguageCode language) {

        return readableProductVariantMapper.convert(instance, store, language);
    }

}
