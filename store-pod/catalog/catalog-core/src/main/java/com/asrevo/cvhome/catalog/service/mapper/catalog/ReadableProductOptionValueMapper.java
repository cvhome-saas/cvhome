package com.asrevo.cvhome.catalog.service.mapper.catalog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValueFull;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.utils.ImageFilePath;

@Component
public class ReadableProductOptionValueMapper implements Mapper<ProductOptionValue, ReadableProductOptionValue> {

    private final ImageFilePath imageUtils;

    public ReadableProductOptionValueMapper(ImageFilePath imageUtils) {
        this.imageUtils = imageUtils;
    }

    @Override
    public ReadableProductOptionValue merge(ProductOptionValue source, ReadableProductOptionValue destination,
                                            StoreMerchantId store, LanguageCode language) {
        ReadableProductOptionValue readableProductOptionValue = new ReadableProductOptionValue();
        if (language == null) {
            List<com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription> descriptions = new ArrayList<>();
            for (ProductOptionValueDescription desc : source.getDescriptions()) {
                com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription d = this
                        .description(desc);
                descriptions.add(d);
            }
            ((ReadableProductOptionValueFull) readableProductOptionValue).setDescriptions(descriptions);
        } else {
            readableProductOptionValue = new ReadableProductOptionValue();
            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                for (ProductOptionValueDescription desc : source.getDescriptions()) {
                    if (desc != null && desc.getLanguageCode().equals(language)) {
                        com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription d = this
                                .description(desc);
                        readableProductOptionValue.setDescription(d);
                    }
                }
            }
        }

        readableProductOptionValue.setCode(source.getCode());
        if (source.getId() != null) {
            readableProductOptionValue.setId(source.getId());
        }
        if (source.getProductOptionValueSortOrder() != null) {
            readableProductOptionValue.setOrder(source.getProductOptionValueSortOrder());
        }
        if (!StringUtils.isBlank(source.getProductOptionValueImage())) {
            readableProductOptionValue
                    .setImage(imageUtils.buildProductPropertyImageUtils(store, source.getProductOptionValueImage()));
        }

        return readableProductOptionValue;
    }

    com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription description(
            ProductOptionValueDescription description) {
        com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription desc =
                new com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription();
        desc.setDescription(description.getDescription());
        desc.setName(description.getName());
        desc.setId(description.getId());
        desc.setLanguage(description.getLanguageCode());
        return desc;
    }

    @Override
    public ReadableProductOptionValue convert(ProductOptionValue source, StoreMerchantId store, LanguageCode language) {
        ReadableProductOptionValue destination = new ReadableProductOptionValue();
        return merge(source, destination, store, language);
    }

}
