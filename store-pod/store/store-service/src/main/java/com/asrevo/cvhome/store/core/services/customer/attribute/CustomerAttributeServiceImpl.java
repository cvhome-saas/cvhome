package com.asrevo.cvhome.store.core.services.customer.attribute;

import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerAttribute;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.customer.attribute.CustomerAttributeRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("customerAttributeService")
public class CustomerAttributeServiceImpl extends
        SalesManagerEntityServiceImpl<Long, CustomerAttribute> implements CustomerAttributeService {

    private CustomerAttributeRepository customerAttributeRepository;

    @Autowired
    public CustomerAttributeServiceImpl(CustomerAttributeRepository customerAttributeRepository) {
        super(customerAttributeRepository);
        this.customerAttributeRepository = customerAttributeRepository;
    }


    @Override
    public void saveOrUpdate(CustomerAttribute customerAttribute)
            throws ServiceException {

        customerAttributeRepository.save(customerAttribute);


    }

    @Override
    public void delete(CustomerAttribute attribute) throws ServiceException {

        //override method, this allows the error that we try to remove a detached instance
        attribute = this.getById(attribute.getId());
        super.delete(attribute);

    }


    @Override
    public CustomerAttribute getByCustomerOptionId(MerchantStore store, Long customerId, Long id) {
        return customerAttributeRepository.findByOptionId(store.getId(), customerId, id);
    }


    @Override
    public List<CustomerAttribute> getByCustomer(MerchantStore store, Customer customer) {
        return customerAttributeRepository.findByCustomerId(store.getId(), customer.getId());
    }


    @Override
    public List<CustomerAttribute> getByCustomerOptionValueId(MerchantStore store,
                                                              Long id) {
        return customerAttributeRepository.findByOptionValueId(store.getId(), id);
    }

    @Override
    public List<CustomerAttribute> getByOptionId(MerchantStore store,
                                                 Long id) {
        return customerAttributeRepository.findByOptionId(store.getId(), id);
    }

}
