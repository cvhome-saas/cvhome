package com.asrevo.cvhome.s2s.services;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import org.springframework.security.core.Authentication;

public interface StoreSecurityService {
    boolean isSuperAdmin(Authentication authentication, ManagerStoreId requestedStoreId);

    boolean isOrgAdmin(Authentication authentication, ManagerStoreId requestedStoreId);

    boolean isStoreAdmin(Authentication authentication, ManagerStoreId requestedStoreId);

    boolean isStoreModerator(Authentication authentication, ManagerStoreId requestedStoreId);
}
