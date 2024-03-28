package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;
import org.springframework.security.core.Authentication;

public interface SecurityService {
    boolean isOrgAdmin(Authentication authentication, ManagerStoreId requestedStoreId);

    boolean isStoreAdmin(Authentication authentication, ManagerStoreId requestedStoreId);
}
