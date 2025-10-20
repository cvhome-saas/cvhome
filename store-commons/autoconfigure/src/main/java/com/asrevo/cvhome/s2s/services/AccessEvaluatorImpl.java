package com.asrevo.cvhome.s2s.services;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
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

	@Override
	public boolean hasAccessOnStoreDomainList(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasReadAccessOnStore(authentication, requestedStoreId);
	}

	@Override
	public boolean hasAccessOnStoreDomainCreate(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnStoreDomain(authentication, requestedStoreId);
	}

	@Override
	public boolean hasAccessOnStoreDomainDelete(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnStoreDomain(authentication, requestedStoreId);
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
			return false;
		}
	}

	private boolean hasMaintainAccessOnStoreDomain(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (securityRoleCheckService.isOrgAdmin(authentication, requestedStoreId)) {
			return true;
		}
		else if (securityRoleCheckService.isStoreAdmin(authentication, requestedStoreId)) {
			return true;
		}
		else {
			return false;
		}
	}

}
