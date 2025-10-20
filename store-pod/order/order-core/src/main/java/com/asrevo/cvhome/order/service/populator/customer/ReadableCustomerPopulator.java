package com.asrevo.cvhome.order.service.populator.customer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.address.Address;
import com.asrevo.cvhome.customer.model.customer.attribute.*;
import com.asrevo.cvhome.order.entity.customer.Customer;
import com.asrevo.cvhome.order.entity.customer.attribute.CustomerAttribute;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import org.apache.commons.lang3.StringUtils;

public class ReadableCustomerPopulator extends AbstractDataPopulator<Customer, StoreMerchantId, ReadableCustomer> {

	@Override
	public ReadableCustomer populate(Customer source, ReadableCustomer target, StoreMerchantId store,
			LanguageCode language) throws ConversionException {

		try {

			if (target == null) {
				target = new ReadableCustomer();
			}

			if (source.getId() != null && source.getId() > 0) {
				target.setId(source.getId());
			}
			target.setEmailAddress(source.getEmailAddress());

			if (StringUtils.isNotEmpty(source.getNick())) {
				target.setUserName(source.getNick());
			}

			if (source.getDefaultLanguageCode() != null) {
				target.setLanguage(source.getDefaultLanguageCode());
			}

			if (source.getGender() != null) {
				target.setGender(source.getGender().name());
			}

			if (StringUtils.isNotEmpty(source.getProvider())) {
				target.setProvider(source.getProvider());
			}

			if (source.getBilling() != null) {
				Address address = new Address();
				address.setAddress(source.getBilling().getAddress());
				address.setCity(source.getBilling().getCity());
				address.setCompany(source.getBilling().getCompany());
				address.setFirstName(source.getBilling().getFirstName());
				address.setLastName(source.getBilling().getLastName());
				address.setPostalCode(source.getBilling().getPostalCode());
				address.setPhone(source.getBilling().getTelephone());
				if (source.getBilling().getCountry() != null) {
					address.setCountry(source.getBilling().getCountry());
				}
				if (source.getBilling().getZone() != null) {
					address.setZone(source.getBilling().getZone());
				}
				if (source.getBilling().getState() != null) {
					address.setStateProvince(source.getBilling().getState());
				}

				target.setFirstName(address.getFirstName());
				target.setLastName(address.getLastName());

				target.setBilling(address);
			}

			if (source.getCustomerReviewAvg() != null) {
				target.setRating(source.getCustomerReviewAvg().doubleValue());
			}

			if (source.getCustomerReviewCount() != null) {
				target.setRatingCount(source.getCustomerReviewCount());
			}

			if (source.getDelivery() != null) {
				Address address = new Address();
				address.setCity(source.getDelivery().getCity());
				address.setAddress(source.getDelivery().getAddress());
				address.setCompany(source.getDelivery().getCompany());
				address.setFirstName(source.getDelivery().getFirstName());
				address.setLastName(source.getDelivery().getLastName());
				address.setPostalCode(source.getDelivery().getPostalCode());
				address.setPhone(source.getDelivery().getTelephone());
				if (source.getDelivery().getCountry() != null) {
					address.setCountry(source.getDelivery().getCountry());
				}
				if (source.getDelivery().getZone() != null) {
					address.setZone(source.getDelivery().getZone());
				}
				if (source.getDelivery().getState() != null) {
					address.setStateProvince(source.getDelivery().getState());
				}

				target.setDelivery(address);
			}

			if (source.getAttributes() != null) {
				for (CustomerAttribute attribute : source.getAttributes()) {
					ReadableCustomerAttribute readableAttribute = new ReadableCustomerAttribute();
					readableAttribute.setId(attribute.getId());
					readableAttribute.setTextValue(attribute.getTextValue());
					ReadableCustomerOption option = new ReadableCustomerOption();
					option.setId(attribute.getCustomerOption().getId());
					option.setCode(attribute.getCustomerOption().getCode());

					CustomerOptionDescription d = new CustomerOptionDescription();
					d.setDescription(
							attribute.getCustomerOption().getDescriptionsSettoList().getFirst().getDescription());
					d.setName(attribute.getCustomerOption().getDescriptionsSettoList().getFirst().getName());
					option.setDescription(d);

					readableAttribute.setCustomerOption(option);

					ReadableCustomerOptionValue optionValue = new ReadableCustomerOptionValue();
					optionValue.setId(attribute.getCustomerOptionValue().getId());
					CustomerOptionValueDescription vd = new CustomerOptionValueDescription();
					vd.setDescription(
							attribute.getCustomerOptionValue().getDescriptionsSettoList().getFirst().getDescription());
					vd.setName(attribute.getCustomerOptionValue().getDescriptionsSettoList().getFirst().getName());
					optionValue.setCode(attribute.getCustomerOptionValue().getCode());
					optionValue.setDescription(vd);

					readableAttribute.setCustomerOptionValue(optionValue);
					target.getAttributes().add(readableAttribute);
				}
			}

		}
		catch (Exception e) {
			throw new ConversionException(e);
		}

		return target;
	}

	@Override
	protected ReadableCustomer createTarget() {
		return null;
	}

}
