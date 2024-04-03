package com.asrevo.cvhome.store.service.populator.customer;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOption;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.customer.attribute.CustomerOptionDescription;
import com.asrevo.cvhome.store.core.model.customer.attribute.PersistableCustomerOption;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

public class PersistableCustomerOptionPopulator extends
        AbstractDataPopulator<PersistableCustomerOption, CustomerOption> {


    private LanguageService languageService;

    @Override
    public CustomerOption populate(PersistableCustomerOption source,
                                   CustomerOption target, MerchantStore store, Language language)
            throws ConversionException {


        Assert.notNull(languageService, "Requires to set LanguageService");


        try {

            target.setCode(source.getCode());
            target.setMerchantStore(store);
            target.setSortOrder(source.getOrder());
            if (!StringUtils.isBlank(source.getType())) {
                target.setCustomerOptionType(source.getType());
            } else {
                target.setCustomerOptionType("TEXT");
            }
            target.setPublicOption(true);

            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                Set<com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionDescription> descriptions = new HashSet<>();
                for (CustomerOptionDescription desc : source.getDescriptions()) {
                    com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionDescription description = new com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionDescription();
                    Language lang = languageService.getByCode(desc.getLanguage());
                    if (lang == null) {
                        throw new ConversionException("Language is null for code " + description.getLanguage() + " use language ISO code [en, fr ...]");
                    }
                    description.setLanguage(lang);
                    description.setName(desc.getName());
                    description.setTitle(desc.getTitle());
                    description.setCustomerOption(target);
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
    protected CustomerOption createTarget() {
        return null;
    }

    public LanguageService getLanguageService() {
        return languageService;
    }

    public void setLanguageService(LanguageService languageService) {
        this.languageService = languageService;
    }

}
