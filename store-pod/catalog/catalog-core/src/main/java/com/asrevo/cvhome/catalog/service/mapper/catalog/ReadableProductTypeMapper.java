package com.asrevo.cvhome.catalog.service.mapper.catalog;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;

@Component
public class ReadableProductTypeMapper implements Mapper<ProductType, ReadableProductType> {

    @Override
    public ReadableProductType convert(ProductType source, StoreMerchantId store, LanguageCode language) {
        ReadableProductType type = new ReadableProductType();
        return this.merge(source, type, store, language);
    }

    @Override
    public ReadableProductType merge(ProductType source, ReadableProductType target, StoreMerchantId store,
                                     LanguageCode language) {
        target = new ReadableProductType();

        target.setCode(source.getCode());
        target.setId(source.getId());
        target.setVisible(source.getVisible() != null && source.getVisible());
        target.setAllowAddToCart(source.getAllowAddToCart() != null && source.getAllowAddToCart());

        if (LanguageCode.isAllLanguage(language)) {
            var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(Set.of());
            target.setDescriptions(descriptionSet.stream().map(this::populateDescription).toList());
        }
        if (LanguageCode.isLanguage(language)) {
            var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(Set.of());
            var description = descriptionSet.stream()
                    .filter(it -> language.equals(it.getLanguageCode()))
                    .findFirst()
                    .map(this::populateDescription)
                    .orElse(null);
            target.setDescription(description);
        }

        return target;
    }

    private com.asrevo.cvhome.catalog.model.product.type.ProductTypeDescription populateDescription(
            com.asrevo.cvhome.catalog.entity.product.type.ProductTypeDescription description) {
        com.asrevo.cvhome.catalog.model.product.type.ProductTypeDescription d =
                new com.asrevo.cvhome.catalog.model.product.type.ProductTypeDescription();
        d.setId(description.getId());
        d.setName(description.getName());
        d.setDescription(description.getDescription());
        d.setLanguage(description.getLanguageCode());
        return d;
    }

}
