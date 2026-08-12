package com.asrevo.cvhome.s2s.services;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface StoreOrgOwnerRetriever {

    ManagerOrgId owner(StoreMerchantId store);

}
