package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOption;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.store.core.entity.catalog.product.variation.ProductVariation;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.variation.PersistableProductVariation;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductOptionService;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductOptionValueService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class PersistableProductVariationMapper implements Mapper<PersistableProductVariation, ProductVariation> {

    @Autowired
    private ProductOptionService productOptionService;

    @Autowired
    private ProductOptionValueService productOptionValueService;

    @Override
    public ProductVariation convert(PersistableProductVariation source, MerchantStore store, Language language) {

        ProductVariation variation = new ProductVariation();
        return this.merge(source, variation, store, language);

    }

    @Override
    public ProductVariation merge(PersistableProductVariation source, ProductVariation destination, MerchantStore store,
                                  Language language) {
        Assert.notNull(destination, "ProductVariation cannot be null");

        destination.setId(source.getId());
        destination.setCode(source.getCode());
        destination.setMerchantStore(store);

        ProductOption option = productOptionService.getById(store, source.getOption());
        if (option == null) {
            throw new ConversionRuntimeException("ProductOption [" + source.getOption() + "] does not exists");
        }
        destination.setProductOption(option);

        ProductOptionValue optionValue = productOptionValueService.getById(store, source.getOptionValue());
        if (optionValue == null) {
            throw new ConversionRuntimeException("ProductOptionValue [" + source.getOptionValue() + "] does not exists");
        }
        destination.setProductOptionValue(optionValue);


        return destination;


    }

}
