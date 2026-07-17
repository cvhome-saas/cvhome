package com.asrevo.cvhome.catalog.service.mapper.catalog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.catalog.model.product.type.PersistableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ProductTypeDescription;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;

@Component
public class PersistableProductTypeMapper implements Mapper<PersistableProductType, ProductType> {

    @Override
    public ProductType convert(PersistableProductType source, StoreMerchantId store, LanguageCode language) {
        ProductType type = new ProductType();
        return this.merge(source, type, store, language);
    }

    @Override
    public ProductType merge(PersistableProductType source, ProductType destination, StoreMerchantId store,
                             LanguageCode language) {
        return type(source, destination);
    }

    private ProductType type(PersistableProductType type, ProductType destination) {
        if (destination == null) {
            destination = new ProductType();
        }
        destination.setCode(type.getCode());
        destination.setId(type.getId());
        destination.setAllowAddToCart(type.isAllowAddToCart());
        destination.setVisible(type.isVisible());
        // destination.set

        List<com.asrevo.cvhome.catalog.entity.product.type.ProductTypeDescription> descriptions = new ArrayList<>();
        if (!CollectionUtils.isEmpty(type.getDescriptions())) {

            for (ProductTypeDescription d : type.getDescriptions()) {
                com.asrevo.cvhome.catalog.entity.product.type.ProductTypeDescription desc = typeDescription(d,
                        destination, d.getLanguage());
                descriptions.add(desc);
            }

            destination.setDescriptions(new HashSet<>(descriptions));
        }

        return destination;
    }

    private com.asrevo.cvhome.catalog.entity.product.type.ProductTypeDescription typeDescription(
            ProductTypeDescription description, ProductType typeModel, LanguageCode lang) {

        com.asrevo.cvhome.catalog.entity.product.type.ProductTypeDescription desc = null;
        if (!CollectionUtils.isEmpty(typeModel.getDescriptions())) {
            desc = this.findAppropriateDescription(typeModel, lang);
        }

        if (desc == null) {
            desc = new com.asrevo.cvhome.catalog.entity.product.type.ProductTypeDescription();
        }

        desc.setName(description.getName());
        desc.setDescription(description.getDescription());
        desc.setLanguageCode(description.getLanguage());
        desc.setProductType(typeModel);
        return desc;
    }

    private com.asrevo.cvhome.catalog.entity.product.type.ProductTypeDescription findAppropriateDescription(
            ProductType typeModel, LanguageCode lang) {
        java.util.Optional<com.asrevo.cvhome.catalog.entity.product.type.ProductTypeDescription> d = typeModel
                .getDescriptions()
                .stream()
                .filter(t -> t.getLanguageCode().equals(lang))
                .findFirst();
        return d.orElse(null);
    }

}
