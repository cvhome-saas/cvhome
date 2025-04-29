package com.asrevo.cvhome.catalog.service.mapper.catalog;

import com.asrevo.cvhome.catalog.entity.product.attribute.*;
import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductOption;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.optionset.ReadableProductOptionSet;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ReadableProductOptionSetMapper
        implements Mapper<ProductOptionSet, ReadableProductOptionSet> {

    private final ReadableProductTypeMapper readableProductTypeMapper;

    public ReadableProductOptionSetMapper(ReadableProductTypeMapper readableProductTypeMapper) {
        this.readableProductTypeMapper = readableProductTypeMapper;
    }

    @Override
    public ReadableProductOptionSet convert(
            ProductOptionSet source, StoreMerchantId store, LanguageCode language) {
        ReadableProductOptionSet optionSource = new ReadableProductOptionSet();
        return merge(source, optionSource, store, language);
    }

    @Override
    public ReadableProductOptionSet merge(
            ProductOptionSet source,
            ReadableProductOptionSet destination,
            StoreMerchantId store,
            LanguageCode language) {
        Assert.notNull(source, "ProductOptionSet must not be null");
        Assert.notNull(destination, "ReadableProductOptionSet must not be null");

        destination.setId(source.getId());
        destination.setCode(source.getCode());
        destination.setReadOnly(source.isOptionDisplayOnly());

        destination.setOption(this.option(source.getOption(), language));

        List<Long> ids = new ArrayList<>();

        if (!CollectionUtils.isEmpty(source.getValues())) {
            List<ReadableProductOptionValue> values =
                    source.getValues().stream()
                            .map(val -> optionValue(ids, val, language))
                            .collect(Collectors.toList());
            destination.setValues(values);
            destination.getValues().removeAll(Collections.singleton(null));
        }

        if (!CollectionUtils.isEmpty(source.getProductTypes())) {
            List<ReadableProductType> types =
                    source.getProductTypes().stream()
                            .map(t -> this.productType(t, store, language))
                            .collect(Collectors.toList());
            destination.setProductTypes(types);
        }

        return destination;
    }

    private ReadableProductOption option(ProductOption option, LanguageCode lang) {

        ReadableProductOption opt = new ReadableProductOption();
        opt.setCode(option.getCode());
        opt.setId(option.getId());
        opt.setLang(lang);
        opt.setReadOnly(option.isReadOnly());
        opt.setType(option.getProductOptionType());
        ProductOptionDescription desc = this.optionDescription(option.getDescriptions(), lang);
        if (desc != null) {
            opt.setName(desc.getName());
        }

        return opt;
    }

    private ReadableProductOptionValue optionValue(
            List<Long> ids, ProductOptionValue optionValue, LanguageCode language) {

        if (!ids.contains(optionValue.getId())) {
            ReadableProductOptionValue value = new ReadableProductOptionValue();
            value.setCode(optionValue.getCode());
            value.setId(optionValue.getId());
            ProductOptionValueDescription desc =
                    optionValueDescription(optionValue.getDescriptions(), language);
            if (desc != null) {
                value.setName(desc.getName());
            }
            ids.add(optionValue.getId());
            return value;
        } else {
            return null;
        }
    }

    private ProductOptionDescription optionDescription(
            Set<ProductOptionDescription> descriptions, LanguageCode lang) {
        return descriptions.stream()
                .filter(desc -> desc.getLanguageCode().equals(lang))
                .findAny()
                .orElse(null);
    }

    private ProductOptionValueDescription optionValueDescription(
            Set<ProductOptionValueDescription> descriptions, LanguageCode lang) {
        return descriptions.stream()
                .filter(desc -> desc.getLanguageCode().equals(lang))
                .findAny()
                .orElse(null);
    }

    private ReadableProductType productType(
            ProductType type, StoreMerchantId store, LanguageCode language) {
        return readableProductTypeMapper.convert(type, store, language);
    }
}
