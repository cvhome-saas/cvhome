package com.asrevo.cvhome.catalog.service.mapper.catalog;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.product.variation.ProductVariation;
import com.asrevo.cvhome.catalog.errors.ProductOptionReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductOptionValueReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.variation.PersistableProductVariation;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionService;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionValueService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;

@Component
public class PersistableProductVariationMapper implements Mapper<PersistableProductVariation, ProductVariation> {

    private final ProductOptionService productOptionService;

    private final ProductOptionValueService productOptionValueService;

    public PersistableProductVariationMapper(ProductOptionService productOptionService,
                                             ProductOptionValueService productOptionValueService) {
        this.productOptionService = productOptionService;
        this.productOptionValueService = productOptionValueService;
    }

    @Override
    public ProductVariation convert(PersistableProductVariation source, StoreMerchantId store, LanguageCode language)
            throws ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException {

        ProductVariation variation = new ProductVariation();
        return this.merge(source, variation, store, language);
    }

    @Override
    public ProductVariation merge(PersistableProductVariation source, ProductVariation destination,
                                  StoreMerchantId store, LanguageCode language)
            throws ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException {
        destination.setId(source.getId());
        destination.setCode(source.getCode());
        destination.setStoreMerchantId(store);

        ProductOption option = productOptionService.getById(store, source.getOption());
        if (option == null) {
            throw ProductOptionReferenceUnresolvableException.of(source.getOption(), store);
        }
        destination.setProductOption(option);

        ProductOptionValue optionValue = productOptionValueService.getById(store, source.getOptionValue());
        if (optionValue == null) {
            throw ProductOptionValueReferenceUnresolvableException.of(source.getOptionValue(), store);
        }
        destination.setProductOptionValue(optionValue);

        return destination;
    }

}
