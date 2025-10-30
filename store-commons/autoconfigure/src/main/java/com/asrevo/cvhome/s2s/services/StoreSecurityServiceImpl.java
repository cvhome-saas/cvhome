package com.asrevo.cvhome.s2s.services;

import static com.asrevo.cvhome.s2s.utils.SecurityUtils.getOrgStoreIdentity;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
public class StoreSecurityServiceImpl implements StoreSecurityService {

	private final Function<ManagerStoreId, ManagerOrgId> getOwnerForStore;

	private static boolean hasSuperAdminRole(Authentication authentication) {
		return hasRole(authentication, Roles.ROLE_SUPER_ADMIN);
	}

	private static boolean hasOrgAdminRole(Authentication authentication) {
		return hasRole(authentication, Roles.ROLE_ORG_ADMIN);
	}

	private static boolean hasStoreAdminRole(Authentication authentication) {
		return hasRole(authentication, Roles.ROLE_STORE_ADMIN);
	}

	private static boolean hasStoreModeratorRole(Authentication authentication) {
		return hasRole(authentication, Roles.ROLE_STORE_MODERATOR);
	}

	private static boolean hasMicroServiceRole(Authentication authentication) {
		return hasRole(authentication, Roles.ROLE_MICROSERVICE);
	}

	private static boolean hasRole(Authentication authentication, Roles role) {
		return authentication.getAuthorities().stream().anyMatch(it -> it.getAuthority().contains(role.name()));
	}

	@Override
	public boolean isSuperAdmin(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasSuperAdminRole(authentication);
	}

	@Override
	public boolean isOrgAdmin(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (!hasOrgAdminRole(authentication)) {
			return false;
		}
		UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
		return getOwnerForStore.apply((requestedStoreId)).equals(identity.org());
	}

	@Override
	public boolean isStoreAdmin(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (!hasStoreAdminRole(authentication)) {
			return false;
		}
		UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
		if (!requestedStoreId.getId().toString().equals(identity.store())) {
			return false;
		}
		return getOwnerForStore.apply((requestedStoreId)).equals(identity.org());
	}

	@Override
	public boolean isStoreModerator(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (!hasStoreModeratorRole(authentication)) {
			return false;
		}
		UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
		if (!requestedStoreId.getId().toString().equals(identity.store())) {
			return false;
		}
		return getOwnerForStore.apply((requestedStoreId)).equals(identity.org());
	}

	@Override
	public boolean isMicroService(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (!hasMicroServiceRole(authentication)) {
			return false;
		}
		return true;
	}

}
