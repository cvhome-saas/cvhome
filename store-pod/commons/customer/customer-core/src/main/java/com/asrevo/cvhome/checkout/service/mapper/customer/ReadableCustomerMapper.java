package com.asrevo.cvhome.checkout.service.mapper.customer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.address.CustomerAddress;
import com.asrevo.cvhome.store.core.mapper.Mapper;

@Component
public class ReadableCustomerMapper implements Mapper<Customer, ReadableCustomer> {

    @Override
    public ReadableCustomer convert(Customer source, StoreMerchantId store, LanguageCode language) {

        ReadableCustomer destination = new ReadableCustomer();
        return this.merge(source, destination, store, language);
    }

    @Override
    public ReadableCustomer merge(Customer source, ReadableCustomer target, StoreMerchantId store,
                                  LanguageCode language) {

        if (source.getId() != null && source.getId() > 0) {
            target.setId(source.getId());
        }
        target.setEmailAddress(source.getEmailAddress());

        if (StringUtils.isNotEmpty(source.getUsername())) {
            target.setUsername(source.getUsername());
        }

        if (StringUtils.isNotEmpty(source.getCuaExternalId())) {
            target.setCuaExternalId(source.getCuaExternalId());
        }

        if (source.getBilling() != null) {
            CustomerAddress customerAddress = new CustomerAddress();
            customerAddress.setAddress(source.getBilling().getAddress());
            customerAddress.setCity(source.getBilling().getCity());
            customerAddress.setCompany(source.getBilling().getCompany());
            customerAddress.setFirstName(source.getBilling().getFirstName());
            customerAddress.setLastName(source.getBilling().getLastName());
            customerAddress.setPostalCode(source.getBilling().getPostalCode());
            customerAddress.setPhone(source.getBilling().getTelephone());
            if (source.getBilling().getCountry() != null) {
                customerAddress.setCountry(source.getBilling().getCountry());
            }
            if (source.getBilling().getZone() != null) {
                customerAddress.setZone(source.getBilling().getZone());
            }
            if (source.getBilling().getState() != null) {
                customerAddress.setStateProvince(source.getBilling().getState());
            }

            target.setFirstName(customerAddress.getFirstName());
            target.setLastName(customerAddress.getLastName());

            target.setBilling(customerAddress);
        }

        if (source.getDelivery() != null) {
            CustomerAddress customerAddress = new CustomerAddress();
            customerAddress.setCity(source.getDelivery().getCity());
            customerAddress.setAddress(source.getDelivery().getAddress());
            customerAddress.setCompany(source.getDelivery().getCompany());
            customerAddress.setFirstName(source.getDelivery().getFirstName());
            customerAddress.setLastName(source.getDelivery().getLastName());
            customerAddress.setPostalCode(source.getDelivery().getPostalCode());
            customerAddress.setPhone(source.getDelivery().getTelephone());
            customerAddress.setCountry(source.getDelivery().getCountry());
            if (source.getDelivery().getZone() != null) {
                customerAddress.setZone(source.getDelivery().getZone());
            }
            if (source.getDelivery().getState() != null) {
                customerAddress.setStateProvince(source.getDelivery().getState());
            }

            target.setDelivery(customerAddress);
        } else {
            target.setDelivery(target.getBilling());
        }

        return target;
    }

}
