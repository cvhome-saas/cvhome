package com.asrevo.cvhome.checkout.service.populator.customer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.customer.model.customer.address.Address;
import com.asrevo.cvhome.customer.model.customer.attribute.PersistableCustomerAttribute;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.customer.CustomerGender;
import com.asrevo.cvhome.checkout.entity.customer.attribute.CustomerAttribute;
import com.asrevo.cvhome.checkout.entity.customer.attribute.CustomerOption;
import com.asrevo.cvhome.checkout.entity.customer.attribute.CustomerOptionValue;
import com.asrevo.cvhome.order.entity.reference.country.Country;
import com.asrevo.cvhome.order.entity.reference.zone.Zone;
import com.asrevo.cvhome.checkout.services.customer.attribute.CustomerOptionService;
import com.asrevo.cvhome.checkout.services.customer.attribute.CustomerOptionValueService;
import com.asrevo.cvhome.order.services.reference.country.CountryService;
import com.asrevo.cvhome.order.services.reference.zone.ZoneService;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerPopulator extends AbstractDataPopulator<PersistableCustomer, StoreMerchantId, Customer> {

	private final CountryService countryService;

	private final ZoneService zoneService;

	private final CustomerOptionService customerOptionService;

	private final CustomerOptionValueService customerOptionValueService;

	public CustomerPopulator(CountryService countryService, ZoneService zoneService,
			CustomerOptionService customerOptionService, CustomerOptionValueService customerOptionValueService) {
		this.countryService = countryService;
		this.zoneService = zoneService;
		this.customerOptionService = customerOptionService;
		this.customerOptionValueService = customerOptionValueService;
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

			if (!StringUtils.isBlank(source.getPassword())) {
				target.setPassword(
						/* @TODO ASHRAF passwordEncoder.encode(source.getPassword()) */ source.getPassword());
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

			Map<CountryIsoCode, Country> countries = countryService.getCountriesMap(language);
			Map<ZoneCode, Zone> zones = zoneService.getZones(language);

			target.setStoreMerchantId(store);

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
				if (sourceBilling.getCountry().isValid()) {
					billingCountry = countries.get(sourceBilling.getCountry());
					if (billingCountry == null) {
						throw new ConversionException("Unsuported country code " + sourceBilling.getCountry());
					}
					billing.setCountry(billingCountry.getIsoCode());
				}

				if (billingCountry != null && sourceBilling.getZone() != null) {
					Zone zone = zoneService.getByCode(sourceBilling.getZone());
					if (zone == null) {
						throw new ConversionException("Unsuported zone code " + sourceBilling.getZone());
					}
					Zone zoneDescription = zones.get(zone.getCode());
					billing.setZone(zoneDescription.getId());
				}
				// target.setBilling(billing);

			}
			if (target.getBilling() == null && source.getBilling() != null) {
				log.info("Setting default values for billing");
				Billing billing = new Billing();
				Country billingCountry;
				if (source.getBilling().getCountry().isValid()) {
					billingCountry = countries.get(source.getBilling().getCountry());
					if (billingCountry == null) {
						throw new ConversionException("Unsuported country code " + sourceBilling.getCountry());
					}
					billing.setCountry(billingCountry.getId());
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

				if (sourceShipping.getCountry().isValid()) {
					deliveryCountry = countries.get(sourceShipping.getCountry());
					if (deliveryCountry == null) {
						throw new ConversionException("Unsuported country code " + sourceShipping.getCountry());
					}
					delivery.setCountry(deliveryCountry.getIsoCode());
				}

				if (deliveryCountry != null && sourceShipping.getZone() != null) {
					Zone zone = zoneService.getByCode(sourceShipping.getZone());
					if (zone == null) {
						throw new ConversionException("Unsuported zone code " + sourceShipping.getZone());
					}
					delivery.setZone(zone.getCode());
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
				if (source.getDelivery().getCountry().isValid()) {
					deliveryCountry = countries.get(source.getDelivery().getCountry());
					if (deliveryCountry == null) {
						CountryIsoCode countryIsoCode = Optional.ofNullable(sourceShipping)
							.map(Address::getCountry)
							.orElse(null);
						throw new ConversionException("Unsupported country code " + countryIsoCode);
					}
					delivery.setCountry(deliveryCountry.getIsoCode());
					target.setDelivery(delivery);
				}
			}

			if (source.getAttributes() != null) {
				for (PersistableCustomerAttribute attr : source.getAttributes()) {

					CustomerOption customerOption = customerOptionService.getById(attr.getCustomerOption().getId());
					if (customerOption == null) {
						throw new ConversionException(
								"Customer option id " + attr.getCustomerOption().getId() + " does not exist");
					}

					CustomerOptionValue customerOptionValue = customerOptionValueService
						.getById(attr.getCustomerOptionValue().getId());
					if (customerOptionValue == null) {
						throw new ConversionException("Customer option value id "
								+ attr.getCustomerOptionValue().getId() + " does not exist");
					}

					if (!Objects.equals(customerOption.getStoreMerchantId(), store)) {
						throw new ConversionException("Invalid customer option id ");
					}

					if (!Objects.equals(customerOptionValue.getStoreMerchantId(), store)) {
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

			if (target.getDefaultLanguageCode() == null) {

				LanguageCode lang = source.getLanguage() == null ? language : source.getLanguage();

				target.setDefaultLanguageCode(lang);
			}

		}
		catch (Exception e) {
			throw new ConversionException(e);
		}

		return target;
	}

	@Override
	protected Customer createTarget() {
		return new Customer();
	}

}
