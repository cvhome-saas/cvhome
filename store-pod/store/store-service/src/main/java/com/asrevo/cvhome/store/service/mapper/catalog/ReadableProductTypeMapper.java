package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ProductTypeDescription;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductType;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductTypeFull;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ReadableProductTypeMapper implements Mapper<ProductType, ReadableProductType> {

    @Override
    public ReadableProductType convert(ProductType source, MerchantStore store, Language language) {
        ReadableProductType type = new ReadableProductType();
        return this.merge(source, type, store, language);
    }

    @Override
    public ReadableProductType merge(ProductType source, ReadableProductType destination, MerchantStore store,
                                     Language language) {
        Assert.notNull(source, "ProductType cannot be null");
        Assert.notNull(destination, "ReadableProductType cannot be null");
        return type(source, language);
    }

    private ReadableProductType type(ProductType type, Language language) {
        ReadableProductType readableType = null;


        if (language != null) {
            readableType = new ReadableProductType();
            if (!CollectionUtils.isEmpty(type.getDescriptions())) {
                Optional<ProductTypeDescription> desc = type.getDescriptions().stream().filter(t -> t.getLanguage().getCode().equals(language.getCode()))
                        .map(d -> typeDescription(d)).findFirst();
                if (desc.isPresent()) {
                    readableType.setDescription(desc.get());
                }
            }
        } else {

            readableType = new ReadableProductTypeFull();
            List<ProductTypeDescription> descriptions = type.getDescriptions().stream().map(t -> this.typeDescription(t)).collect(Collectors.toList());
            ((ReadableProductTypeFull) readableType).setDescriptions(descriptions);

        }

        readableType.setCode(type.getCode());
        readableType.setId(type.getId());
        readableType.setVisible(type.getVisible() != null && type.getVisible().booleanValue() ? true : false);
        readableType.setAllowAddToCart(type.getAllowAddToCart() != null && type.getAllowAddToCart().booleanValue() ? true : false);

        return readableType;
    }

    private ProductTypeDescription typeDescription(com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductTypeDescription description) {
        ProductTypeDescription desc = new ProductTypeDescription();
        desc.setId(description.getId());
        desc.setName(description.getName());
        desc.setDescription(description.getDescription());
        desc.setLanguage(description.getLanguage().getCode());
        return desc;
    }

}
