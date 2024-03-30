package com.asrevo.cvhome.store.core.services.customer.attribute;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.customer.attribute.CustomerOptionSetRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOption;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionSet;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionValue;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;


@Service("customerOptionSetService")
public class CustomerOptionSetServiceImpl extends
        SalesManagerEntityServiceImpl<Long, CustomerOptionSet> implements CustomerOptionSetService {


    @Autowired
    private CustomerOptionSetRepository customerOptionSetRepository;


    @Autowired
    public CustomerOptionSetServiceImpl(
            CustomerOptionSetRepository customerOptionSetRepository) {
        super(customerOptionSetRepository);
        this.customerOptionSetRepository = customerOptionSetRepository;
    }


    @Override
    public List<CustomerOptionSet> listByOption(CustomerOption option, MerchantStore store) throws ServiceException {
        Assert.notNull(store, "merchant store cannot be null");
        Assert.notNull(option, "option cannot be null");

        return customerOptionSetRepository.findByOptionId(store.getId(), option.getId());
    }

    @Override
    public void delete(CustomerOptionSet customerOptionSet) throws ServiceException {
        customerOptionSet = customerOptionSetRepository.findOne(customerOptionSet.getId());
        super.delete(customerOptionSet);
    }

    @Override
    public List<CustomerOptionSet> listByStore(MerchantStore store, Language language) throws ServiceException {
        Assert.notNull(store, "merchant store cannot be null");


        return customerOptionSetRepository.findByStore(store.getId(), language.getId());
    }


    @Override
    public void saveOrUpdate(CustomerOptionSet entity) throws ServiceException {
        Assert.notNull(entity, "customer option set cannot be null");

        if (entity.getId() > 0) {
            super.update(entity);
        } else {
            super.create(entity);
        }

    }


    @Override
    public List<CustomerOptionSet> listByOptionValue(
            CustomerOptionValue optionValue, MerchantStore store)
            throws ServiceException {
        return customerOptionSetRepository.findByOptionValueId(store.getId(), optionValue.getId());
    }


}
