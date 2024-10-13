package com.asrevo.cvhome.store.service.populator.store;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.references.PersistableAddress;
import com.asrevo.cvhome.store.core.model.store.PersistableMerchantStore;
import com.asrevo.cvhome.store.core.services.merchant.MerchantStoreService;
import com.asrevo.cvhome.store.core.services.reference.country.CountryService;
import com.asrevo.cvhome.store.core.services.reference.currency.CurrencyService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.core.services.reference.zone.ZoneService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.DateUtil;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Getter
@Setter
public class PersistableMerchantStorePopulator
        extends AbstractDataPopulator<PersistableMerchantStore, MerchantStore> {

    private final CountryService countryService;
    private final ZoneService zoneService;
    private final LanguageService languageService;
    private final CurrencyService currencyService;
    private final MerchantStoreService merchantStoreService;

    public PersistableMerchantStorePopulator(
            CountryService countryService,
            ZoneService zoneService,
            LanguageService languageService,
            CurrencyService currencyService,
            MerchantStoreService merchantStoreService) {
        this.countryService = countryService;
        this.zoneService = zoneService;
        this.languageService = languageService;
        this.currencyService = currencyService;
        this.merchantStoreService = merchantStoreService;
    }

    @Override
    public MerchantStore populate(
            PersistableMerchantStore source,
            MerchantStore target,
            MerchantStore store,
            Language language)
            throws ConversionException {

        Assert.notNull(source, "PersistableMerchantStore mst not be null");

        if (target == null) {
            target = new MerchantStore();
        }

        target.setCode(source.getCode());
        if (source.getId() != 0) {
            target.setId(source.getId());
        }

        if (store.getStoreLogo() != null) {
            target.setStoreLogo(store.getStoreLogo());
        }

        if (store.getStoreBanner() != null) {
            target.setStoreBanner(store.getStoreBanner());
        }

        if (!StringUtils.isEmpty(source.getInBusinessSince())) {
            try {
                Date dt = DateUtil.getDate(source.getInBusinessSince());
                target.setInBusinessSince(dt);
            } catch (Exception e) {
                throw new ConversionException(
                        "Cannot parse date [" + source.getInBusinessSince() + "]", e);
            }
        }

        if (source.getDimension() != null) {
            target.setSeizeunitcode(source.getDimension().name());
        }
        if (source.getWeight() != null) {
            target.setWeightunitcode(source.getWeight().name());
        }
        target.setCurrencyFormatNational(source.isCurrencyFormatNational());
        target.setStorename(source.getName());
        if (source.getOrg() != null) {
            target.setOrg(source.getOrg());
        }
        target.setStorephone(source.getPhone());
        target.setStoreEmailAddress(source.getEmail());
        target.setUseCache(source.isUseCache());
        target.setRetailer(source.isRetailer());

        // get parent store
        if (!StringUtils.isBlank(source.getRetailerStore())) {
            if (source.getRetailerStore().equals(source.getCode())) {
                throw new ConversionException(
                        "Parent store ["
                                + source.getRetailerStore()
                                + "] cannot be parent of current store");
            }
            MerchantStore parent = merchantStoreService.getByCode(source.getRetailerStore());
            if (parent == null) {
                throw new ConversionException(
                        "Parent store [" + source.getRetailerStore() + "] does not exist");
            }
            target.setParent(parent);
        }

        try {

            if (!StringUtils.isEmpty(source.getDefaultLanguage())) {
                Language l = languageService.getByCode(source.getDefaultLanguage());
                target.setDefaultLanguage(l);
            }

            if (!StringUtils.isEmpty(source.getCurrency())) {
                Currency c = currencyService.getByCode(source.getCurrency());
                target.setCurrency(c);
            } else {
                target.setCurrency(
                        currencyService.getByCode(Constants.DEFAULT_CURRENCY.getCurrencyCode()));
            }

            List<String> languages = source.getSupportedLanguages();
            if (!CollectionUtils.isEmpty(languages)) {
                for (String lang : languages) {
                    Language ll = languageService.getByCode(lang);
                    target.getLanguages().add(ll);
                }
            }

        } catch (Exception e) {
            throw new ConversionException(e);
        }

        // address population
        PersistableAddress address = source.getAddress();
        if (address != null) {
            Country country;
            try {
                country = countryService.getByCode(address.getCountry());

                Zone zone = zoneService.getByCode(address.getStateProvince());
                if (zone != null) {
                    target.setZone(zone);
                } else {
                    target.setStorestateprovince(address.getStateProvince());
                }

                target.setStoreaddress(address.getAddress());
                target.setStorecity(address.getCity());
                target.setCountry(country);
                target.setStorepostalcode(address.getPostalCode());

            } catch (ServiceException e) {
                throw new ConversionException(e);
            }
        }

        if (StringUtils.isNotEmpty(source.getTemplate()))
            target.setStoreTemplate(source.getTemplate());

        return target;
    }

    @Override
    protected MerchantStore createTarget() {
        // TODO Auto-generated method stub
        return null;
    }
}
