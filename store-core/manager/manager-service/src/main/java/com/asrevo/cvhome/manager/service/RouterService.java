package com.asrevo.cvhome.manager.service;


import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

import java.util.List;

public interface RouterService {
    void create(Domain domain, ManagerStoreId managerStoreId);

    ManagerStoreId getReferenceByDomain(Domain domain);

    List<Domain> allocations(ManagerStoreId store);

    void remove(Domain domain, ManagerStoreId store);
}