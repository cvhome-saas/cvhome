package com.asrevo.cvhome.store.service.mapper.customer;

import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerAttribute;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.user.Group;
import com.asrevo.cvhome.store.core.model.customer.ReadableCustomer;
import com.asrevo.cvhome.store.core.model.customer.address.Address;
import com.asrevo.cvhome.store.core.model.customer.attribute.*;
import com.asrevo.cvhome.store.core.model.security.ReadableGroup;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ReadableCustomerMapper implements Mapper<Customer, ReadableCustomer> {

    @Override
    public ReadableCustomer convert(Customer source, MerchantStore store, Language language) {

        ReadableCustomer destination = new ReadableCustomer();
        return this.merge(source, destination, store, language);
    }

    @Override
    public ReadableCustomer merge(Customer source, ReadableCustomer target, MerchantStore store,
                                  Language language) {

        if (source.getId() != null && source.getId() > 0) {
            target.setId(source.getId());
        }
        target.setEmailAddress(source.getEmailAddress());

        if (StringUtils.isNotEmpty(source.getNick())) {
            target.setUserName(source.getNick());
        }

        if (source.getDefaultLanguage() != null) {
            target.setLanguage(source.getDefaultLanguage().getCode());
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
                address.setCountry(source.getBilling().getCountry().getIsoCode());
            }
            if (source.getBilling().getZone() != null) {
                address.setZone(source.getBilling().getZone().getCode());
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
                address.setCountry(source.getDelivery().getCountry().getIsoCode());
            }
            if (source.getDelivery().getZone() != null) {
                address.setZone(source.getDelivery().getZone().getCode());
            }
            if (source.getDelivery().getState() != null) {
                address.setStateProvince(source.getDelivery().getState());
            }

            target.setDelivery(address);
        } else {
            target.setDelivery(target.getBilling());
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
                d.setDescription(attribute.getCustomerOption().getDescriptionsSettoList().getFirst().getDescription());
                d.setName(attribute.getCustomerOption().getDescriptionsSettoList().getFirst().getName());
                option.setDescription(d);

                readableAttribute.setCustomerOption(option);

                ReadableCustomerOptionValue optionValue = new ReadableCustomerOptionValue();
                optionValue.setId(attribute.getCustomerOptionValue().getId());
                CustomerOptionValueDescription vd = new CustomerOptionValueDescription();
                vd.setDescription(attribute.getCustomerOptionValue().getDescriptionsSettoList().getFirst().getDescription());
                vd.setName(attribute.getCustomerOptionValue().getDescriptionsSettoList().getFirst().getName());
                optionValue.setCode(attribute.getCustomerOptionValue().getCode());
                optionValue.setDescription(vd);


                readableAttribute.setCustomerOptionValue(optionValue);
                target.getAttributes().add(readableAttribute);
            }

            if (source.getGroups() != null) {
                for (Group group : source.getGroups()) {
                    ReadableGroup readableGroup = new ReadableGroup();
                    readableGroup.setId(group.getId().longValue());
                    readableGroup.setName(group.getGroupName());
                    readableGroup.setType(group.getGroupType().name());
                    target.getGroups().add(
                            readableGroup
                    );
                }
            }
        }

        return target;
    }

}
