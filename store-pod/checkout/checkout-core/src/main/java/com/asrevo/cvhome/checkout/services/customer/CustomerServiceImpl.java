package com.asrevo.cvhome.checkout.services.customer;

import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.entity.Customer;
import com.asrevo.cvhome.checkout.model.customer.CustomerFilter;
import com.asrevo.cvhome.checkout.repositories.CustomerRepository;
import com.asrevo.cvhome.checkout.repositories.OrderSpecifications;
import com.asrevo.cvhome.checkout.services.reference.CountryService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.errors.UnsupportedCountryCodeException;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomerList;
import com.asrevo.cvhome.customer.model.customer.address.CustomerAddress;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    /** Guest checkouts share one customer row per email, so a repeat guest is one customer in the console. */
    static final String GUEST_PREFIX = "guest:";

    private final CustomerRepository customers;

    private final CountryService countries;

    @Override
    @Transactional
    public Customer getOrCreate(StoreMerchantId store, ShopperId shopper, PersistableCustomer body)
            throws UnsupportedCountryCodeException {
        validateCountry(body.getBilling());
        validateCountry(body.getDelivery());
        String email = body.getEmailAddress().trim();
        String key = shopper == null ? GUEST_PREFIX + email.toLowerCase(Locale.ROOT) : shopper.sub();
        Customer customer = customers.findByStoreMerchantIdAndCuaExternalId(store, key)
                .orElseGet(() -> new Customer(store, key, email));
        customer.setEmail(email);
        customer.setFirstName(firstNonBlank(body.getFirstName(), body.getBilling() == null ? null
                : body.getBilling().getFirstName(), customer.getFirstName()));
        customer.setLastName(firstNonBlank(body.getLastName(), body.getBilling() == null ? null
                : body.getBilling().getLastName(), customer.getLastName()));
        if (body.getBilling() != null) {
            customer.setBilling(CustomerMapper.toSnapshot(body.getBilling()));
        }
        if (body.getDelivery() != null) {
            customer.setDelivery(CustomerMapper.toSnapshot(body.getDelivery()));
        }
        return customers.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> find(StoreMerchantId store, ShopperId shopper) {
        return customers.findByStoreMerchantIdAndCuaExternalId(store, shopper.sub());
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableCustomer info(StoreMerchantId store, ShopperId shopper) {
        return find(store, shopper).map(CustomerMapper::toReadable).orElseGet(() -> {
            ReadableCustomer empty = new ReadableCustomer();
            empty.setCuaExternalId(shopper.sub());
            return empty;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableCustomerList list(StoreMerchantId store, CustomerFilter filter, Pageable pageable) {
        Page<Customer> page = customers.findAll(OrderSpecifications.customers(store, filter), pageable);
        ReadableCustomerList list = new ReadableCustomerList();
        list.setContent(page.getContent().stream().map(CustomerMapper::toReadable).toList());
        list.setSize(page.getNumberOfElements());
        list.setTotalElements(page.getTotalElements());
        list.setTotalPages(page.getTotalPages());
        list.setPageNumber(page.getNumber());
        list.setRecordsFiltered(page.getNumberOfElements());
        return list;
    }

    private void validateCountry(CustomerAddress address) throws UnsupportedCountryCodeException {
        if (address == null || address.getCountry() == null || address.getCountry().isoCode() == null) {
            return;
        }
        if (!countries.isKnown(address.getCountry().isoCode())) {
            throw UnsupportedCountryCodeException.of(address.getCountry().isoCode());
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
