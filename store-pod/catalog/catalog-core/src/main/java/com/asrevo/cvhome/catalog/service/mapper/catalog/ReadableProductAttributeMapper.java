package com.asrevo.cvhome.catalog.service.mapper.catalog;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductAttributeEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValue;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.PriceUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ReadableProductAttributeMapper
        implements Mapper<ProductAttribute, ReadableProductAttributeEntity> {

    private final ReadableProductOptionMapper readableProductOptionMapper;

    private final ReadableProductOptionValueMapper readableProductOptionValueMapper;

    private final PricingService pricingService;

    public ReadableProductAttributeMapper(
            ReadableProductOptionMapper readableProductOptionMapper,
            ReadableProductOptionValueMapper readableProductOptionValueMapper,
            PricingService pricingService) {
        this.readableProductOptionMapper = readableProductOptionMapper;
        this.readableProductOptionValueMapper = readableProductOptionValueMapper;
        this.pricingService = pricingService;
    }

    @Override
    public ReadableProductAttributeEntity convert(
            ProductAttribute source, StoreMerchantId store, LanguageCode language) {
        ReadableProductAttributeEntity productAttribute = new ReadableProductAttributeEntity();
        return merge(source, productAttribute, store, language);
    }

    @Override
    public ReadableProductAttributeEntity merge(
            ProductAttribute source,
            ReadableProductAttributeEntity destination,
            StoreMerchantId store,
            LanguageCode language) {

        ReadableProductAttributeEntity attr = new ReadableProductAttributeEntity();
        if (destination != null) {
            attr = destination;
        }
        try {
            attr.setId(source.getId()); // attribute of the option

            if (source.getProductAttributePrice() != null
                    && source.getProductAttributePrice().doubleValue() > 0) {
                String formatedPrice;
                formatedPrice =
                        pricingService.getDisplayAmount(source.getProductAttributePrice(), store);
                attr.setProductAttributePrice(formatedPrice);
                attr.setProductAttributeUnformattedPrice(
                        PriceUtils.getStringAmount(source.getProductAttributePrice()));
            }

            attr.setProductAttributeWeight(source.getAttributeAdditionalWeight());
            attr.setAttributeDisplayOnly(source.isAttributeDisplayOnly());
            attr.setAttributeDefault(source.isAttributeDefault());
            if (!StringUtils.isBlank(source.getAttributeSortOrder())) {
                attr.setSortOrder(Integer.parseInt(source.getAttributeSortOrder()));
            }

            if (source.getProductOption() != null) {
                ReadableProductOptionEntity option =
                        readableProductOptionMapper.convert(
                                source.getProductOption(), store, language);
                attr.setOption(option);
            }

            if (source.getProductOptionValue() != null) {
                ReadableProductOptionValue optionValue =
                        readableProductOptionValueMapper.convert(
                                source.getProductOptionValue(), store, language);
                attr.setOptionValue(optionValue);
            }

        } catch (Exception e) {
            throw new ConversionRuntimeException("Exception while product attribute conversion", e);
        }

        return attr;
    }
}
