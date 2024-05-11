package com.asrevo.cvhome.manager.service;


import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

public interface RouterService {
    void create(Domain domain, ManagerStoreId managerStoreId);

    ManagerStoreId getReferenceByDomain(Domain domain);

}