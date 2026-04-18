package com.asrevo.cvhome.catalog.service.mapper.catalog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductOptionValue;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

@Component
public class PersistableProductOptionValueMapper implements Mapper<PersistableProductOptionValue, ProductOptionValue> {

    public PersistableProductOptionValueMapper() {
    }

    ProductOptionValueDescription description(
            com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription description) {
        ProductOptionValueDescription desc = new ProductOptionValueDescription();
        desc.setId(null);
        desc.setDescription(description.getDescription());
        desc.setName(description.getName());
        if (StringUtils.isBlank(desc.getName())) {
            desc.setName(description.getDescription());
        }
        if (description.getId() != null && description.getId() > 0) {
            desc.setId(description.getId());
        }
        desc.setLanguageCode(description.getLanguage());
        return desc;
    }

    @Override
    public ProductOptionValue merge(PersistableProductOptionValue source, ProductOptionValue destination,
                                    StoreMerchantId store, LanguageCode language) {
        if (destination == null) {
            destination = new ProductOptionValue();
        }

        try {

            if (StringUtils.isBlank(source.getCode())) {
                if (!StringUtils.isBlank(destination.getCode())) {
                    source.setCode(destination.getCode());
                }
            }

            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                for (com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription desc : source
                        .getDescriptions()) {
                    ProductOptionValueDescription description = null;
                    if (!CollectionUtils.isEmpty(destination.getDescriptions())) {
                        for (ProductOptionValueDescription d : destination.getDescriptions()) {
                            if (!StringUtils.isBlank(desc.getLanguage().code())
                                    && desc.getLanguage().equals(d.getLanguageCode())) {

                                d.setDescription(desc.getDescription());
                                d.setName(desc.getName());
                                d.setTitle(desc.getTitle());
                                if (StringUtils.isBlank(d.getName())) {
                                    d.setName(d.getDescription());
                                }
                                description = d;
                                break;
                            }
                        }
                    }
                    if (description == null) {
                        description = description(desc);
                        description.setProductOptionValue(destination);
                        destination.getDescriptions().add(description);
                    }
                }
            }

            destination.setCode(source.getCode());
            destination.setStoreMerchantId(store);
            destination.setProductOptionValueSortOrder(source.getSortOrder());

            return destination;
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while converting product option", e);
        }
    }

    @Override
    public ProductOptionValue convert(PersistableProductOptionValue source, StoreMerchantId store,
                                      LanguageCode language) {
        ProductOptionValue destination = new ProductOptionValue();
        return merge(source, destination, store, language);
    }

}
