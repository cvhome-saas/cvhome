package com.asrevo.cvhome.checkout.services.customer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.checkout.entity.customer.CustomerList;
import com.asrevo.cvhome.checkout.repositories.customer.CustomerRepository;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("customerService")
@Slf4j
public class CustomerServiceImpl extends SalesManagerEntityServiceImpl<Long, Customer> implements CustomerService {

	private final CustomerRepository customerRepository;

	@Autowired
	public CustomerServiceImpl(CustomerRepository customerRepository) {
		super(customerRepository);
		this.customerRepository = customerRepository;
	}

	@Override
	public Customer getById(Long id) {
		return customerRepository.findOne(id);
	}

	@Override
	public Customer getByNick(String nick, StoreMerchantId storeMerchantId) {
		return customerRepository.findByNick(nick, storeMerchantId);
	}

	@Override
	public CustomerList getListByStore(StoreMerchantId store, CustomerCriteria criteria) {
		return customerRepository.listByStore(store, criteria);
	}

	@Override
	public void saveOrUpdate(Customer customer) throws ServiceException {

		log.debug("Creating Customer");

		if (customer.getId() != null && customer.getId() > 0) {
			super.update(customer);
		}
		else {

			super.create(customer);
		}
	}

	public void delete(Customer customer) {
		customerRepository.delete(customer);
	}

}
