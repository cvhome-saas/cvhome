package com.asrevo.cvhome.uaa.service;

import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;

import java.util.Map;
import java.util.Set;

public interface UserAccountService {

	ReadableUser createUser(PersistableUser user);

	ReadableUser updateUser(PersistableUser user);

	ReadableUser current(String id);

	ReadableUserList list(Map<String, String> filters, Integer pageNumber, Integer pageSize);

	void deleteUser(String userId);

	void enableUser(String userId);

	void disableUser(String userId);

	ReadableUser findOne(String userId);

	void changePassword(String userId, UserPassword request);

	Set<String> getAssignableRoles();

}
