package com.asrevo.cvhome.store.service.populator.store;


import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.content.ReadableImage;
import com.asrevo.cvhome.store.core.model.entity.ReadableAudit;
import com.asrevo.cvhome.store.core.model.references.MeasureUnit;
import com.asrevo.cvhome.store.core.model.references.ReadableAddress;
import com.asrevo.cvhome.store.core.model.references.ReadableLanguage;
import com.asrevo.cvhome.store.core.model.references.WeightUnit;
import com.asrevo.cvhome.store.core.model.store.ReadableMerchantStore;
import com.asrevo.cvhome.store.core.services.reference.country.CountryService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.core.services.reference.zone.ZoneService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.DateUtil;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Populates MerchantStore core entity model object
 *
 * @author carlsamson
 */
@Component
public class ReadableMerchantStorePopulator extends
        AbstractDataPopulator<MerchantStore, ReadableMerchantStore> {

    protected final Log logger = LogFactory.getLog(getClass());

    private final CountryService countryService;
    private final ZoneService zoneService;
    private final ImageFilePath filePath;
    private final LanguageService languageService;

    public ReadableMerchantStorePopulator(CountryService countryService, ZoneService zoneService, @Qualifier("img") ImageFilePath filePath, LanguageService languageService) {
        this.countryService = countryService;
        this.zoneService = zoneService;
        this.filePath = filePath;
        this.languageService = languageService;
    }


    @Override
    public ReadableMerchantStore populate(MerchantStore source,
                                          ReadableMerchantStore target, MerchantStore store, Language language)
            throws ConversionException {
        Assert.notNull(countryService, "Must use setter for countryService");
        Assert.notNull(zoneService, "Must use setter for zoneService");

        if (target == null) {
            target = new ReadableMerchantStore();
        }

        target.setId(source.getId());
        target.setCode(source.getCode());
        if (source.getDefaultLanguage() != null) {
            target.setDefaultLanguage(source.getDefaultLanguage().getCode());
        }

        target.setCurrency(source.getCurrency().getCode());
        target.setPhone(source.getStorephone());

        ReadableAddress address = new ReadableAddress();
        address.setAddress(source.getStoreaddress());
        address.setCity(source.getStorecity());
        if (source.getCountry() != null) {
            try {
                address.setCountry(source.getCountry().getIsoCode());
                Country c = countryService.getCountriesMap(language).get(source.getCountry().getIsoCode());
                if (c != null) {
                    address.setCountry(c.getIsoCode());
                }
            } catch (ServiceException e) {
                logger.error("Cannot get Country", e);
            }
        }

        if (source.getParent() != null) {
            ReadableMerchantStore parent = populate(source.getParent(),
                    new ReadableMerchantStore(), source, language);
            target.setParent(parent);
        }

        if (target.getParent() == null) {
            target.setRetailer(true);
        } else {
            target.setRetailer(source.isRetailer());
        }


        target.setDimension(MeasureUnit.valueOf(source.getSeizeunitcode()));
        target.setWeight(WeightUnit.valueOf(source.getWeightunitcode()));

        if (source.getZone() != null) {
            address.setStateProvince(source.getZone().getCode());
            try {
                Zone z = zoneService.getZones(language).get(source.getZone().getCode());
                address.setStateProvince(z.getCode());
            } catch (ServiceException e) {
                logger.error("Cannot get Zone", e);
            }
        }


        if (!StringUtils.isBlank(source.getStorestateprovince())) {
            address.setStateProvince(source.getStorestateprovince());
        }

        if (!StringUtils.isBlank(source.getStoreLogo())) {
            ReadableImage image = new ReadableImage();
            image.setName(source.getStoreLogo());
            if (filePath != null) {
                image.setPath(filePath.buildStoreLogoFilePath(source));
            }
            target.setLogo(image);
        }

        address.setPostalCode(source.getStorepostalcode());

        target.setAddress(address);

        target.setCurrencyFormatNational(source.isCurrencyFormatNational());
        target.setEmail(source.getStoreEmailAddress());
        target.setName(source.getStorename());
        target.setOrg(source.getOrg());
        target.setId(source.getId());
        target.setInBusinessSince(DateUtil.formatDate(source.getInBusinessSince()));
        target.setUseCache(source.isUseCache());

        if (!CollectionUtils.isEmpty(source.getLanguages())) {
            List<ReadableLanguage> supported = new ArrayList<ReadableLanguage>();
            for (Language lang : source.getLanguages()) {
                try {
                    Language langObject = languageService.getLanguagesMap().get(lang.getCode());
                    if (langObject != null) {
                        ReadableLanguage l = new ReadableLanguage();
                        l.setId(langObject.getId());
                        l.setCode(langObject.getCode());
                        supported.add(l);
                    }

                } catch (ServiceException e) {
                    logger.error("Cannot get Language [" + lang.getId() + "]");
                }

            }
            target.setSupportedLanguages(supported);
        }

        if (source.getAuditSection() != null) {
            ReadableAudit audit = new ReadableAudit();
            if (source.getAuditSection().getDateCreated() != null) {
                audit.setCreated(DateUtil.formatDate(source.getAuditSection().getDateCreated()));
            }
            if (source.getAuditSection().getDateModified() != null) {
                audit.setModified(DateUtil.formatDate(source.getAuditSection().getDateCreated()));
            }
            audit.setUser(source.getAuditSection().getModifiedBy());
            target.setReadableAudit(audit);
        }

        return target;
    }

    @Override
    protected ReadableMerchantStore createTarget() {
        // TODO Auto-generated method stub
        return null;
    }


}
