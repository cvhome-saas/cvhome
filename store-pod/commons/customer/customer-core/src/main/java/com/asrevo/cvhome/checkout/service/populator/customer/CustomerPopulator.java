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
import com.asrevo.cvhome.customer.errors.UnsupportedCountryCodeException;
import com.asrevo.cvhome.customer.errors.UnsupportedZoneCodeException;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.customer.model.customer.address.CustomerAddress;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
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
            throws UnsupportedCountryCodeException, UnsupportedZoneCodeException {

        applyBasics(source, target);

        Map<CountryIsoCode, Country> countries = countryService.getCountriesMap(language);
        Map<ZoneCode, Zone> zones = zoneService.getZones(language);

        target.setStoreMerchantId(store);

        // No blanket catch any more: it used to turn an unsupported country code and a NullPointerException in this
        // mapping code into the same 400, which told a shopper their input was wrong when the bug was ours.
        applyBilling(source, target, countries, zones);
        applyDelivery(source, target, countries);

        return target;
    }

    private void applyBasics(PersistableCustomer source, Customer target) {
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
    }

    private void applyBilling(PersistableCustomer source, Customer target, Map<CountryIsoCode, Country> countries,
                              Map<ZoneCode, Zone> zones) throws UnsupportedCountryCodeException, UnsupportedZoneCodeException {
        applyBillingFromSource(source, target, countries, zones);
        applyDefaultBillingIfMissing(source, target, countries);
    }

    private void applyBillingFromSource(PersistableCustomer source, Customer target, Map<CountryIsoCode, Country> countries,
                                        Map<ZoneCode, Zone> zones) throws UnsupportedCountryCodeException, UnsupportedZoneCodeException {
        CustomerAddress sourceBilling = source.getBilling();
        if (sourceBilling == null) {
            return;
        }
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

    private void applyDefaultBillingIfMissing(PersistableCustomer source, Customer target,
                                              Map<CountryIsoCode, Country> countries) throws UnsupportedCountryCodeException {
        if (target.getBilling() != null || source.getBilling() == null) {
            return;
        }
        log.info("Setting default values for billing");
        Billing billing = new Billing();
        if (source.getBilling().getCountry().isValid()) {
            Country billingCountry = resolveCountry(source.getBilling().getCountry(), countries);
            billing.setCountry(billingCountry.getId());
            target.setBilling(billing);
        }
    }

    private void applyDelivery(PersistableCustomer source, Customer target, Map<CountryIsoCode, Country> countries)
            throws UnsupportedCountryCodeException, UnsupportedZoneCodeException {
        applyDeliveryFromSource(source, target, countries);
        applyDefaultDeliveryIfMissing(source, target, countries);
    }

    private void applyDeliveryFromSource(PersistableCustomer source, Customer target, Map<CountryIsoCode, Country> countries)
            throws UnsupportedCountryCodeException, UnsupportedZoneCodeException {
        CustomerAddress sourceShipping = source.getDelivery();
        if (sourceShipping == null) {
            return;
        }
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

    private void applyDefaultDeliveryIfMissing(PersistableCustomer source, Customer target,
                                               Map<CountryIsoCode, Country> countries) throws UnsupportedCountryCodeException {
        if (target.getDelivery() != null || source.getDelivery() == null) {
            return;
        }
        log.info("Setting default value for delivery");
        if (source.getDelivery().getCountry().isValid()) {
            Delivery delivery = new Delivery();
            Country deliveryCountry = resolveCountry(source.getDelivery().getCountry(), countries);
            delivery.setCountry(deliveryCountry.getIsoCode());
            target.setDelivery(delivery);
        }
    }

    private Country resolveCountry(CountryIsoCode code, Map<CountryIsoCode, Country> countries)
            throws UnsupportedCountryCodeException {
        Country country = countries.get(code);
        if (country == null) {
            throw UnsupportedCountryCodeException.of(code);
        }
        return country;
    }

    private Zone resolveZone(ZoneCode code) throws UnsupportedZoneCodeException {
        Zone zone = zoneService.getByCode(code);
        if (zone == null) {
            throw UnsupportedZoneCodeException.of(code);
        }
        return zone;
    }

    @Override
    protected Customer createTarget() {
        return new Customer();
    }

}
