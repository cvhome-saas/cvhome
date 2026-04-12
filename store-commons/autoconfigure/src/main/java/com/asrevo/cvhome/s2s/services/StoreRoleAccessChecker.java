package com.asrevo.cvhome.s2s.services;

import static com.asrevo.cvhome.s2s.utils.SecurityUtils.getOrgStoreIdentity;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import static com.asrevo.cvhome.s2s.utils.SecurityUtils.*;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Objects;

@Slf4j
public class StoreRoleAccessChecker {

	public boolean isSuperAdmin(Authentication authentication) {
		return hasSuperAdminRole(authentication);
	}

	public boolean isOrgAdmin(Authentication authentication, ManagerStoreId requestedStoreId) {
		return isOrgAdmin(authentication, requestedStoreId, null);
	}

	public boolean isStoreAdmin(Authentication authentication, ManagerStoreId requestedStoreId) {
		return isStoreAdmin(authentication, requestedStoreId, null);
	}

	public boolean isStoreModerator(Authentication authentication, ManagerStoreId requestedStoreId) {
		return isStoreModerator(authentication, requestedStoreId, null);
	}

	public boolean isOrgAdmin(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod) {
		if (!hasOrgAdminRole(authentication)) {
			return false;
		}
		UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
		if (!isPodAllowOrg(pod, identity.org())) {
			log.debug("User {} does not have org admin role with roles {} on pod {} not allowed for org {}",
					authentication.getName(), getRoles(authentication), pod.name(), identity.org().id().toString());
			return false;
		}
		// @TODO find better way to know if requested store created by this org admin
		return true;
	}

	public boolean isStoreAdmin(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod) {
		if (!hasStoreAdminRole(authentication)) {
			log.debug("User {} does not have store admin role with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
		if (!isPodAllowOrg(pod, identity.org())) {
			log.debug("User {} does not have store admin role with roles {} on pod {} not allowed for org {}",
					authentication.getName(), getRoles(authentication), pod.name(), identity.org().id().toString());
			return false;
		}
		if (!requestedStoreId.getId().toString().equals(identity.store())) {
			log.debug("User {} does not have store admin role with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		return true;
	}

	public boolean isStoreModerator(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod) {
		if (!hasStoreModeratorRole(authentication)) {
			log.debug("User {} does not have store moderator role with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
		if (!isPodAllowOrg(pod, identity.org())) {
			log.debug("User {} does not have store moderator role with roles {} on pod {} not allowed for org {}",
					authentication.getName(), getRoles(authentication), pod.name(), identity.org().id().toString());
			return false;
		}
		if (!requestedStoreId.getId().toString().equals(identity.store())) {
			log.debug("User {} does not have store moderator role with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		return true;
	}

	private boolean isPodAllowOrg(Pod pod, ManagerOrgId orgId) {
		if (Objects.isNull(pod)) {
			return true;
		}
		if (Objects.isNull(pod.orgId())) {
			return true;
		}
		if (pod.orgId().equals(orgId)) {
			return true;
		}
		else {
			log.debug("Pod {} does not allow org {}", pod.name(), orgId);
			return false;
		}

	}

	public boolean isStoreCustomer(Authentication authentication, ManagerStoreId requestedStoreId) {
		if (!hasStoreCustomerRole(authentication)) {
			log.debug("User {} does not have store customer role with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		if (!(authentication instanceof JwtAuthenticationToken auth)) {
			return false;
		}
		String storeId = getStoreId(auth);
		if (!storeId.equals(requestedStoreId.getId().toString())) {
			log.debug("User {} does not have store customer role with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		return true;
	}

	public boolean isScopeStoreCore(Authentication authentication) {
		if (!hasScopeStoreCore(authentication)) {
			log.debug("User {} does not have store core scope with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		return true;
	}

	public boolean isScopeStorePod(Authentication authentication, Pod pod) {
		if (Objects.isNull(pod)) {
			log.debug("pod is null, cannot check internal scope");
			return false;
		}
		if (!hasScopeStorePod(authentication)) {
			log.debug("User {} does not have internal scope with roles {} pod {}", authentication.getName(),
					getRoles(authentication), pod);
			return false;
		}
		if (!(authentication instanceof JwtAuthenticationToken auth)) {
			return false;
		}
		String resource = getResource(auth);
		if (!resource.equals(pod.name())) {
			log.debug("User {} does not have internal scope with roles {} on resource {} not matched pod name {}",
					authentication.getName(), getRoles(authentication), resource, pod.name());
			return false;
		}

		return true;
	}

	private static String getResource(JwtAuthenticationToken authentication) {
		return authentication.getTokenAttributes().getOrDefault("resource", "").toString();
	}

	private static String getStoreId(JwtAuthenticationToken authentication) {
		return authentication.getTokenAttributes().getOrDefault("clientId", "").toString();
	}

}
