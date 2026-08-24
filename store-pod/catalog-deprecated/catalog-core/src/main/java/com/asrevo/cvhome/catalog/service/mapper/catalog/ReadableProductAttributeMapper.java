package com.asrevo.cvhome.catalog.service.mapper.catalog;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.errors.ProductAttributeNotConvertibleException;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductAttributeEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValue;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.utils.PriceUtils;

@Component
public class ReadableProductAttributeMapper implements Mapper<ProductAttribute, ReadableProductAttributeEntity> {

    private final ReadableProductOptionMapper readableProductOptionMapper;

    private final ReadableProductOptionValueMapper readableProductOptionValueMapper;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ReadableProductAttributeMapper(ReadableProductOptionMapper readableProductOptionMapper,
                                          ReadableProductOptionValueMapper readableProductOptionValueMapper,
                                          ExternalMerchantStoreService externalMerchantStoreService) {
        this.readableProductOptionMapper = readableProductOptionMapper;
        this.readableProductOptionValueMapper = readableProductOptionValueMapper;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableProductAttributeEntity convert(ProductAttribute source, StoreMerchantId store,
                                                  LanguageCode language) throws ProductAttributeNotConvertibleException {
        ReadableProductAttributeEntity productAttribute = new ReadableProductAttributeEntity();
        return merge(source, productAttribute, store, language);
    }

    @Override
    public ReadableProductAttributeEntity merge(ProductAttribute source, ReadableProductAttributeEntity destination,
                                                StoreMerchantId store, LanguageCode language)
            throws ProductAttributeNotConvertibleException {

        ReadableProductAttributeEntity attr = new ReadableProductAttributeEntity();
        if (destination != null) {
            attr = destination;
        }
        try {
            attr.setId(source.getId()); // attribute of the option

            if (source.getProductAttributePrice() != null && source.getProductAttributePrice().doubleValue() > 0) {
                String formatedPrice;
                formatedPrice = PriceUtils.getStoreFormatedAmountWithCurrency(
                        externalMerchantStoreService.getStore(store), source.getProductAttributePrice());
                attr.setProductAttributePrice(formatedPrice);
                attr.setProductAttributeUnformattedPrice(PriceUtils.getStringAmount(source.getProductAttributePrice()));
            }

            attr.setProductAttributeWeight(source.getAttributeAdditionalWeight());
            attr.setAttributeDisplayOnly(source.isAttributeDisplayOnly());
            attr.setAttributeDefault(source.isAttributeDefault());
            if (!StringUtils.isBlank(source.getAttributeSortOrder())) {
                attr.setSortOrder(Integer.parseInt(source.getAttributeSortOrder()));
            }

            if (source.getProductOption() != null) {
                ReadableProductOptionEntity option = readableProductOptionMapper.convert(source.getProductOption(),
                        store, language);
                attr.setOption(option);
            }

            if (source.getProductOptionValue() != null) {
                ReadableProductOptionValue optionValue = readableProductOptionValueMapper
                        .convert(source.getProductOptionValue(), store, language);
                attr.setOptionValue(optionValue);
            }

        } catch (Exception e) {
            throw ProductAttributeNotConvertibleException.of(e);
        }

        return attr;
    }

}
