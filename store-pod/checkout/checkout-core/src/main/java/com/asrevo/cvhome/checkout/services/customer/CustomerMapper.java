package com.asrevo.cvhome.checkout.services.customer;

import com.asrevo.cvhome.checkout.entity.AddressSnapshot;
import com.asrevo.cvhome.checkout.entity.Customer;
import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.ZoneCode;
import com.asrevo.cvhome.customer.model.customer.ReadableBilling;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableDelivery;
import com.asrevo.cvhome.customer.model.customer.address.CustomerAddress;

/**
 * Between the entity's {@link AddressSnapshot} and the {@code customer-commons} address DTO both frontends read.
 */
public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static ReadableCustomer toReadable(Customer customer) {
        ReadableCustomer readable = new ReadableCustomer();
        readable.setId(customer.getId());
        readable.setCuaExternalId(customer.getCuaExternalId());
        readable.setEmailAddress(customer.getEmail());
        readable.setFirstName(customer.getFirstName());
        readable.setLastName(customer.getLastName());
        readable.setUsername(customer.getEmail());
        readable.setBilling(toBilling(customer.getBilling(), customer.getEmail()));
        readable.setDelivery(toDelivery(customer.getDelivery()));
        return readable;
    }

    public static ReadableBilling toBilling(AddressSnapshot snapshot, String email) {
        ReadableBilling billing = new ReadableBilling();
        fill(billing, snapshot);
        billing.setEmail(email);
        return billing;
    }

    public static ReadableDelivery toDelivery(AddressSnapshot snapshot) {
        ReadableDelivery delivery = new ReadableDelivery();
        fill(delivery, snapshot);
        return delivery;
    }

    public static CustomerAddress toAddress(AddressSnapshot snapshot) {
        CustomerAddress address = new CustomerAddress();
        fill(address, snapshot);
        return address;
    }

    public static AddressSnapshot toSnapshot(CustomerAddress address) {
        AddressSnapshot snapshot = new AddressSnapshot();
        if (address == null) {
            return snapshot;
        }
        snapshot.setFirstName(address.getFirstName());
        snapshot.setLastName(address.getLastName());
        snapshot.setCompany(address.getCompany());
        snapshot.setStreetAddress(address.getAddress());
        snapshot.setCity(address.getCity());
        snapshot.setStateProvince(address.getStateProvince());
        snapshot.setPostcode(address.getPostalCode());
        snapshot.setTelephone(address.getPhone());
        snapshot.setCountry(address.getCountry() == null && address.getCountryCode() != null
                ? new CountryIsoCode(address.getCountryCode()) : address.getCountry());
        snapshot.setZoneCode(address.getZone() == null ? null : address.getZone().code());
        return snapshot;
    }

    private static void fill(CustomerAddress target, AddressSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        target.setFirstName(snapshot.getFirstName());
        target.setLastName(snapshot.getLastName());
        target.setCompany(snapshot.getCompany());
        target.setAddress(snapshot.getStreetAddress());
        target.setCity(snapshot.getCity());
        target.setStateProvince(snapshot.getStateProvince());
        target.setPostalCode(snapshot.getPostcode());
        target.setPhone(snapshot.getTelephone());
        target.setCountry(snapshot.getCountry());
        target.setCountryCode(snapshot.getCountry() == null ? null : snapshot.getCountry().isoCode());
        target.setZone(snapshot.getZoneCode() == null ? null : new ZoneCode(snapshot.getZoneCode()));
    }
}
