package com.asrevo.cvhome.s2s.services;

import static com.asrevo.cvhome.s2s.utils.SecurityUtils.getOrgStoreIdentity;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import static com.asrevo.cvhome.s2s.utils.SecurityUtils.*;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Objects;

@Slf4j
public class StoreSecurityServiceImpl implements StoreSecurityService {

	private final PodInfoProperties podInfoProperties;

	private final StoreOrgOwnerRetriever ownerRetriever;

	public StoreSecurityServiceImpl(PodInfoProperties podInfoProperties, StoreOrgOwnerRetriever ownerRetriever) {
		this.podInfoProperties = podInfoProperties;
		this.ownerRetriever = ownerRetriever;
	}

	public StoreSecurityServiceImpl(StoreOrgOwnerRetriever ownerRetriever) {
		this.podInfoProperties = null;
		this.ownerRetriever = ownerRetriever;
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
	public boolean isScopeStore(Authentication authentication) {
		if (!hasScopeStore(authentication)) {
			log.debug("User {} does not have store scope with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		return true;
	}

	@Override
	public boolean isScopeInternal(Authentication authentication) {
		if (!isPodInfoProvided()) {
			log.debug("PodInfoProperties is null, cannot check internal scope");
			return false;
		}
		if (!hasScopeInternal(authentication)) {
			log.debug("User {} does not have internal scope with roles {}", authentication.getName(),
					getRoles(authentication));
			return false;
		}
		if (!(authentication instanceof JwtAuthenticationToken)) {
			return false;
		}
		String resource = getResource(authentication);
		if (!resource.equals(podInfoProperties.pod().name())) {
			log.debug("User {} does not have internal scope with roles {} on resource {} not matched pod name {}",
					authentication.getName(), getRoles(authentication), resource, podInfoProperties.pod().name());
			return false;
		}

		return true;
	}

	public boolean isPodInfoProvided() {
		return (Objects.nonNull(podInfoProperties) && Objects.nonNull(podInfoProperties.pod()));
	}

	@Override
	public boolean isOrgPod() {
		return isPodInfoProvided() && Objects.nonNull(podInfoProperties.pod().orgId());
	}

	@Override
	public Pod getPod() {
		if (!isPodInfoProvided()) {
			return null;
		}
		return podInfoProperties.pod();
	}

	private static String getResource(Authentication authentication) {
		return ((JwtAuthenticationToken) authentication).getTokenAttributes().getOrDefault("resource", "").toString();
	}

}
