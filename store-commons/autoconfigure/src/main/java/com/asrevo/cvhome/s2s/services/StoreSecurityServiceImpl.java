package com.asrevo.cvhome.s2s.services;

import static com.asrevo.cvhome.s2s.utils.SecurityUtils.getOrgStoreIdentity;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import static com.asrevo.cvhome.s2s.utils.SecurityUtils.*;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
@Slf4j
public class StoreSecurityServiceImpl implements StoreSecurityService {

	private final StoreOrgOwnerRetriever ownerRetriever;

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
		return ownerRetriever.owner((requestedStoreId)).equals(identity.org());
	}

	@Override
	public boolean isStoreAdmin(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (!hasStoreAdminRole(authentication)) {
			log.debug("User {} does not have store admin role with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
		if (!requestedStoreId.getId().toString().equals(identity.store())) {
			log.debug("User {} does not have store admin role with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		return ownerRetriever.owner((requestedStoreId)).equals(identity.org());
	}

	@Override
	public boolean isStoreModerator(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (!hasStoreModeratorRole(authentication)) {
			log.debug("User {} does not have store moderator role with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
		if (!requestedStoreId.getId().toString().equals(identity.store())) {
			log.debug("User {} does not have store moderator role with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		return ownerRetriever.owner((requestedStoreId)).equals(identity.org());
	}

	@Override
	public boolean isMicroService(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (!hasScopeInternal(authentication)) {
			log.debug("User {} does not have micro service role with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		return true;
	}

}
