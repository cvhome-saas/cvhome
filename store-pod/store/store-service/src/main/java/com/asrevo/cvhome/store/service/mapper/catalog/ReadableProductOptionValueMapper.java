package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.api.ReadableProductOptionValue;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.api.ReadableProductOptionValueFull;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ReadableProductOptionValueMapper
        implements Mapper<ProductOptionValue, ReadableProductOptionValue> {

    private final ImageFilePath imageUtils;

    public ReadableProductOptionValueMapper(ImageFilePath imageUtils) {
        this.imageUtils = imageUtils;
    }

    @Override
    public ReadableProductOptionValue merge(
            ProductOptionValue source,
            ReadableProductOptionValue destination,
            MerchantStore store,
            Language language) {
        ReadableProductOptionValue readableProductOptionValue = new ReadableProductOptionValue();
        if (language == null) {
            readableProductOptionValue = new ReadableProductOptionValueFull();
            List<
                            com.asrevo.cvhome.store.core.model.catalog.product.attribute
                                    .ProductOptionValueDescription>
                    descriptions = new ArrayList<>();
            for (ProductOptionValueDescription desc : source.getDescriptions()) {
                com.asrevo.cvhome.store.core.model.catalog.product.attribute
                                .ProductOptionValueDescription
                        d = this.description(desc);
                descriptions.add(d);
            }
            ((ReadableProductOptionValueFull) readableProductOptionValue)
                    .setDescriptions(descriptions);
        } else {
            readableProductOptionValue = new ReadableProductOptionValue();
            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                for (ProductOptionValueDescription desc : source.getDescriptions()) {
                    if (desc != null
                            && desc.getLanguage() != null
                            && desc.getLanguage().getId().equals(language.getId())) {
                        com.asrevo.cvhome.store.core.model.catalog.product.attribute
                                        .ProductOptionValueDescription
                                d = this.description(desc);
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
            readableProductOptionValue.setImage(
                    imageUtils.buildProductPropertyImageUtils(
                            store, source.getProductOptionValueImage()));
        }

        return readableProductOptionValue;
    }

    com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionValueDescription
            description(ProductOptionValueDescription description) {
        com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionValueDescription
                desc =
                        new com.asrevo.cvhome.store.core.model.catalog.product.attribute
                                .ProductOptionValueDescription();
        desc.setDescription(description.getDescription());
        desc.setName(description.getName());
        desc.setId(description.getId());
        desc.setLanguage(description.getLanguage().getCode());
        return desc;
    }

    @Override
    public ReadableProductOptionValue convert(
            ProductOptionValue source, MerchantStore store, Language language) {
        ReadableProductOptionValue destination = new ReadableProductOptionValue();
        return merge(source, destination, store, language);
    }
}
