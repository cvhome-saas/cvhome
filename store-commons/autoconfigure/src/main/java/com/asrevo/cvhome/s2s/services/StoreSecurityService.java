package com.asrevo.cvhome.s2s.services;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import org.springframework.security.core.Authentication;

public interface StoreSecurityService {

	boolean isSuperAdmin(Authentication authentication);

	boolean isOrgAdmin(Authentication authentication, ManagerStoreId requestedStoreId);

	boolean isStoreAdmin(Authentication authentication, ManagerStoreId requestedStoreId);

	boolean isStoreModerator(Authentication authentication, ManagerStoreId requestedStoreId);

	boolean isOrgAdmin(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod);

	boolean isStoreAdmin(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod);

	boolean isStoreModerator(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod);

	boolean isStoreCustomer(Authentication authentication, ManagerStoreId requestedStoreId);

	boolean isScopeStoreCore(Authentication authentication);

	boolean isScopeStorePod(Authentication authentication, Pod pod);

}
