package com.asrevo.cvhome.catalog.service.mapper.catalog;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.catalog.entity.product.variation.ProductVariation;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductOption;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.variation.ReadableProductVariation;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;

@Component
public class ReadableProductVariationMapper implements Mapper<ProductVariation, ReadableProductVariation> {

    @Override
    public ReadableProductVariation convert(ProductVariation source, StoreMerchantId store, LanguageCode language) {
        ReadableProductVariation variation = new ReadableProductVariation();
        return merge(source, variation, store, language);
    }

    @Override
    public ReadableProductVariation merge(ProductVariation source, ReadableProductVariation destination,
                                          StoreMerchantId store, LanguageCode language) {

        destination.setId(source.getId());
        destination.setCode(source.getCode());

        destination.setOption(this.option(source.getProductOption(), language));
        destination.setOptionValue(this.optionValue(source.getProductOptionValue(), language));

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

    private ReadableProductOptionValue optionValue(ProductOptionValue val, LanguageCode language) {

        ReadableProductOptionValue value = new ReadableProductOptionValue();
        value.setCode(val.getCode());
        value.setId(val.getId());
        ProductOptionValueDescription desc = optionValueDescription(val.getDescriptions(), language);
        if (desc != null) {
            value.setName(desc.getName());
        }
        return value;
    }

    private ProductOptionDescription optionDescription(Set<ProductOptionDescription> descriptions, LanguageCode lang) {
        return descriptions.stream().filter(desc -> desc.getLanguageCode().equals(lang)).findAny().orElse(null);
    }

    private ProductOptionValueDescription optionValueDescription(Set<ProductOptionValueDescription> descriptions,
                                                                 LanguageCode lang) {
        return descriptions.stream().filter(desc -> desc.getLanguageCode().equals(lang)).findAny().orElse(null);
    }

}
