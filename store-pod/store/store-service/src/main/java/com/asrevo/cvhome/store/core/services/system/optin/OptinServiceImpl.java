package com.asrevo.cvhome.store.core.services.system.optin;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.system.optin.Optin;
import com.asrevo.cvhome.store.core.entity.system.optin.OptinType;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.system.OptinRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OptinServiceImpl extends SalesManagerEntityServiceImpl<Long, Optin> implements OptinService {


    private final OptinRepository optinRepository;

    @Autowired
    public OptinServiceImpl(OptinRepository optinRepository) {
        super(optinRepository);
        this.optinRepository = optinRepository;
    }


    @Override
    public Optin getOptinByCode(MerchantStore store, String code) throws ServiceException {
        return optinRepository.findByMerchantAndCode(store.getId(), code);
    }

    @Override
    public Optin getOptinByMerchantAndType(MerchantStore store, OptinType type) throws ServiceException {
        return optinRepository.findByMerchantAndType(store.getId(), type);
    }

}
