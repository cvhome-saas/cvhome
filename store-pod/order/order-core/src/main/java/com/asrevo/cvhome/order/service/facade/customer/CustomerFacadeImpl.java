/**
 *
 */
package com.asrevo.cvhome.order.service.facade.customer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomerList;
import com.asrevo.cvhome.order.entity.customer.Customer;
import com.asrevo.cvhome.order.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.order.entity.customer.CustomerList;
import com.asrevo.cvhome.order.service.populator.customer.CustomerPopulator;
import com.asrevo.cvhome.order.service.populator.customer.ReadableCustomerPopulator;
import com.asrevo.cvhome.order.services.customer.CustomerService;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Customer Facade work as an abstraction layer between Controller and Service layer. It
 * work as an entry point to service layer.
 *
 * @author Umesh Awasthi
 * @version 2.2.1, 2.8.0
 */
// @TODO ASHRAF

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
	public boolean checkIfUserExists(String userName, StoreMerchantId store) {
		if (StringUtils.isNotBlank(userName) && store != null) {
			Customer customer = customerService.getByNick(userName, store);
			if (customer != null) {
				log.info("Customer with userName {} already exists for store {} ", userName, store);
				return true;
			}

			return false;
		}
		log.info("Either userName is empty or we have not found any value for store");
		return false;
	}

	@Override
	public ReadableCustomer getCustomerById(Long id, final StoreMerchantId store, final LanguageCode language) {

		Customer customerModel = Optional.ofNullable(customerService.getById(id))
			.orElseThrow(() -> new ResourceNotFoundException("No Customer found for ID : " + id));

		return convertCustomerToReadableCustomer(customerModel, store, language);
	}

	@Override
	public Customer populateCustomerModel(Customer customerModel, PersistableCustomer customer, StoreMerchantId store,
			LanguageCode language) throws Exception {
		log.info("Starting to populate customer model from customer data");

		customerModel = customerPopulator.populate(customer, customerModel, store, language);

		log.info("About to persist customer to database.");
		customerService.saveOrUpdate(customerModel);
		return customerModel;
	}

	@Override
	public ReadableCustomerList getListByStore(StoreMerchantId store, CustomerCriteria criteria,
			LanguageCode language) {
		CustomerList customerList = customerService.getListByStore(store, criteria);
		return convertCustomerListToReadableCustomerList(customerList, store, language);
	}

	private ReadableCustomerList convertCustomerListToReadableCustomerList(CustomerList customerList,
			StoreMerchantId store, LanguageCode language) {
		List<ReadableCustomer> readableCustomers = customerList.getCustomers()
			.stream()
			.map(customer -> convertCustomerToReadableCustomer(customer, store, language))
			.collect(Collectors.toList());

		ReadableCustomerList readableCustomerList = new ReadableCustomerList();
		readableCustomerList.setContent(readableCustomers);
		readableCustomerList.setTotalElements(customerList.getTotalCount());
		readableCustomerList.setSize(customerList.getCustomers().size());
		readableCustomerList
			.setTotalPages((int) Math.ceil((double) customerList.getTotalCount() / readableCustomerList.getSize()));

		return readableCustomerList;
	}

	private ReadableCustomer convertCustomerToReadableCustomer(Customer customer, StoreMerchantId store,
			LanguageCode language) {
		ReadableCustomerPopulator populator = new ReadableCustomerPopulator();
		try {
			return populator.populate(customer, new ReadableCustomer(), store, language);
		}
		catch (ConversionException e) {
			throw new ConversionRuntimeException(e);
		}
	}

}
