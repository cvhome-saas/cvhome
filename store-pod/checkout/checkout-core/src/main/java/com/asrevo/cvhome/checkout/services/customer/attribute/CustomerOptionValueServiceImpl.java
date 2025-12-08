package com.asrevo.cvhome.checkout.services.customer.attribute;

import com.asrevo.cvhome.checkout.entity.customer.attribute.CustomerOptionValue;
import com.asrevo.cvhome.checkout.repositories.customer.attribute.CustomerOptionValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("customerOptionValueService")
public class CustomerOptionValueServiceImpl implements CustomerOptionValueService {

	private final CustomerOptionValueRepository customerOptionValueRepository;

	@Autowired
	public CustomerOptionValueServiceImpl(CustomerOptionValueRepository customerOptionValueRepository) {
		this.customerOptionValueRepository = customerOptionValueRepository;
	}

	@Override
	public CustomerOptionValue getById(Long id) {
		return customerOptionValueRepository.findById(id).orElse(null);
	}

}
