package com.asrevo.cvhome.store.service.populator.customer;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.customer.review.CustomerReview;
import com.asrevo.cvhome.store.core.entity.customer.review.CustomerReviewDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.customer.ReadableCustomer;
import com.asrevo.cvhome.store.core.model.customer.ReadableCustomerReview;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.DateUtil;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class ReadableCustomerReviewPopulator
        extends AbstractDataPopulator<CustomerReview, ReadableCustomerReview> {

    @Override
    public ReadableCustomerReview populate(
            CustomerReview source,
            ReadableCustomerReview target,
            MerchantStore store,
            Language language)
            throws ConversionException {

        try {

            if (target == null) {
                target = new ReadableCustomerReview();
            }

            if (source.getReviewDate() != null) {
                target.setDate(DateUtil.formatDate(source.getReviewDate()));
            }

            ReadableCustomer reviewed = new ReadableCustomer();
            reviewed.setId(source.getReviewedCustomer().getId());
            reviewed.setFirstName(source.getReviewedCustomer().getBilling().getFirstName());
            reviewed.setLastName(source.getReviewedCustomer().getBilling().getLastName());

            target.setId(source.getId());
            target.setCustomerId(source.getCustomer().getId());
            target.setReviewedCustomer(reviewed);
            target.setRating(source.getReviewRating());
            target.setReviewedCustomer(reviewed);
            target.setCustomerId(source.getCustomer().getId());

            Set<CustomerReviewDescription> descriptions = source.getDescriptions();
            if (CollectionUtils.isNotEmpty(descriptions)) {
                CustomerReviewDescription description = null;
                if (descriptions.size() > 1) {
                    for (CustomerReviewDescription desc : descriptions) {
                        if (desc.getLanguage().getCode().equals(language.getCode())) {
                            description = desc;
                            break;
                        }
                    }
                } else {
                    description = descriptions.iterator().next();
                }

                if (description != null) {
                    target.setDescription(description.getDescription());
                    target.setLanguage(description.getLanguage().getCode());
                }
            }

        } catch (Exception e) {
            throw new ConversionException("Cannot populate ReadableCustomerReview", e);
        }

        return target;
    }

    @Override
    protected ReadableCustomerReview createTarget() {
        // TODO Auto-generated method stub
        return null;
    }
}
