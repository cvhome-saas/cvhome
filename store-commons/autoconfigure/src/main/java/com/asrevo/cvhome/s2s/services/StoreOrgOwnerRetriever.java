package com.asrevo.cvhome.s2s.services;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

public interface StoreOrgOwnerRetriever {

    ManagerOrgId owner(ManagerStoreId store);

}
