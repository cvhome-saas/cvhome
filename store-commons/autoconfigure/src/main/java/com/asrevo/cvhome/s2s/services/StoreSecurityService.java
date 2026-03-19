package com.asrevo.cvhome.s2s.services;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public interface StoreSecurityService {

	boolean isSuperAdmin(Authentication authentication, ManagerStoreId requestedStoreId);

	boolean isOrgAdmin(Authentication authentication, ManagerStoreId requestedStoreId);

	boolean isStoreAdmin(Authentication authentication, ManagerStoreId requestedStoreId);

	boolean isStoreModerator(Authentication authentication, ManagerStoreId requestedStoreId);

	boolean isMicroService(Authentication authentication, ManagerStoreId requestedStoreId);

	static Set<String> getRoles(Authentication authentication) {
		return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
	}

}
