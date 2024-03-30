package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOption;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.api.ReadableProductOptionEntity;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.api.ReadableProductOptionFull;
import org.springframework.stereotype.Component;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReadableProductOptionMapper implements Mapper<ProductOption, ReadableProductOptionEntity> {

    @Override
    public ReadableProductOptionEntity convert(ProductOption source, MerchantStore store,
                                               Language language) {
        ReadableProductOptionEntity destination = new ReadableProductOptionEntity();
        return merge(source, destination, store, language);
    }


    @Override
    public ReadableProductOptionEntity merge(ProductOption source,
                                             ReadableProductOptionEntity destination, MerchantStore store, Language language) {
        ReadableProductOptionEntity readableProductOption = new ReadableProductOptionEntity();
        if (language == null) {
            readableProductOption = new ReadableProductOptionFull();
            List<com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionDescription> descriptions = new ArrayList<com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionDescription>();
            for (ProductOptionDescription desc : source.getDescriptions()) {
                com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionDescription d = this.description(desc);
                descriptions.add(d);
            }
            ((ReadableProductOptionFull) readableProductOption).setDescriptions(descriptions);
        } else {
            readableProductOption = new ReadableProductOptionEntity();
            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                for (ProductOptionDescription desc : source.getDescriptions()) {
                    if (desc != null && desc.getLanguage() != null && desc.getLanguage().getId() == language.getId()) {
                        com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionDescription d = this.description(desc);
                        readableProductOption.setDescription(d);
                    }
                }
            }
        }

        readableProductOption.setCode(source.getCode());
        readableProductOption.setId(source.getId());
        readableProductOption.setType(source.getProductOptionType());


        return readableProductOption;
    }


    com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionDescription description(ProductOptionDescription description) {
        com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionDescription desc = new com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionDescription();
        desc.setDescription(description.getDescription());
        desc.setName(description.getName());
        desc.setId(description.getId());
        desc.setLanguage(description.getLanguage().getCode());
        return desc;
    }

}