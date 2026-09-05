package com.asrevo.cvhome.checkout.services.customer;

import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.entity.Customer;
import com.asrevo.cvhome.checkout.model.customer.CustomerFilter;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.errors.UnsupportedCountryCodeException;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomerList;

public interface CustomerService {

    /**
     * The customer for this shopper in this store, created on first order and updated with the checkout body's email,
     * names and addresses on every later one. {@code shopper} is null for a guest checkout, which keys the customer on
     * the email instead.
     */
    Customer getOrCreate(StoreMerchantId store, ShopperId shopper, PersistableCustomer body)
            throws UnsupportedCountryCodeException;

    Optional<Customer> find(StoreMerchantId store, ShopperId shopper);

    /**
     * The shopper's profile. A shopper who has not ordered yet has no row, and answers an empty profile carrying only
     * their cua id — the account page renders "nothing yet", not an error.
     */
    ReadableCustomer info(StoreMerchantId store, ShopperId shopper);

    ReadableCustomerList list(StoreMerchantId store, CustomerFilter filter, Pageable pageable);
}
