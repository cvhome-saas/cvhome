package com.asrevo.cvhome.store.service.populator.customer;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionValue;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionValueDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.customer.attribute.PersistableCustomerOptionValue;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.util.HashSet;
import java.util.Set;

public class PersistableCustomerOptionValuePopulator extends
        AbstractDataPopulator<PersistableCustomerOptionValue, CustomerOptionValue> {


    private LanguageService languageService;

    @Override
    public CustomerOptionValue populate(PersistableCustomerOptionValue source,
                                        CustomerOptionValue target, MerchantStore store, Language language)
            throws ConversionException {


        Validate.notNull(languageService, "Requires to set LanguageService");


        try {

            target.setCode(source.getCode());
            target.setMerchantStore(store);
            target.setSortOrder(source.getOrder());

            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                Set<CustomerOptionValueDescription> descriptions = new HashSet<>();
                for (com.asrevo.cvhome.store.core.model.customer.attribute.CustomerOptionValueDescription desc : source.getDescriptions()) {
                    CustomerOptionValueDescription description = new CustomerOptionValueDescription();
                    Language lang = languageService.getByCode(desc.getLanguage());
                    if (lang == null) {
                        throw new ConversionException("Language is null for code " + description.getLanguage() + " use language ISO code [en, fr ...]");
                    }
                    description.setLanguage(lang);
                    description.setName(desc.getName());
                    description.setTitle(desc.getTitle());
                    description.setCustomerOptionValue(target);
                    descriptions.add(description);
                }
                target.setDescriptions(descriptions);
            }

        } catch (Exception e) {
            throw new ConversionException(e);
        }
        return target;
    }

    @Override
    protected CustomerOptionValue createTarget() {
        return null;
    }

    public LanguageService getLanguageService() {
        return languageService;
    }

    public void setLanguageService(LanguageService languageService) {
        this.languageService = languageService;
    }

}
