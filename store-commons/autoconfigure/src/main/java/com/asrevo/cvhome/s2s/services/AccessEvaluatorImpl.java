package com.asrevo.cvhome.s2s.services;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.s2s.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
@Slf4j
public class AccessEvaluatorImpl implements AccessEvaluator {

	private final StoreSecurityService securityRoleCheckService;

	@Override
	public boolean hasAccessOnStoreUsersList(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasReadAccessOnStore(authentication, requestedStoreId);
	}

	@Override
	public boolean hasAccessOnStoreUsersCreate(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnUsers(authentication, requestedStoreId);
	}

	@Override
	public boolean hasAccessOnStoreUsersUpdate(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnUsers(authentication, requestedStoreId);
	}

	@Override
	public boolean hasAccessOnStoreUsersDelete(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnUsers(authentication, requestedStoreId);
	}

	@Override
	public boolean hasAccessOnStoreUsersEnable(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnUsers(authentication, requestedStoreId);
	}

	@Override
	public boolean hasAccessOnStoreUsersDisable(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnUsers(authentication, requestedStoreId);
	}

	@Override
	public boolean hasAccessOnStoreFindOne(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasReadAccessOnStore(authentication, requestedStoreId);
	}

	private boolean hasReadAccessOnStore(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (securityRoleCheckService.isOrgAdmin(authentication, requestedStoreId)) {
			return true;
		}
		else if (securityRoleCheckService.isStoreAdmin(authentication, requestedStoreId)) {
			return true;
		}
		else if (securityRoleCheckService.isStoreModerator(authentication, requestedStoreId)) {
			return true;
		}
		else if (securityRoleCheckService.isMicroService(authentication, requestedStoreId)) {
			return true;
		}
		else {
			log.debug("User {} does not have read access on store {} on roles {}", authentication.getName(),
					requestedStoreId, SecurityUtils.getRoles(authentication));
			return false;
		}
	}

	private boolean hasMaintainAccessOnUsers(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (securityRoleCheckService.isOrgAdmin(authentication, requestedStoreId)) {
			return true;
		}
		else if (securityRoleCheckService.isStoreAdmin(authentication, requestedStoreId)) {
			return true;
		}
		else {
			log.debug("User {} does not have maintain access on users on store {} on roles {}",
					authentication.getName(), requestedStoreId, SecurityUtils.getRoles(authentication));
			return false;
		}
	}

}
