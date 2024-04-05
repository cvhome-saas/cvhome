/**
 *
 */
package com.asrevo.cvhome.store.service.populator.customer;


import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.customer.address.Address;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;

/**
 * @author csamson
 */
public class CustomerBillingAddressPopulator extends AbstractDataPopulator<Customer, Address> {

    @Override
    public Address populate(Customer source, Address target, MerchantStore store, Language language)
            throws ConversionException {

        target.setCity(source.getBilling().getCity());
        target.setCompany(source.getBilling().getCompany());
        target.setFirstName(source.getBilling().getFirstName());
        target.setLastName(source.getBilling().getLastName());
        target.setPostalCode(source.getBilling().getPostalCode());
        target.setPhone(source.getBilling().getTelephone());
        if (source.getBilling().getTelephone() == null) {
            target.setPhone(source.getBilling().getTelephone());
        }
        target.setAddress(source.getBilling().getAddress());
        if (source.getBilling().getCountry() != null) {
            target.setCountry(source.getBilling().getCountry().getIsoCode());
        }
        if (source.getBilling().getZone() != null) {
            target.setZone(source.getBilling().getZone().getCode());
        }
        target.setStateProvince(source.getBilling().getState());

        return target;
    }

    @Override
    protected Address createTarget() {
        return new Address();
    }

}
