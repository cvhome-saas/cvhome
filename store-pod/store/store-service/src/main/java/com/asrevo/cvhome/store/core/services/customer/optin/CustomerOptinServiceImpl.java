package com.asrevo.cvhome.store.core.services.customer.optin;


import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.system.optin.CustomerOptin;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.customer.optin.CustomerOptinRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


@Service
public class CustomerOptinServiceImpl extends SalesManagerEntityServiceImpl<Long, CustomerOptin> implements CustomerOptinService {


    private final CustomerOptinRepository customerOptinRepository;


    @Autowired
    public CustomerOptinServiceImpl(CustomerOptinRepository customerOptinRepository) {
        super(customerOptinRepository);
        this.customerOptinRepository = customerOptinRepository;
    }

    @Override
    public void optinCumtomer(CustomerOptin optin) throws ServiceException {
        Assert.notNull(optin, "CustomerOptin must not be null");

        customerOptinRepository.save(optin);


    }

    @Override
    public void optoutCumtomer(CustomerOptin optin) throws ServiceException {
        Assert.notNull(optin, "CustomerOptin must not be null");

        customerOptinRepository.delete(optin);

    }

    @Override
    public CustomerOptin findByEmailAddress(MerchantStore store, String emailAddress, String code) throws ServiceException {
        return customerOptinRepository.findByMerchantAndCodeAndEmail(store.getId(), code, emailAddress);
    }

}
