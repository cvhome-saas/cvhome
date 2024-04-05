package com.asrevo.cvhome.store.core.services.customer;

import com.asrevo.cvhome.store.core.entity.common.Address;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.store.core.entity.customer.CustomerList;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerAttribute;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.customer.CustomerRepository;
import com.asrevo.cvhome.store.core.services.customer.attribute.CustomerAttributeService;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.asrevo.cvhome.store.core.services.geo.GeoLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("customerService")
@Slf4j
public class CustomerServiceImpl extends SalesManagerEntityServiceImpl<Long, Customer> implements CustomerService {


    private final CustomerRepository customerRepository;

    private final CustomerAttributeService customerAttributeService;

    private final GeoLocation geoLocation;


    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerAttributeService customerAttributeService, GeoLocation geoLocation) {
        super(customerRepository);
        this.customerRepository = customerRepository;
        this.customerAttributeService = customerAttributeService;
        this.geoLocation = geoLocation;
    }

    @Override
    public List<Customer> getByName(String firstName) {
        return customerRepository.findByName(firstName);
    }

    @Override
    public Customer getById(Long id) {
        return customerRepository.findOne(id);
    }

    @Override
    public Customer getByNick(String nick) {
        return customerRepository.findByNick(nick);
    }

    @Override
    public Customer getByNick(String nick, int storeId) {
        return customerRepository.findByNick(nick, storeId);
    }

    @Override
    public List<Customer> getListByStore(MerchantStore store) {
        return customerRepository.findByStore(store.getId());
    }

    @Override
    public CustomerList getListByStore(MerchantStore store, CustomerCriteria criteria) {
        return customerRepository.listByStore(store, criteria);
    }

    @Override
    public Address getCustomerAddress(MerchantStore store, String ipAddress) throws ServiceException {

        try {
            return geoLocation.getAddress(ipAddress);
        } catch (Exception e) {
            throw new ServiceException(e);
        }

    }

    @Override
    public void saveOrUpdate(Customer customer) throws ServiceException {

        log.debug("Creating Customer");

        if (customer.getId() != null && customer.getId() > 0) {
            super.update(customer);
        } else {

            super.create(customer);

        }
    }

    public void delete(Customer customer) throws ServiceException {
        customer = getById(customer.getId());

        //delete attributes
        List<CustomerAttribute> attributes = customerAttributeService.getByCustomer(customer.getMerchantStore(), customer);
        if (attributes != null) {
            for (CustomerAttribute attribute : attributes) {
                customerAttributeService.delete(attribute);
            }
        }
        customerRepository.delete(customer);

    }

    @Override
    public Customer getByNick(String nick, String code) {
        return customerRepository.findByNick(nick, code);
    }

    @Override
    public Customer getByPasswordResetToken(String storeCode, String token) {
        return customerRepository.findByResetPasswordToken(token, storeCode);
    }


}
