package com.asrevo.cvhome.uaa.service.impl;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import java.security.Principal;

public class UserAccountServiceImpl implements UserAccountService {

	@Override
	public ReadableUser createOrgUser(ManagerOrgId org, PersistableUser user) {
		return null;
	}

	@Override
	public ReadableUser current(String id) {
		return null;
	}

	@Override
	public ReadableUserList list(Principal principal, UserOrgStoreIdentity identity, ManagerStoreId store) {
		return null;
	}

	@Override
	public ReadableUser createManagedUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser create) {
		return null;
	}

	@Override
	public ReadableUser updateManagedUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser user) {
		return null;
	}

	@Override
	public void resetPassword(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store,
			UserPassword passwordRequestDto, String userId, boolean temporary) {

	}

	@Override
	public boolean usernameExist(String username) {
		return false;
	}

	@Override
	public void deleteUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId) {

	}

	@Override
	public void enableUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId) {

	}

	@Override
	public void disableUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId) {

	}

	@Override
	public ReadableUser findOne(UserOrgStoreIdentity identity, String userId) {
		return null;
	}

	@Override
	public void changePassword(ManagerOrgId managerOrgId, UserPassword request) {

	}

}
