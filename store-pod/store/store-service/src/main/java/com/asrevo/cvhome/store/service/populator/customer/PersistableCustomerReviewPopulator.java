package com.asrevo.cvhome.store.service.populator.customer;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.customer.review.CustomerReview;
import com.asrevo.cvhome.store.core.entity.customer.review.CustomerReviewDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.customer.PersistableCustomerReview;
import com.asrevo.cvhome.store.core.services.customer.CustomerService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.DateUtil;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PersistableCustomerReviewPopulator extends AbstractDataPopulator<PersistableCustomerReview, CustomerReview> {

    private CustomerService customerService;

    private LanguageService languageService;

    public LanguageService getLanguageService() {
        return languageService;
    }

    public void setLanguageService(LanguageService languageService) {
        this.languageService = languageService;
    }

    @Override
    public CustomerReview populate(PersistableCustomerReview source, CustomerReview target, MerchantStore store,
                                   Language language) throws ConversionException {

        Assert.notNull(customerService, "customerService cannot be null");
        Assert.notNull(languageService, "languageService cannot be null");
        Assert.notNull(source.getRating(), "Rating cannot bot be null");

        try {

            if (target == null) {
                target = new CustomerReview();
            }

            if (source.getDate() == null) {
                String date = DateUtil.formatDate(new Date());
                source.setDate(date);
            }
            target.setReviewDate(DateUtil.getDate(source.getDate()));

            if (source.getId() != null && source.getId().longValue() == 0) {
                source.setId(null);
            } else {
                target.setId(source.getId());
            }


            Customer reviewer = customerService.getById(source.getCustomerId());
            Customer reviewed = customerService.getById(source.getReviewedCustomer());

            target.setReviewRating(source.getRating());

            target.setCustomer(reviewer);
            target.setReviewedCustomer(reviewed);

            Language lang = languageService.getByCode(language.getCode());
            if (lang == null) {
                throw new ConversionException("Invalid language code, use iso codes (en, fr ...)");
            }

            CustomerReviewDescription description = new CustomerReviewDescription();
            description.setDescription(source.getDescription());
            description.setLanguage(lang);
            description.setName("-");
            description.setCustomerReview(target);

            Set<CustomerReviewDescription> descriptions = new HashSet<CustomerReviewDescription>();
            descriptions.add(description);

            target.setDescriptions(descriptions);

        } catch (Exception e) {
            throw new ConversionException("Cannot populate CustomerReview", e);
        }


        return target;
    }

    @Override
    protected CustomerReview createTarget() {
        // TODO Auto-generated method stub
        return null;
    }

    public CustomerService getCustomerService() {
        return customerService;
    }

    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

}
