package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductVariantParentMissingException;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariantGroup;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;

@Component
public class ReadableProductVariantGroupMapper implements Mapper<ProductVariantGroup, ReadableProductVariantGroup> {

    private final ReadableProductVariantMapper readableProductVariantMapper;

    public ReadableProductVariantGroupMapper(ReadableProductVariantMapper readableProductVariantMapper) {
        this.readableProductVariantMapper = readableProductVariantMapper;
    }

    @Override
    public ReadableProductVariantGroup convert(ProductVariantGroup source, StoreMerchantId store,
                                               LanguageCode language)
            throws ProductVariantParentMissingException, InventoryNotConvertibleException {
        return this.merge(source, new ReadableProductVariantGroup(), store, language);
    }

    @Override
    public ReadableProductVariantGroup merge(ProductVariantGroup source, ReadableProductVariantGroup destination,
                                             StoreMerchantId store, LanguageCode language)
            throws ProductVariantParentMissingException, InventoryNotConvertibleException {
        if (destination == null) {
            destination = new ReadableProductVariantGroup();
        }

        destination.setId(source.getId());

        Set<ProductVariant> instances = source.getProductVariants();
        // A plain loop rather than stream().map(...): the variant mapper declares checked failures, and a lambda
        // cannot carry them. The one-line `instance` helper it used to call went with it.
        List<ReadableProductVariant> readableVariants = new ArrayList<>();
        for (ProductVariant instance : instances) {
            readableVariants.add(readableProductVariantMapper.convert(instance, store, language));
        }
        destination.setProductVariants(readableVariants);

        return destination;
    }

}
