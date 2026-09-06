package com.asrevo.cvhome.checkout.services.customer;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.checkout.entity.AddressSnapshot;
import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.ZoneCode;
import com.asrevo.cvhome.customer.model.customer.ReadableBilling;
import com.asrevo.cvhome.customer.model.customer.address.CustomerAddress;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private static final String X_Y_Z = "x@y.z";

    private static final String DE_2 = "DE";

    private static final String BE_2 = "BE";

    private static final String E_2 = "E";

    @Test
    void roundTripsAnAddress() {
        CustomerAddress address = new CustomerAddress();
        address.setFirstName("A");
        address.setLastName("B");
        address.setCompany("C");
        address.setAddress("D");
        address.setCity(E_2);
        address.setStateProvince("F");
        address.setPostalCode("G");
        address.setPhone("H");
        address.setCountry(new CountryIsoCode(DE_2));
        address.setZone(new ZoneCode(BE_2));

        AddressSnapshot snapshot = CustomerMapper.toSnapshot(address);
        CustomerAddress back = CustomerMapper.toAddress(snapshot);

        assertThat(back).usingRecursiveComparison().ignoringFields("countryCode").isEqualTo(address);
        assertThat(back.getCountryCode()).isEqualTo(DE_2);
        assertThat(CustomerMapper.toDelivery(snapshot).getCity()).isEqualTo(E_2);
        ReadableBilling billing = CustomerMapper.toBilling(snapshot, X_Y_Z);
        assertThat(billing.getEmail()).isEqualTo(X_Y_Z);
        assertThat(billing.getZone().code()).isEqualTo(BE_2);
    }

    @Test
    void nullsAreTolerated() {
        assertThat(CustomerMapper.toSnapshot(null).isEmpty()).isTrue();
        assertThat(CustomerMapper.toAddress(null).getCity()).isNull();
        AddressSnapshot bare = new AddressSnapshot();
        assertThat(CustomerMapper.toAddress(bare).getCountry()).isNull();
        assertThat(CustomerMapper.toAddress(bare).getZone()).isNull();
        CustomerAddress noCountry = new CustomerAddress();
        assertThat(CustomerMapper.toSnapshot(noCountry).getCountry()).isNull();
    }
}
