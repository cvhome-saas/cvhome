package com.asrevo.cvhome.catalog.service.populator.catalog.product;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.asrevo.cvhome.catalog.entity.product.group.ProductGroup;
import com.asrevo.cvhome.catalog.entity.product.group.ProductGroupDescription;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroupDescription;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableMinimalProductPopulator;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

public class ReadableProductGroupPopulator
        extends AbstractDataPopulator<ProductGroup, StoreMerchantId, ReadableProductGroup> {

    private final ReadableMinimalProductPopulator readableMinimalProductPopulator;

    public ReadableProductGroupPopulator(ReadableMinimalProductPopulator readableMinimalProductPopulator) {
        this.readableMinimalProductPopulator = readableMinimalProductPopulator;
    }

    @Override
    public ReadableProductGroup populate(ProductGroup source, ReadableProductGroup target, StoreMerchantId store,
                                         LanguageCode language) throws ConversionException {
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setActive(source.isActive());

        if (LanguageCode.isAllLanguage(language)) {
            var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(Set.of());
            target.setDescriptions(descriptionSet.stream().map(this::convertDescription).toList());
        }

        if (LanguageCode.isLanguage(language)) {
            var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(Set.of());
            var description = descriptionSet.stream()
                    .filter(it -> language.equals(it.getLanguageCode()))
                    .findFirst()
                    .map(this::convertDescription)
                    .orElse(null);
            target.setDescription(description);
        }

        if (source.getParentProduct() != null) {
            ReadableProduct readableProduct = new ReadableProduct();
            readableMinimalProductPopulator.populate(source.getParentProduct(), readableProduct, store, language);
            target.setParentProduct(readableProduct);
        }

        if (CollectionUtils.isNotEmpty(source.getProducts())) {
            target.setProducts(source.getProducts().stream().map(p -> {
                ReadableProduct readableProduct = new ReadableProduct();
                try {
                    readableMinimalProductPopulator.populate(p, readableProduct, store, language);
                } catch (ConversionException e) {
                    throw new RuntimeException(e);
                }
                return readableProduct;
            }).toList());
        }

        return target;
    }

    private ReadableProductGroupDescription convertDescription(ProductGroupDescription description) {
        ReadableProductGroupDescription d = new ReadableProductGroupDescription();
        d.setId(description.getId());
        d.setName(description.getName());
        d.setDescription(description.getDescription());
        d.setLanguage(description.getLanguageCode());
        d.setFriendlyUrl(description.getSeUrl());
        d.setTitle(description.getMetatagTitle());
        d.setMetaDescription(description.getMetatagDescription());
        d.setKeyWords(description.getMetatagKeywords());
        return d;
    }

    @Override
    protected ReadableProductGroup createTarget() {
        return new ReadableProductGroup();
    }

}
