package com.asrevo.cvhome.catalog.service.mapper.catalog;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.catalog.model.product.type.ProductTypeDescription;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductTypeFull;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ReadableProductTypeMapper implements Mapper<ProductType, ReadableProductType> {

    @Override
    public ReadableProductType convert(
            ProductType source, StoreMerchantId store, LanguageCode language) {
        ReadableProductType type = new ReadableProductType();
        return this.merge(source, type, store, language);
    }

    @Override
    public ReadableProductType merge(
            ProductType source,
            ReadableProductType destination,
            StoreMerchantId store,
            LanguageCode language) {
        Assert.notNull(source, "ProductType cannot be null");
        Assert.notNull(destination, "ReadableProductType cannot be null");
        return type(source, language);
    }

    private ReadableProductType type(ProductType type, LanguageCode language) {
        ReadableProductType readableType;

        if (language != null) {
            readableType = new ReadableProductType();
            if (!CollectionUtils.isEmpty(type.getDescriptions())) {
                Optional<ProductTypeDescription> desc =
                        type.getDescriptions().stream()
                                .filter(t -> t.getLanguageCode().equals(language))
                                .map(this::typeDescription)
                                .findFirst();
                desc.ifPresent(readableType::setDescription);
            }
        } else {

            readableType = new ReadableProductTypeFull();
            List<ProductTypeDescription> descriptions =
                    type.getDescriptions().stream()
                            .map(this::typeDescription)
                            .collect(Collectors.toList());
            ((ReadableProductTypeFull) readableType).setDescriptions(descriptions);
        }

        readableType.setCode(type.getCode());
        readableType.setId(type.getId());
        readableType.setVisible(type.getVisible() != null && type.getVisible());
        readableType.setAllowAddToCart(
                type.getAllowAddToCart() != null && type.getAllowAddToCart());

        return readableType;
    }

    private ProductTypeDescription typeDescription(
            com.asrevo.cvhome.catalog.entity.product.type.ProductTypeDescription description) {
        ProductTypeDescription desc = new ProductTypeDescription();
        desc.setId(description.getId());
        desc.setName(description.getName());
        desc.setDescription(description.getDescription());
        desc.setLanguage(description.getLanguageCode());
        return desc;
    }
}
