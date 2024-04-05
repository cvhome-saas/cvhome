package com.asrevo.cvhome.store.core.services.system;

import com.asrevo.cvhome.store.core.entity.system.MerchantLog;
import com.asrevo.cvhome.store.core.repositories.system.MerchantLogRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("merchantLogService")
@Slf4j
public class MerchantLogServiceImpl extends
        SalesManagerEntityServiceImpl<Long, MerchantLog> implements
        MerchantLogService {


    @Autowired
    public MerchantLogServiceImpl(
            MerchantLogRepository merchantLogRepository) {
        super(merchantLogRepository);
    }


}
