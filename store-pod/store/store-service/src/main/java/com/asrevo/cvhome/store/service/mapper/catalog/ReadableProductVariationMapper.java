package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOption;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.variation.ProductVariation;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductOption;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductOptionValue;
import com.asrevo.cvhome.store.core.model.catalog.product.variation.ReadableProductVariation;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Set;

@Component
public class ReadableProductVariationMapper implements Mapper<ProductVariation, ReadableProductVariation> {


    @Override
    public ReadableProductVariation convert(ProductVariation source, MerchantStore store, Language language) {
        ReadableProductVariation variation = new ReadableProductVariation();
        return merge(source, variation, store, language);
    }

    @Override
    public ReadableProductVariation merge(ProductVariation source, ReadableProductVariation destination,
                                          MerchantStore store, Language language) {

        Assert.notNull(source, "ProductVariation must not be null");
        Assert.notNull(destination, "ReadableProductVariation must not be null");


        destination.setId(source.getId());
        destination.setCode(source.getCode());


        destination.setOption(this.option(source.getProductOption(), language));
        destination.setOptionValue(this.optionValue(source.getProductOptionValue(), store, language));

        return destination;
    }

    private ReadableProductOption option(ProductOption option, Language lang) {

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

    private ReadableProductOptionValue optionValue(ProductOptionValue val, MerchantStore store, Language language) {

        ReadableProductOptionValue value = new ReadableProductOptionValue();
        value.setCode(val.getCode());
        value.setId(val.getId());
        ProductOptionValueDescription desc = optionValueDescription(val.getDescriptions(), language);
        if (desc != null) {
            value.setName(desc.getName());
        }
        return value;

    }

    private ProductOptionDescription optionDescription(Set<ProductOptionDescription> descriptions, Language lang) {
        return descriptions.stream().filter(desc -> desc.getLanguage().getCode().equals(lang.getCode())).findAny().orElse(null);
    }

    private ProductOptionValueDescription optionValueDescription(Set<ProductOptionValueDescription> descriptions, Language lang) {
        return descriptions.stream().filter(desc -> desc.getLanguage().getCode().equals(lang.getCode())).findAny().orElse(null);
    }

}
