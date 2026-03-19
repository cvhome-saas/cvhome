package com.asrevo.cvhome.s2s.services;

import static com.asrevo.cvhome.s2s.utils.SecurityUtils.getOrgStoreIdentity;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
@Slf4j
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

	private static boolean hasScopeInternal(Authentication authentication) {
		return hasRole(authentication, Roles.SCOPE_INTERNAL);
	}

	private static boolean hasRole(Authentication authentication, Roles role) {
		return StoreSecurityService.getRoles(authentication).stream().anyMatch(it -> it.contains(role.name()));
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
			log.debug("User {} does not have store admin role with roles {}", authentication.getName(),
					StoreSecurityService.getRoles(authentication));
			return false;
		}
		UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
		if (!requestedStoreId.getId().toString().equals(identity.store())) {
			log.debug("User {} does not have store admin role with roles {}", authentication.getName(),
					StoreSecurityService.getRoles(authentication));
			return false;
		}
		return getOwnerForStore.apply((requestedStoreId)).equals(identity.org());
	}

	@Override
	public boolean isStoreModerator(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (!hasStoreModeratorRole(authentication)) {
			log.debug("User {} does not have store moderator role with roles {}", authentication.getName(),
					StoreSecurityService.getRoles(authentication));
			return false;
		}
		UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
		if (!requestedStoreId.getId().toString().equals(identity.store())) {
			log.debug("User {} does not have store moderator role with roles {}", authentication.getName(),
					StoreSecurityService.getRoles(authentication));
			return false;
		}
		return getOwnerForStore.apply((requestedStoreId)).equals(identity.org());
	}

	@Override
	public boolean isMicroService(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (!hasScopeInternal(authentication)) {
			log.debug("User {} does not have micro service role with roles {}", authentication.getName(),
					StoreSecurityService.getRoles(authentication));
			return false;
		}
		return true;
	}

}
