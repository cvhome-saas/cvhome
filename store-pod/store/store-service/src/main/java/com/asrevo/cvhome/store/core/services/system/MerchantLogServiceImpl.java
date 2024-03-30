package com.asrevo.cvhome.store.core.services.system;

import com.asrevo.cvhome.store.core.repositories.system.MerchantLogRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.asrevo.cvhome.store.core.entity.system.MerchantLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("merchantLogService")
public class MerchantLogServiceImpl extends
        SalesManagerEntityServiceImpl<Long, MerchantLog> implements
        MerchantLogService {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantLogServiceImpl.class);


    private MerchantLogRepository merchantLogRepository;

    @Autowired
    public MerchantLogServiceImpl(
            MerchantLogRepository merchantLogRepository) {
        super(merchantLogRepository);
        this.merchantLogRepository = merchantLogRepository;
    }


}
