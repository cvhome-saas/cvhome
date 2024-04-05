package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.PersistableProductOptionValue;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class PersistableProductOptionValueMapper
        implements Mapper<PersistableProductOptionValue, ProductOptionValue> {

    private final LanguageService languageService;

    public PersistableProductOptionValueMapper(LanguageService languageService) {
        this.languageService = languageService;
    }

    ProductOptionValueDescription description(
            com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionValueDescription description)
            throws Exception {
        Assert.notNull(description.getLanguage(), "description.language should not be null");
        ProductOptionValueDescription desc = new ProductOptionValueDescription();
        desc.setId(null);
        desc.setDescription(description.getDescription());
        desc.setName(description.getName());
        if (StringUtils.isBlank(desc.getName())) {
            desc.setName(description.getDescription());
        }
        if (description.getId() != null && description.getId().longValue() > 0) {
            desc.setId(description.getId());
        }
        Language lang = languageService.getByCode(description.getLanguage());
        desc.setLanguage(lang);
        return desc;
    }

    @Override
    public ProductOptionValue merge(PersistableProductOptionValue source, ProductOptionValue destination,
                                    MerchantStore store, Language language) {
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
                for (com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionValueDescription desc : source
                        .getDescriptions()) {
                    ProductOptionValueDescription description = null;
                    if (!CollectionUtils.isEmpty(destination.getDescriptions())) {
                        for (ProductOptionValueDescription d : destination.getDescriptions()) {
                            if (!StringUtils.isBlank(desc.getLanguage())
                                    && desc.getLanguage().equals(d.getLanguage().getCode())) {

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
                    } //else {
                    if (description == null) {
                        description = description(desc);
                        description.setProductOptionValue(destination);
                        destination.getDescriptions().add(description);
                    }
                    //description = description(desc);
                    //description.setProductOptionValue(destination);
                    //}
                    //destination.getDescriptions().add(description);
                }
            }

            destination.setCode(source.getCode());
            destination.setMerchantStore(store);
            destination.setProductOptionValueSortOrder(source.getSortOrder());


            return destination;
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while converting product option", e);
        }
    }

    @Override
    public ProductOptionValue convert(PersistableProductOptionValue source, MerchantStore store,
                                      Language language) {
        ProductOptionValue destination = new ProductOptionValue();
        return merge(source, destination, store, language);
    }


}