package com.asrevo.cvhome.store.service.populator.customer;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.customer.CustomerGender;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerAttribute;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionValue;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.model.customer.PersistableCustomer;
import com.asrevo.cvhome.store.core.model.customer.address.Address;
import com.asrevo.cvhome.store.core.model.customer.attribute.PersistableCustomerAttribute;
import com.asrevo.cvhome.store.core.services.customer.attribute.CustomerOptionService;
import com.asrevo.cvhome.store.core.services.customer.attribute.CustomerOptionValueService;
import com.asrevo.cvhome.store.core.services.reference.country.CountryService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.core.services.reference.zone.ZoneService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import java.math.BigDecimal;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerPopulator extends AbstractDataPopulator<PersistableCustomer, Customer> {

    private final CountryService countryService;
    private final ZoneService zoneService;
    private final LanguageService languageService;
    private final CustomerOptionService customerOptionService;
    private final CustomerOptionValueService customerOptionValueService;

    public CustomerPopulator(
            CountryService countryService,
            ZoneService zoneService,
            LanguageService languageService,
            CustomerOptionService customerOptionService,
            CustomerOptionValueService customerOptionValueService) {
        this.countryService = countryService;
        this.zoneService = zoneService;
        this.languageService = languageService;
        this.customerOptionService = customerOptionService;
        this.customerOptionValueService = customerOptionValueService;
    }

    /**
     * Creates a Customer entity ready to be saved
     */
    @Override
    public Customer populate(
            PersistableCustomer source, Customer target, MerchantStore store, Language language)
            throws ConversionException {

        try {

            if (source.getId() != null && source.getId() > 0) {
                target.setId(source.getId());
            }

            if (!StringUtils.isBlank(source.getPassword())) {
                target.setPassword(
                        /*@TODO ASHRAF passwordEncoder.encode(source.getPassword())*/ source
                                .getPassword());
                target.setNick(source.getUserName());
                target.setAnonymous(false);
            }

            if (source.getBilling() != null) {
                target.setBilling(new Billing());
                if (!StringUtils.isEmpty(source.getFirstName())) {
                    target.getBilling().setFirstName(source.getFirstName());
                }
                if (!StringUtils.isEmpty(source.getLastName())) {
                    target.getBilling().setLastName(source.getLastName());
                }
            }

            if (!StringUtils.isBlank(source.getProvider())) {
                target.setProvider(source.getProvider());
            }

            if (!StringUtils.isBlank(source.getEmailAddress())) {
                target.setEmailAddress(source.getEmailAddress());
            }

            if (source.getGender() != null && target.getGender() == null) {
                target.setGender(CustomerGender.valueOf(source.getGender()));
            }
            if (target.getGender() == null) {
                target.setGender(CustomerGender.M);
            }

            Map<String, Country> countries = countryService.getCountriesMap(language);
            Map<String, Zone> zones = zoneService.getZones(language);

            target.setMerchantStore(store);

            Address sourceBilling = source.getBilling();
            if (sourceBilling != null) {
                Billing billing = target.getBilling();
                billing.setAddress(sourceBilling.getAddress());
                billing.setCity(sourceBilling.getCity());
                billing.setCompany(sourceBilling.getCompany());
                // billing.setCountry(country);
                if (!StringUtils.isEmpty(sourceBilling.getFirstName()))
                    billing.setFirstName(sourceBilling.getFirstName());
                if (!StringUtils.isEmpty(sourceBilling.getLastName()))
                    billing.setLastName(sourceBilling.getLastName());
                billing.setTelephone(sourceBilling.getPhone());
                billing.setPostalCode(sourceBilling.getPostalCode());
                billing.setState(sourceBilling.getStateProvince());
                Country billingCountry = null;
                if (!StringUtils.isBlank(sourceBilling.getCountry())) {
                    billingCountry = countries.get(sourceBilling.getCountry());
                    if (billingCountry == null) {
                        throw new ConversionException(
                                "Unsuported country code " + sourceBilling.getCountry());
                    }
                    billing.setCountry(billingCountry);
                }

                if (billingCountry != null && !StringUtils.isBlank(sourceBilling.getZone())) {
                    Zone zone = zoneService.getByCode(sourceBilling.getZone());
                    if (zone == null) {
                        throw new ConversionException(
                                "Unsuported zone code " + sourceBilling.getZone());
                    }
                    Zone zoneDescription = zones.get(zone.getCode());
                    billing.setZone(zoneDescription);
                }
                // target.setBilling(billing);

            }
            if (target.getBilling() == null && source.getBilling() != null) {
                log.info("Setting default values for billing");
                Billing billing = new Billing();
                Country billingCountry = null;
                if (StringUtils.isNotBlank(source.getBilling().getCountry())) {
                    billingCountry = countries.get(source.getBilling().getCountry());
                    if (billingCountry == null) {
                        throw new ConversionException(
                                "Unsuported country code " + sourceBilling.getCountry());
                    }
                    billing.setCountry(billingCountry);
                    target.setBilling(billing);
                }
            }
            Address sourceShipping = source.getDelivery();
            if (sourceShipping != null) {
                Delivery delivery = new Delivery();
                delivery.setAddress(sourceShipping.getAddress());
                delivery.setCity(sourceShipping.getCity());
                delivery.setCompany(sourceShipping.getCompany());
                delivery.setFirstName(sourceShipping.getFirstName());
                delivery.setLastName(sourceShipping.getLastName());
                delivery.setTelephone(sourceShipping.getPhone());
                delivery.setPostalCode(sourceShipping.getPostalCode());
                delivery.setState(sourceShipping.getStateProvince());
                Country deliveryCountry = null;

                if (!StringUtils.isBlank(sourceShipping.getCountry())) {
                    deliveryCountry = countries.get(sourceShipping.getCountry());
                    if (deliveryCountry == null) {
                        throw new ConversionException(
                                "Unsuported country code " + sourceShipping.getCountry());
                    }
                    delivery.setCountry(deliveryCountry);
                }

                if (deliveryCountry != null && !StringUtils.isBlank(sourceShipping.getZone())) {
                    Zone zone = zoneService.getByCode(sourceShipping.getZone());
                    if (zone == null) {
                        throw new ConversionException(
                                "Unsuported zone code " + sourceShipping.getZone());
                    }
                    Zone zoneDescription = zones.get(zone.getCode());
                    delivery.setZone(zoneDescription);
                }
                target.setDelivery(delivery);
            }

            if (source.getRating() != null && source.getRating() > 0) {
                target.setCustomerReviewAvg(BigDecimal.valueOf(source.getRating()));
            }

            if (source.getRatingCount() > 0) {
                target.setCustomerReviewCount(source.getRatingCount());
            }

            if (target.getDelivery() == null && source.getDelivery() != null) {
                log.info("Setting default value for delivery");
                Delivery delivery = new Delivery();
                Country deliveryCountry;
                if (StringUtils.isNotBlank(source.getDelivery().getCountry())) {
                    deliveryCountry = countries.get(source.getDelivery().getCountry());
                    if (deliveryCountry == null) {
                        throw new ConversionException(
                                "Unsuported country code " + sourceShipping.getCountry());
                    }
                    delivery.setCountry(deliveryCountry);
                    target.setDelivery(delivery);
                }
            }

            if (source.getAttributes() != null) {
                for (PersistableCustomerAttribute attr : source.getAttributes()) {

                    com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOption
                            customerOption =
                                    customerOptionService.getById(attr.getCustomerOption().getId());
                    if (customerOption == null) {
                        throw new ConversionException(
                                "Customer option id "
                                        + attr.getCustomerOption().getId()
                                        + " does not exist");
                    }

                    CustomerOptionValue customerOptionValue =
                            customerOptionValueService.getById(
                                    attr.getCustomerOptionValue().getId());
                    if (customerOptionValue == null) {
                        throw new ConversionException(
                                "Customer option value id "
                                        + attr.getCustomerOptionValue().getId()
                                        + " does not exist");
                    }

                    if (customerOption.getMerchantStore().getId().intValue()
                            != store.getId().intValue()) {
                        throw new ConversionException("Invalid customer option id ");
                    }

                    if (customerOptionValue.getMerchantStore().getId().intValue()
                            != store.getId().intValue()) {
                        throw new ConversionException("Invalid customer option value id ");
                    }

                    CustomerAttribute attribute = new CustomerAttribute();
                    attribute.setCustomer(target);
                    attribute.setCustomerOption(customerOption);
                    attribute.setCustomerOptionValue(customerOptionValue);
                    attribute.setTextValue(attr.getTextValue());

                    target.getAttributes().add(attribute);
                }
            }

            if (target.getDefaultLanguage() == null) {

                Language lang =
                        source.getLanguage() == null
                                ? language
                                : languageService.getByCode(source.getLanguage());

                target.setDefaultLanguage(lang);
            }

        } catch (Exception e) {
            throw new ConversionException(e);
        }

        return target;
    }

    @Override
    protected Customer createTarget() {
        return new Customer();
    }
}
