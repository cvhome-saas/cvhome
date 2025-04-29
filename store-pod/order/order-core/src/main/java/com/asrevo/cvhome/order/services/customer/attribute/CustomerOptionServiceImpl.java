package com.asrevo.cvhome.order.services.customer.attribute;

import com.asrevo.cvhome.order.entity.customer.attribute.CustomerOption;
import com.asrevo.cvhome.order.repositories.customer.attribute.CustomerOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("customerOptionService")
public class CustomerOptionServiceImpl implements CustomerOptionService {

    private final CustomerOptionRepository customerOptionRepository;

    @Autowired
    public CustomerOptionServiceImpl(CustomerOptionRepository customerOptionRepository) {
        this.customerOptionRepository = customerOptionRepository;
    }

    @Override
    public CustomerOption getById(Long id) {
        return customerOptionRepository.findById(id).orElse(null);
    }
}
