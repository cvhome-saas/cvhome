package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.type.PersistableProductType;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ProductTypeDescription;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


@Component
public class PersistableProductTypeMapper implements Mapper<PersistableProductType, ProductType> {

    private final LanguageService languageService;

    public PersistableProductTypeMapper(LanguageService languageService) {
        this.languageService = languageService;
    }

    @Override
    public ProductType convert(PersistableProductType source, MerchantStore store, Language language) {
        ProductType type = new ProductType();
        return this.merge(source, type, store, language);
    }

    @Override
    public ProductType merge(PersistableProductType source, ProductType destination, MerchantStore store,
                             Language language) {
        Assert.notNull(destination, "ReadableProductType cannot be null");
        try {
            return type(source, destination);
        } catch (ServiceException e) {
            throw new ConversionRuntimeException(e);
        }
    }

    private ProductType type(PersistableProductType type, ProductType destination) throws ServiceException {
        if (destination == null) {
            destination = new ProductType();
        }
        destination.setCode(type.getCode());
        destination.setId(type.getId());
        destination.setAllowAddToCart(type.isAllowAddToCart());
        destination.setVisible(type.isVisible());
        //destination.set

        List<com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductTypeDescription> descriptions = new ArrayList<>();
        if (!CollectionUtils.isEmpty(type.getDescriptions())) {

            for (ProductTypeDescription d : type.getDescriptions()) {
                com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductTypeDescription desc = typeDescription(d, destination, d.getLanguage());
                descriptions.add(desc);


            }

            destination.setDescriptions(new HashSet<>(descriptions));

        }

        return destination;
    }

    private com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductTypeDescription typeDescription(ProductTypeDescription description, ProductType typeModel, String lang) throws ServiceException {

        com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductTypeDescription desc = null;
        if (!CollectionUtils.isEmpty(typeModel.getDescriptions())) {
            desc = this.findAppropriateDescription(typeModel, lang);
        }

        if (desc == null) {
            desc = new com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductTypeDescription();
        }

        desc.setName(description.getName());
        desc.setDescription(description.getDescription());
        desc.setLanguage(languageService.getByCode(description.getLanguage()));
        desc.setProductType(typeModel);
        return desc;
    }

    private com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductTypeDescription findAppropriateDescription(ProductType typeModel, String lang) {
        java.util.Optional<com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductTypeDescription> d = typeModel.getDescriptions().stream().filter(t -> t.getLanguage().getCode().equals(lang)).findFirst();
        return d.orElse(null);
    }

}
