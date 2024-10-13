package com.asrevo.cvhome.store.core.services.customer.attribute;

import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerAttribute;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOption;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerOptionSet;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.customer.attribute.CustomerOptionRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("customerOptionService")
public class CustomerOptionServiceImpl extends SalesManagerEntityServiceImpl<Long, CustomerOption>
        implements CustomerOptionService {

    private final CustomerOptionRepository customerOptionRepository;

    private final CustomerAttributeService customerAttributeService;

    private final CustomerOptionSetService customerOptionSetService;

    @Autowired
    public CustomerOptionServiceImpl(
            CustomerOptionRepository customerOptionRepository,
            CustomerAttributeService customerAttributeService,
            CustomerOptionSetService customerOptionSetService) {
        super(customerOptionRepository);
        this.customerOptionRepository = customerOptionRepository;
        this.customerAttributeService = customerAttributeService;
        this.customerOptionSetService = customerOptionSetService;
    }

    @Override
    public List<CustomerOption> listByStore(MerchantStore store, Language language)
            throws ServiceException {

        return customerOptionRepository.findByStore(store.getId(), language.getId());
    }

    @Override
    public void saveOrUpdate(CustomerOption entity) throws ServiceException {

        // save or update (persist and attach entities
        if (entity.getId() != null && entity.getId() > 0) {
            super.update(entity);
        } else {
            super.save(entity);
        }
    }

    @Override
    public void delete(CustomerOption customerOption) throws ServiceException {

        // remove all attributes having this option
        List<CustomerAttribute> attributes =
                customerAttributeService.getByOptionId(
                        customerOption.getMerchantStore(), customerOption.getId());

        for (CustomerAttribute attribute : attributes) {
            customerAttributeService.delete(attribute);
        }

        CustomerOption option = this.getById(customerOption.getId());

        List<CustomerOptionSet> optionSets =
                customerOptionSetService.listByOption(
                        customerOption, customerOption.getMerchantStore());

        for (CustomerOptionSet optionSet : optionSets) {
            customerOptionSetService.delete(optionSet);
        }

        // remove option
        super.delete(option);
    }

    @Override
    public CustomerOption getByCode(MerchantStore store, String optionCode) {
        return customerOptionRepository.findByCode(store.getId(), optionCode);
    }
}
