package com.asrevo.cvhome.checkout.service.populator.customer;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.reference.country.Country;
import com.asrevo.cvhome.checkout.entity.reference.zone.Zone;
import com.asrevo.cvhome.checkout.services.reference.country.CountryService;
import com.asrevo.cvhome.checkout.services.reference.zone.ZoneService;
import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.ZoneCode;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.customer.model.customer.address.CustomerAddress;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomerPopulator extends AbstractDataPopulator<PersistableCustomer, StoreMerchantId, Customer> {

    private final CountryService countryService;

    private final ZoneService zoneService;

    public CustomerPopulator(CountryService countryService, ZoneService zoneService) {
        this.countryService = countryService;
        this.zoneService = zoneService;
    }

    /**
     * Creates a Customer entity ready to be saved
     */
    @Override
    public Customer populate(PersistableCustomer source, Customer target, StoreMerchantId store, LanguageCode language)
            throws ConversionException {

        try {

            if (source.getId() != null && source.getId() > 0) {
                target.setId(source.getId());
            }

            if (source.getCuaExternalId() != null) {
                target.setCuaExternalId(source.getCuaExternalId());
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

            if (!StringUtils.isBlank(source.getEmailAddress())) {
                target.setEmailAddress(source.getEmailAddress());
            }

            Map<CountryIsoCode, Country> countries = countryService.getCountriesMap(language);
            Map<ZoneCode, Zone> zones = zoneService.getZones(language);

            target.setStoreMerchantId(store);

            CustomerAddress sourceBilling = source.getBilling();
            if (sourceBilling != null) {
                Billing billing = target.getBilling();
                billing.setAddress(sourceBilling.getAddress());
                billing.setCity(sourceBilling.getCity());
                billing.setCompany(sourceBilling.getCompany());
                if (!StringUtils.isEmpty(sourceBilling.getFirstName())) {
                    billing.setFirstName(sourceBilling.getFirstName());
                }
                if (!StringUtils.isEmpty(sourceBilling.getLastName())) {
                    billing.setLastName(sourceBilling.getLastName());
                }
                billing.setTelephone(sourceBilling.getPhone());
                billing.setPostalCode(sourceBilling.getPostalCode());
                billing.setState(sourceBilling.getStateProvince());
                Country billingCountry = null;
                if (sourceBilling.getCountry().isValid()) {
                    billingCountry = resolveCountry(sourceBilling.getCountry(), countries);
                    billing.setCountry(billingCountry.getIsoCode());
                }

                if (billingCountry != null && sourceBilling.getZone() != null) {
                    Zone zone = resolveZone(sourceBilling.getZone());
                    Zone zoneDescription = zones.get(zone.getCode());
                    billing.setZone(zoneDescription.getId());
                }

            }
            if (target.getBilling() == null && source.getBilling() != null) {
                log.info("Setting default values for billing");
                Billing billing = new Billing();
                Country billingCountry;
                if (source.getBilling().getCountry().isValid()) {
                    billingCountry = resolveCountry(source.getBilling().getCountry(), countries);
                    billing.setCountry(billingCountry.getId());
                    target.setBilling(billing);
                }
            }
            CustomerAddress sourceShipping = source.getDelivery();
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

                if (sourceShipping.getCountry().isValid()) {
                    deliveryCountry = resolveCountry(sourceShipping.getCountry(), countries);
                    delivery.setCountry(deliveryCountry.getIsoCode());
                }

                if (deliveryCountry != null && sourceShipping.getZone() != null) {
                    Zone zone = resolveZone(sourceShipping.getZone());
                    delivery.setZone(zone.getCode());
                }
                target.setDelivery(delivery);
            }

            if (target.getDelivery() == null && source.getDelivery() != null) {
                log.info("Setting default value for delivery");
                Delivery delivery = new Delivery();
                if (source.getDelivery().getCountry().isValid()) {
                    Country deliveryCountry = resolveCountry(source.getDelivery().getCountry(), countries);
                    delivery.setCountry(deliveryCountry.getIsoCode());
                    target.setDelivery(delivery);
                }
            }

        } catch (Exception e) {
            throw new ConversionException(e);
        }

        return target;
    }

    private Country resolveCountry(CountryIsoCode code, Map<CountryIsoCode, Country> countries) throws ConversionException {
        Country country = countries.get(code);
        if (country == null) {
            throw new ConversionException(String.format("Unsupported country code %s", code));
        }
        return country;
    }

    private Zone resolveZone(ZoneCode code) throws ConversionException {
        Zone zone = zoneService.getByCode(code);
        if (zone == null) {
            throw new ConversionException(String.format("Unsupported zone code %s", code));
        }
        return zone;
    }

    @Override
    protected Customer createTarget() {
        return new Customer();
    }

}
