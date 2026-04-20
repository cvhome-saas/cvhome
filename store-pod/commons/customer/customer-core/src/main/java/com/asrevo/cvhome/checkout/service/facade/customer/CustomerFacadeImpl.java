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
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomerList;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

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
    public ReadableCustomer getCustomerById(Long id, final StoreMerchantId store, final LanguageCode language) {

        Customer customerModel = Optional.ofNullable(customerService.getById(id))
                .orElseThrow(() -> new ResourceNotFoundException("No Customer found for ID : " + id));

        return convertCustomerToReadableCustomer(customerModel, store, language);
    }

    @Override
    public Optional<Customer> getOrCreateCustomer(PersistableCustomer customer, StoreMerchantId store,
                                                  LanguageCode language) {
        log.info("Starting to populate customer model from customer data");

        return Optional.ofNullable(customer.getCuaExternalId())
                .flatMap(customerService::getByCuaExternalId)
                .or(() -> populateNewCustomer(customer, store, language));

    }

    private Optional<Customer> populateNewCustomer(PersistableCustomer customer, StoreMerchantId store,
                                                   LanguageCode language) {
        Customer customerModel = new Customer();

        try {
            customerModel = customerPopulator.populate(customer, customerModel, store, language);
            log.info("About to persist customer to database.");
            customerService.saveOrUpdate(customerModel);
            return Optional.ofNullable(customerModel);
        } catch (Exception e) {
            log.error("Error while persisting customer", e);
            return Optional.empty();
        }
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
        try {
            return populator.populate(customer, new ReadableCustomer(), store, language);
        } catch (ConversionException e) {
            throw new ConversionRuntimeException(e);
        }
    }

    @Override
    public Optional<ReadableCustomer> getCustomerByCuaExternalId(String cuaExternalId) {
        return customerService.getByCuaExternalId(cuaExternalId)
                .map(it -> convertCustomerToReadableCustomer(it, it.getStoreMerchantId(), LanguageCode.defaultLanguage()));
    }

}
