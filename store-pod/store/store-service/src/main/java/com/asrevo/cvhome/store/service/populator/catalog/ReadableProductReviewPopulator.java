package com.asrevo.cvhome.store.service.populator.catalog;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.catalog.product.review.ProductReview;
import com.asrevo.cvhome.store.core.entity.catalog.product.review.ProductReviewDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductReview;
import com.asrevo.cvhome.store.core.model.customer.ReadableCustomer;
import com.asrevo.cvhome.store.service.populator.customer.ReadableCustomerPopulator;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.DateUtil;
import java.util.Set;

public class ReadableProductReviewPopulator
        extends AbstractDataPopulator<ProductReview, ReadableProductReview> {

    @Override
    public ReadableProductReview populate(
            ProductReview source,
            ReadableProductReview target,
            MerchantStore store,
            Language language)
            throws ConversionException {

        try {
            ReadableCustomerPopulator populator = new ReadableCustomerPopulator();
            ReadableCustomer customer = new ReadableCustomer();
            populator.populate(source.getCustomer(), customer, store, language);

            target.setId(source.getId());
            target.setDate(DateUtil.formatDate(source.getReviewDate()));
            target.setCustomer(customer);
            target.setRating(source.getReviewRating());
            target.setProductId(source.getProduct().getId());

            Set<ProductReviewDescription> descriptions = source.getDescriptions();
            if (descriptions != null) {
                for (ProductReviewDescription description : descriptions) {
                    target.setDescription(description.getDescription());
                    target.setLanguage(description.getLanguage().getCode());
                    break;
                }
            }

            return target;

        } catch (Exception e) {
            throw new ConversionException("Cannot populate ProductReview", e);
        }
    }

    @Override
    protected ReadableProductReview createTarget() {
        return null;
    }
}
