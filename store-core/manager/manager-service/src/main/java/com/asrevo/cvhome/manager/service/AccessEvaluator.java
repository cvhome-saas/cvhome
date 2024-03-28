package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;
import org.springframework.security.core.Authentication;

public interface AccessEvaluator {
    boolean hasAccessOnStoreUsersList(Authentication authentication, ManagerStoreId requestedStoreId);

    boolean hasAccessOnStoreUsersCreate(Authentication authentication, ManagerStoreId requestedStoreId);

    boolean hasAccessOnStoreUsersDelete(Authentication authentication, ManagerStoreId requestedStoreId);

    boolean hasAccessOnStoreUsersEnable(Authentication authentication, ManagerStoreId requestedStoreId);

    boolean hasAccessOnStoreUsersDisable(Authentication authentication, ManagerStoreId requestedStoreId);
}
