package com.asrevo.cvhome.s2s.services;

import org.springframework.security.core.Authentication;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface StoreSecurityService {

    boolean isSuperAdmin(Authentication authentication);

    boolean isOrgAdmin(Authentication authentication, StoreMerchantId requestedStoreId);

    boolean isStoreAdmin(Authentication authentication, StoreMerchantId requestedStoreId);

    boolean isStoreModerator(Authentication authentication, StoreMerchantId requestedStoreId);

    boolean isOrgAdmin(Authentication authentication, StoreMerchantId requestedStoreId, Pod pod);

    boolean isStoreAdmin(Authentication authentication, StoreMerchantId requestedStoreId, Pod pod);

    boolean isStoreModerator(Authentication authentication, StoreMerchantId requestedStoreId, Pod pod);

    boolean isStoreCustomer(Authentication authentication, StoreMerchantId requestedStoreId);

    boolean isScopeStoreCore(Authentication authentication);

    boolean isScopeStorePod(Authentication authentication, Pod pod);

}
