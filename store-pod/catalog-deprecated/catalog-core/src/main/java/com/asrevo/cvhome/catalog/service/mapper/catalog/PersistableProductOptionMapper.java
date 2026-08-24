package com.asrevo.cvhome.catalog.service.mapper.catalog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotConvertibleException;
import com.asrevo.cvhome.catalog.model.product.attribute.api.PersistableProductOptionEntity;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;

@Component
public class PersistableProductOptionMapper implements Mapper<PersistableProductOptionEntity, ProductOption> {

    ProductOptionDescription description(
            com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionDescription description) {
        ProductOptionDescription desc = new ProductOptionDescription();
        desc.setId(null);
        desc.setDescription(description.getDescription());
        desc.setName(description.getName());
        if (description.getId() != null && description.getId() > 0) {
            desc.setId(description.getId());
        }
        desc.setLanguageCode(description.getLanguage());
        return desc;
    }

    @Override
    public ProductOption convert(PersistableProductOptionEntity source, StoreMerchantId store, LanguageCode language)
            throws ProductOptionNotConvertibleException {
        ProductOption destination = new ProductOption();
        return merge(source, destination, store, language);
    }

    private ProductOptionDescription findMatchingDescription(
            com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionDescription desc,
            ProductOption destination) {
        if (CollectionUtils.isEmpty(destination.getDescriptions())) {
            return null;
        }
        for (ProductOptionDescription d : destination.getDescriptions()) {
            if (StringUtils.isBlank(desc.getLanguage().code()) || !desc.getLanguage().equals(d.getLanguageCode())) {
                continue;
            }
            d.setDescription(desc.getDescription());
            d.setName(desc.getName());
            d.setTitle(desc.getTitle());
            return d;
        }
        return null;
    }

    @Override
    public ProductOption merge(PersistableProductOptionEntity source, ProductOption destination, StoreMerchantId store,
                               LanguageCode language)
            throws ProductOptionNotConvertibleException {
        if (destination == null) {
            destination = new ProductOption();
        }

        try {

            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                for (com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionDescription desc : source
                        .getDescriptions()) {
                    ProductOptionDescription description = findMatchingDescription(desc, destination);
                    if (description == null) {
                        description = description(desc);
                        description.setProductOption(destination);
                        destination.getDescriptions().add(description);
                    }
                }
            }

            destination.setCode(source.getCode());
            destination.setStoreMerchantId(store);
            destination.setProductOptionSortOrder(source.getOrder());
            destination.setProductOptionType(source.getType());
            destination.setReadOnly(source.isReadOnly());

            return destination;
        } catch (Exception e) {
            throw ProductOptionNotConvertibleException.of(e);
        }
    }

}
