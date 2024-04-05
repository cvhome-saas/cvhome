package com.asrevo.cvhome.store.service.populator.customer;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.customer.address.Address;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import org.apache.commons.lang3.StringUtils;

public class PersistableCustomerBillingAddressPopulator extends AbstractDataPopulator<Address, Customer> {

    @Override
    public Customer populate(Address source, Customer target, MerchantStore store, Language language)
            throws ConversionException {


        target.getBilling().setFirstName(source.getFirstName());
        target.getBilling().setLastName(source.getLastName());

        // lets fill optional data now

        if (StringUtils.isNotBlank(source.getAddress())) {
            target.getBilling().setAddress(source.getAddress());
        }

        if (StringUtils.isNotBlank(source.getCity())) {
            target.getBilling().setCity(source.getCity());
        }

        if (StringUtils.isNotBlank(source.getCompany())) {
            target.getBilling().setCompany(source.getCompany());
        }

        if (StringUtils.isNotBlank(source.getPhone())) {
            target.getBilling().setTelephone(source.getPhone());
        }

        if (StringUtils.isNotBlank(source.getPostalCode())) {
            target.getBilling().setPostalCode(source.getPostalCode());
        }

        if (StringUtils.isNotBlank(source.getStateProvince())) {
            target.getBilling().setState(source.getStateProvince());
        }

        return target;

    }

    @Override
    protected Customer createTarget() {
        return null;
    }


}
