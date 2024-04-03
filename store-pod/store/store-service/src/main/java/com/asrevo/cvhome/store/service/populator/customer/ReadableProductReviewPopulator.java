package com.asrevo.cvhome.store.service.populator.customer;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.customer.review.CustomerReview;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.customer.ReadableCustomerReview;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;

public class ReadableProductReviewPopulator extends AbstractDataPopulator<CustomerReview, ReadableCustomerReview> {

    @Override
    public ReadableCustomerReview populate(CustomerReview source, ReadableCustomerReview target, MerchantStore store,
                                           Language language) throws ConversionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ReadableCustomerReview createTarget() {
        // TODO Auto-generated method stub
        return null;
    }

}
