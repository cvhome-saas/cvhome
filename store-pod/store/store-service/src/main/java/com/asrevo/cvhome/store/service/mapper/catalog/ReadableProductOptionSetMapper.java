package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.*;
import com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductOption;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductOptionValue;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.optionset.ReadableProductOptionSet;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductType;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReadableProductOptionSetMapper implements Mapper<ProductOptionSet, ReadableProductOptionSet> {


    private final ReadableProductTypeMapper readableProductTypeMapper;

    public ReadableProductOptionSetMapper(ReadableProductTypeMapper readableProductTypeMapper) {
        this.readableProductTypeMapper = readableProductTypeMapper;
    }

    @Override
    public ReadableProductOptionSet convert(ProductOptionSet source, MerchantStore store, Language language) {
        ReadableProductOptionSet optionSource = new ReadableProductOptionSet();
        return merge(source, optionSource, store, language);
    }

    @Override
    public ReadableProductOptionSet merge(ProductOptionSet source, ReadableProductOptionSet destination,
                                          MerchantStore store, Language language) {
        Assert.notNull(source, "ProductOptionSet must not be null");
        Assert.notNull(destination, "ReadableProductOptionSet must not be null");


        destination.setId(source.getId());
        destination.setCode(source.getCode());
        destination.setReadOnly(source.isOptionDisplayOnly());

        destination.setOption(this.option(source.getOption(), store, language));

        List<Long> ids = new ArrayList<>();

        if (!CollectionUtils.isEmpty(source.getValues())) {
            List<ReadableProductOptionValue> values = source.getValues().stream().map(val -> optionValue(ids, val, store, language)).collect(Collectors.toList());
            destination.setValues(values);
            destination.getValues().removeAll(Collections.singleton(null));
        }

        if (!CollectionUtils.isEmpty(source.getProductTypes())) {
            List<ReadableProductType> types = source.getProductTypes().stream().map(t -> this.productType(t, store, language)).collect(Collectors.toList());
            destination.setProductTypes(types);
        }


        return destination;
    }

    private ReadableProductOption option(ProductOption option, MerchantStore store, Language lang) {

        ReadableProductOption opt = new ReadableProductOption();
        opt.setCode(option.getCode());
        opt.setId(option.getId());
        opt.setLang(lang.getCode());
        opt.setReadOnly(option.isReadOnly());
        opt.setType(option.getProductOptionType());
        ProductOptionDescription desc = this.optionDescription(option.getDescriptions(), lang);
        if (desc != null) {
            opt.setName(desc.getName());
        }

        return opt;
    }

    private ReadableProductOptionValue optionValue(List<Long> ids, ProductOptionValue optionValue, MerchantStore store, Language language) {

        if (!ids.contains(optionValue.getId())) {
            ReadableProductOptionValue value = new ReadableProductOptionValue();
            value.setCode(optionValue.getCode());
            value.setId(optionValue.getId());
            ProductOptionValueDescription desc = optionValueDescription(optionValue.getDescriptions(), language);
            if (desc != null) {
                value.setName(desc.getName());
            }
            ids.add(optionValue.getId());
            return value;
        } else {
            return null;
        }
    }

    private ProductOptionDescription optionDescription(Set<ProductOptionDescription> descriptions, Language lang) {
        return descriptions.stream().filter(desc -> desc.getLanguage().getCode().equals(lang.getCode())).findAny().orElse(null);
    }

    private ProductOptionValueDescription optionValueDescription(Set<ProductOptionValueDescription> descriptions, Language lang) {
        return descriptions.stream().filter(desc -> desc.getLanguage().getCode().equals(lang.getCode())).findAny().orElse(null);
    }

    private ReadableProductType productType(ProductType type, MerchantStore store, Language language) {
        return readableProductTypeMapper.convert(type, store, language);
    }

}
