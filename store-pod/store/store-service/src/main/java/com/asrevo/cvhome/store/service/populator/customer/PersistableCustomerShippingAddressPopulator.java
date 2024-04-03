package com.asrevo.cvhome.store.service.populator.customer;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.customer.address.Address;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import org.apache.commons.lang3.StringUtils;

public class PersistableCustomerShippingAddressPopulator extends AbstractDataPopulator<Address, Customer> {

    @Override
    public Customer populate(Address source, Customer target, MerchantStore store, Language language)
            throws ConversionException {


        if (target.getDelivery() == null) {

            Delivery delivery = new Delivery();
            delivery.setFirstName(source.getFirstName());
            delivery.setLastName(source.getLastName());

            if (StringUtils.isNotBlank(source.getAddress())) {
                delivery.setAddress(source.getAddress());
            }

            if (StringUtils.isNotBlank(source.getCity())) {
                delivery.setCity(source.getCity());
            }

            if (StringUtils.isNotBlank(source.getCompany())) {
                delivery.setCompany(source.getCompany());
            }

            if (StringUtils.isNotBlank(source.getPhone())) {
                delivery.setTelephone(source.getPhone());
            }

            if (StringUtils.isNotBlank(source.getPostalCode())) {
                delivery.setPostalCode(source.getPostalCode());
            }

            if (StringUtils.isNotBlank(source.getStateProvince())) {
                delivery.setPostalCode(source.getStateProvince());
            }

            target.setDelivery(delivery);
        } else {
            target.getDelivery().setFirstName(source.getFirstName());
            target.getDelivery().setLastName(source.getLastName());

            // lets fill optional data now

            if (StringUtils.isNotBlank(source.getAddress())) {
                target.getDelivery().setAddress(source.getAddress());
            }

            if (StringUtils.isNotBlank(source.getCity())) {
                target.getDelivery().setCity(source.getCity());
            }

            if (StringUtils.isNotBlank(source.getCompany())) {
                target.getDelivery().setCompany(source.getCompany());
            }

            if (StringUtils.isNotBlank(source.getPhone())) {
                target.getDelivery().setTelephone(source.getPhone());
            }

            if (StringUtils.isNotBlank(source.getPostalCode())) {
                target.getDelivery().setPostalCode(source.getPostalCode());
            }

            if (StringUtils.isNotBlank(source.getStateProvince())) {
                target.getDelivery().setPostalCode(source.getStateProvince());
            }
        }

        return target;

    }

    @Override
    protected Customer createTarget() {
        return null;
    }


}
