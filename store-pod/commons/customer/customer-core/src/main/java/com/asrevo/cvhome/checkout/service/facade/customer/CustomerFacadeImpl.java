/**
 *
 */
package com.asrevo.cvhome.checkout.service.facade.customer;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.checkout.service.populator.customer.CustomerPopulator;
import com.asrevo.cvhome.checkout.service.populator.customer.ReadableCustomerPopulator;
import com.asrevo.cvhome.checkout.services.customer.CustomerService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.errors.CustomerNotFoundException;
import com.asrevo.cvhome.customer.errors.UnsupportedCountryCodeException;
import com.asrevo.cvhome.customer.errors.UnsupportedZoneCodeException;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomerList;

import lombok.extern.slf4j.Slf4j;

@Service("customerFacade")
@Slf4j
public class CustomerFacadeImpl implements CustomerFacade {

    private final CustomerService customerService;

    private final CustomerPopulator customerPopulator;

    public CustomerFacadeImpl(CustomerService customerService, CustomerPopulator customerPopulator) {
        this.customerService = customerService;
        this.customerPopulator = customerPopulator;
    }

    @Override
    public ReadableCustomer getCustomerById(Long id, final StoreMerchantId store, final LanguageCode language)
            throws CustomerNotFoundException {

        Customer customerModel = Optional.ofNullable(customerService.getById(id))
                .orElseThrow(() -> CustomerNotFoundException.byId(id, store));

        return convertCustomerToReadableCustomer(customerModel, store, language);
    }

    @Override
    public Optional<Customer> getOrCreateCustomer(PersistableCustomer customer, StoreMerchantId store,
                                                  LanguageCode language)
            throws UnsupportedCountryCodeException, UnsupportedZoneCodeException {
        log.info("Starting to populate customer model from customer data");

        // Deliberately not Optional.or(...): a Supplier cannot throw checked, so routing the create branch through one
        // would force the conversion failures into an Unchecked carrier for no gain. A plain short-circuit keeps the
        // same laziness and lets them stay on the signature, which is where a caller reads them.
        Optional<Customer> existing = Optional.ofNullable(customer.getCuaExternalId())
                .flatMap(customerService::getByCuaExternalId);
        if (existing.isPresent()) {
            return existing;
        }
        return populateNewCustomer(customer, store, language);
    }

    private Optional<Customer> populateNewCustomer(PersistableCustomer customer, StoreMerchantId store,
                                                   LanguageCode language)
            throws UnsupportedCountryCodeException, UnsupportedZoneCodeException {

        // Conversion failures are no longer swallowed into an empty Optional: "we could not create the customer" and
        // "the country code you sent does not exist" were the same silent result, so the caller had nothing to report.
        Customer customerModel = customerPopulator.populate(customer, new Customer(), store, language);

        log.info("About to persist customer to database.");
        // A persistence failure is an unchecked DataAccessException the shared advice renders as a 500 with a
        // traceId. It used to be swallowed into an empty Optional, so the caller could not tell "the database is
        // down" from "there is no such customer".
        customerService.saveOrUpdate(customerModel);
        return Optional.ofNullable(customerModel);
    }

    @Override
    public ReadableCustomerList getListByStore(StoreMerchantId store, CustomerCriteria criteria,
                                               LanguageCode language) {
        Page<Customer> listByStore = customerService.getListByStore(store, criteria);

        List<ReadableCustomer> readableCustomers = listByStore.getContent()
                .stream()
                .map(customer -> convertCustomerToReadableCustomer(customer, store, language))
                .toList();

        ReadableCustomerList readableCustomerList = new ReadableCustomerList();
        readableCustomerList.setTotalPages(listByStore.getTotalPages());
        readableCustomerList.setPageNumber(listByStore.getNumber());
        readableCustomerList.setContent(readableCustomers);
        readableCustomerList.setTotalElements(listByStore.getTotalElements());
        readableCustomerList.setSize(listByStore.getSize());

        return readableCustomerList;
    }

    private ReadableCustomer convertCustomerToReadableCustomer(Customer customer, StoreMerchantId store,
                                                               LanguageCode language) {
        ReadableCustomerPopulator populator = new ReadableCustomerPopulator();
        return populator.populate(customer, new ReadableCustomer(), store, language);
    }

    @Override
    public Optional<ReadableCustomer> getCustomerByCuaExternalId(String cuaExternalId) {
        return customerService.getByCuaExternalId(cuaExternalId)
                .map(it -> convertCustomerToReadableCustomer(it, it.getStoreMerchantId(), LanguageCode.defaultLanguage()));
    }

}
