package com.asrevo.cvhome.checkout.services.customer;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.entity.Customer;
import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.checkout.model.customer.CustomerFilter;
import com.asrevo.cvhome.checkout.repositories.CustomerRepository;
import com.asrevo.cvhome.checkout.services.reference.CountryService;
import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.ZoneCode;
import com.asrevo.cvhome.customer.errors.CustomerNotFoundException;
import com.asrevo.cvhome.customer.errors.UnsupportedCountryCodeException;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomerList;
import com.asrevo.cvhome.customer.model.customer.address.CustomerAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * One customer per (store, cua account), created on first order and refreshed from every later checkout body; guests
 * collapse onto their email.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    private static final String GUEST_GUEST_EXAMPLE_COM = "guest:guest@example.com";

    private static final String SHOPPER_EXAMPLE_COM = "shopper@example.com";

    private static final String LIT_1_ANALYTICAL_WAY = "1 Analytical Way";

    private static final String NEW_EXAMPLE_COM = "new@example.com";

    private static final String LOVELACE = "Lovelace";

    private static final String AUGUSTA = "Augusta";

    private static final String SUB_1 = "sub-1";

    private static final String PARIS = "Paris";

    private static final String SUB_2 = "sub-2";

    private static final String KEPT = "Kept";

    private static final String LDN_2 = "LDN";

    private static final String ADA = "Ada";

    private static final String XX_2 = "XX";

    private static final String GB_2 = "GB";

    private static final String FR_2 = "FR";

    private static final ShopperId SHOPPER = new ShopperId(SUB_1);

    @Mock
    private CustomerRepository customers;

    @Mock
    private CountryService countries;

    @InjectMocks
    private CustomerServiceImpl service;

    static PersistableCustomer body(String email, String country) {
        PersistableCustomer body = new PersistableCustomer();
        body.setEmailAddress(email);
        CustomerAddress billing = new CustomerAddress();
        billing.setFirstName(ADA);
        billing.setLastName(LOVELACE);
        billing.setAddress(LIT_1_ANALYTICAL_WAY);
        billing.setCity("London");
        billing.setPostalCode("N1");
        billing.setPhone("+44 1");
        billing.setCompany("Engines Ltd");
        billing.setStateProvince("Greater London");
        billing.setCountry(new CountryIsoCode(country));
        billing.setZone(new ZoneCode(LDN_2));
        body.setBilling(billing);
        return body;
    }

    @Test
    void firstOrderCreatesTheCustomerFromTheBody() throws Exception {
        when(countries.isKnown(GB_2)).thenReturn(true);
        when(customers.findByStoreMerchantIdAndCuaExternalId(Orders.STORE, SUB_1)).thenReturn(Optional.empty());
        when(customers.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer customer = service.getOrCreate(Orders.STORE, SHOPPER, body(" Shopper@Example.com ", GB_2));

        assertThat(customer.getCuaExternalId()).isEqualTo(SUB_1);
        assertThat(customer.getEmail()).isEqualTo("Shopper@Example.com");
        assertThat(customer.getFirstName()).isEqualTo(ADA);
        assertThat(customer.getLastName()).isEqualTo(LOVELACE);
        assertThat(customer.getBilling().getCountry().isoCode()).isEqualTo(GB_2);
        assertThat(customer.getBilling().getZoneCode()).isEqualTo(LDN_2);
        assertThat(customer.getBilling().getStreetAddress()).isEqualTo(LIT_1_ANALYTICAL_WAY);
        assertThat(customer.getDelivery().isEmpty()).isTrue();
    }

    @Test
    void laterOrdersRefreshTheExistingRow() throws Exception {
        Customer existing = Orders.customer();
        when(countries.isKnown(any())).thenReturn(true);
        when(customers.findByStoreMerchantIdAndCuaExternalId(Orders.STORE, SUB_1)).thenReturn(Optional.of(existing));
        when(customers.save(existing)).thenReturn(existing);
        PersistableCustomer body = body(NEW_EXAMPLE_COM, FR_2);
        body.setFirstName(AUGUSTA);
        CustomerAddress delivery = new CustomerAddress();
        delivery.setCity(PARIS);
        delivery.setCountryCode(FR_2);
        body.setDelivery(delivery);

        Customer customer = service.getOrCreate(Orders.STORE, SHOPPER, body);

        assertThat(customer).isSameAs(existing);
        assertThat(customer.getEmail()).isEqualTo(NEW_EXAMPLE_COM);
        assertThat(customer.getFirstName()).as("explicit name wins over billing").isEqualTo(AUGUSTA);
        assertThat(customer.getBilling().getCountry().isoCode()).isEqualTo(FR_2);
        assertThat(customer.getDelivery().getCity()).isEqualTo(PARIS);
        assertThat(customer.getDelivery().getCountry().isoCode()).as("countryCode fallback").isEqualTo(FR_2);
    }

    @Test
    void aGuestIsKeyedOnTheirLowercasedEmail() throws Exception {
        when(countries.isKnown(any())).thenReturn(true);
        when(customers.findByStoreMerchantIdAndCuaExternalId(eq(Orders.STORE), any())).thenReturn(Optional.empty());
        when(customers.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer guest = service.getOrCreate(Orders.STORE, null, body("Guest@Example.com", GB_2));

        assertThat(guest.getCuaExternalId()).isEqualTo(GUEST_GUEST_EXAMPLE_COM);
        verify(customers).findByStoreMerchantIdAndCuaExternalId(Orders.STORE, GUEST_GUEST_EXAMPLE_COM);
    }

    @Test
    void anUnknownCountryIsRefusedBeforeAnythingIsWritten() {
        when(countries.isKnown(XX_2)).thenReturn(false);

        assertThatThrownBy(() -> service.getOrCreate(Orders.STORE, SHOPPER, body("a@b.c", XX_2)))
                .isInstanceOf(UnsupportedCountryCodeException.class);
        verify(customers, never()).save(any());
    }

    @Test
    void aBodyWithoutAddressesKeepsWhatTheRowHad() throws Exception {
        Customer existing = Orders.customer();
        existing.getBilling().setCity(KEPT);
        when(customers.findByStoreMerchantIdAndCuaExternalId(Orders.STORE, SUB_1)).thenReturn(Optional.of(existing));
        when(customers.save(existing)).thenReturn(existing);
        PersistableCustomer body = new PersistableCustomer();
        body.setEmailAddress(SHOPPER_EXAMPLE_COM);

        Customer customer = service.getOrCreate(Orders.STORE, SHOPPER, body);

        assertThat(customer.getBilling().getCity()).isEqualTo(KEPT);
        assertThat(customer.getFirstName()).isEqualTo(ADA);
    }

    @Test
    void infoAnswersTheReadableShapeOr404() throws Exception {
        when(customers.findByStoreMerchantIdAndCuaExternalId(Orders.STORE, SUB_1))
                .thenReturn(Optional.of(Orders.customer()));

        ReadableCustomer info = service.info(Orders.STORE, SHOPPER);

        assertThat(info.getId()).isEqualTo(7L);
        assertThat(info.getEmailAddress()).isEqualTo(SHOPPER_EXAMPLE_COM);
        assertThat(info.getUsername()).isEqualTo(SHOPPER_EXAMPLE_COM);
        assertThat(info.getCuaExternalId()).isEqualTo(SUB_1);
        assertThat(info.getFirstName()).isEqualTo(ADA);
        assertThat(info.getBilling()).isNotNull();

        when(customers.findByStoreMerchantIdAndCuaExternalId(Orders.STORE, SUB_2)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.info(Orders.STORE, new ShopperId(SUB_2)))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void theConsoleListIsAPageOfReadables() {
        Pageable page = PageRequest.of(0, 10);
        when(customers.findAll(any(Specification.class), eq(page)))
                .thenReturn(new PageImpl<>(List.of(Orders.customer()), page, 1));

        ReadableCustomerList list = service.list(Orders.STORE, CustomerFilter.none(), page);

        assertThat(list.getContent()).singleElement().satisfies(c -> assertThat(c.getFirstName()).isEqualTo(ADA));
        assertThat(list.getTotalElements()).isEqualTo(1);
        assertThat(list.getSize()).isEqualTo(1);
        assertThat(list.getRecordsFiltered()).isEqualTo(1);
        ArgumentCaptor<Specification<Customer>> spec = ArgumentCaptor.forClass(Specification.class);
        verify(customers).findAll(spec.capture(), eq(page));
        assertThat(spec.getValue()).isNotNull();
    }
}
