package com.asrevo.cvhome.s2s.services;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.s2s.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

import java.util.Objects;

@AllArgsConstructor
@Slf4j
public class PermissionAccessChecker {

	private final StoreRoleAccessChecker storeRoleAccessChecker = new StoreRoleAccessChecker();

	public boolean hasAccessOnStoreUsersList(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasReadAccessOnStore(authentication, requestedStoreId);
	}

	public boolean hasAccessOnStoreUsersCreate(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnUsers(authentication, requestedStoreId);
	}

	public boolean hasAccessOnStoreUsersUpdate(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnUsers(authentication, requestedStoreId);
	}

	public boolean hasAccessOnStoreUsersDelete(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnUsers(authentication, requestedStoreId);
	}

	public boolean hasAccessOnStoreUsersEnable(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnUsers(authentication, requestedStoreId);
	}

	public boolean hasAccessOnStoreUsersDisable(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnUsers(authentication, requestedStoreId);
	}

	public boolean hasAccessOnStoreFindOne(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasReadAccessOnStore(authentication, requestedStoreId);
	}

	public boolean hasAccessOnStoreCreate(Authentication authentication, String org, Pod pod) {
		if (!storeRoleAccessChecker.isScopeStoreCore(authentication)) {
			log.debug("User {} does not have store scope with roles {}", authentication.getName(),
					SecurityUtils.getRoles(authentication));
			return false;
		}
		if (Objects.nonNull(pod) && Objects.nonNull(pod.orgId())) {
			log.debug("will check Org match pod org");
			if (!pod.orgId().id().toString().equals(org)) {
				log.debug("Org {} does not match pod org {}", org, pod.orgId().id());
				return false;
			}
		}
		return true;
	}

	public boolean isSameStorePod(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod) {
		if (storeRoleAccessChecker.isScopeStorePod(authentication, pod)) {
			return true;
		}
		return false;
	}

	public boolean hasManageAccessOnStore(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod) {
		if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId, pod)) {
			return true;
		}
		else if (storeRoleAccessChecker.isStoreAdmin(authentication, requestedStoreId, pod)) {
			return true;
		}
		return false;
	}

	public boolean hasAccessOnStoreDelete(Authentication authentication, ManagerStoreId requestedStoreId) {
		return hasMaintainAccessOnStore(authentication, requestedStoreId);
	}

	private boolean hasReadAccessOnStore(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId)) {
			return true;
		}
		else if (storeRoleAccessChecker.isStoreAdmin(authentication, requestedStoreId)) {
			return true;
		}
		else if (storeRoleAccessChecker.isStoreModerator(authentication, requestedStoreId)) {
			return true;
		}
		else if (storeRoleAccessChecker.isScopeStoreCore(authentication)) {
			return true;
		}
		else {
			log.debug("User {} does not have read access on store {} on roles {}", authentication.getName(),
					requestedStoreId, SecurityUtils.getRoles(authentication));
			return false;
		}
	}

	private boolean hasReadAccessOnStore(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod) {
		if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId, pod)) {
			return true;
		}
		else if (storeRoleAccessChecker.isStoreAdmin(authentication, requestedStoreId, pod)) {
			return true;
		}
		else if (storeRoleAccessChecker.isStoreModerator(authentication, requestedStoreId, pod)) {
			return true;
		}
		else if (storeRoleAccessChecker.isScopeStoreCore(authentication)) {
			return true;
		}
		else if (isSameStorePod(authentication, requestedStoreId, pod)) {
			return true;
		}
		else {
			log.debug("User {} does not have read access on store {} on roles {} on pod {}", authentication.getName(),
					requestedStoreId, SecurityUtils.getRoles(authentication), pod);
			return false;
		}
	}

	private boolean hasMaintainAccessOnStore(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId)) {
			return true;
		}
		log.debug("User {} does not have maintain access on store {} on roles {}", authentication.getName(),
				requestedStoreId, SecurityUtils.getRoles(authentication));
		return false;
	}

	private boolean hasMaintainAccessOnUsers(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId)) {
			return true;
		}
		else if (storeRoleAccessChecker.isStoreAdmin(authentication, requestedStoreId)) {
			return true;
		}
		else {
			log.debug("User {} does not have maintain access on users on store {} on roles {}",
					authentication.getName(), requestedStoreId, SecurityUtils.getRoles(authentication));
			return false;
		}
	}

}
