package com.asrevo.cvhome.catalog.service.mapper.catalog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionFull;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;

@Component
public class ReadableProductOptionMapper implements Mapper<ProductOption, ReadableProductOptionEntity> {

    @Override
    public ReadableProductOptionEntity convert(ProductOption source, StoreMerchantId store, LanguageCode language) {
        ReadableProductOptionEntity destination = new ReadableProductOptionEntity();
        return merge(source, destination, store, language);
    }

    @Override
    public ReadableProductOptionEntity merge(ProductOption source, ReadableProductOptionEntity destination,
                                             StoreMerchantId store, LanguageCode language) {
        ReadableProductOptionEntity readableProductOption = new ReadableProductOptionEntity();
        if (language == null) {
            List<com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionDescription> descriptions = new ArrayList<>();
            for (ProductOptionDescription desc : source.getDescriptions()) {
                com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionDescription d = this.description(desc);
                descriptions.add(d);
            }
            ((ReadableProductOptionFull) readableProductOption).setDescriptions(descriptions);
        } else {
            readableProductOption = new ReadableProductOptionEntity();
            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                for (ProductOptionDescription desc : source.getDescriptions()) {
                    if (desc != null && desc.getLanguageCode().equals(language)) {
                        com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionDescription d = this
                                .description(desc);
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

    com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionDescription description(
            ProductOptionDescription description) {
        com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionDescription desc =
                new com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionDescription();
        desc.setDescription(description.getDescription());
        desc.setName(description.getName());
        desc.setId(description.getId());
        desc.setLanguage(description.getLanguageCode());
        return desc;
    }

}
