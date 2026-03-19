package com.asrevo.cvhome.uaa.service.impl;

import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.sdk.AdminUserClient;
import com.asrevo.cvhome.uaa.sdk.dto.*;
import com.asrevo.cvhome.uaa.service.UserAccountService;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

	private AdminUserClient client;

	@Override
	public ReadableUser createUser(PersistableUser user) {
		var createdUser = client.createUser(CreateUserRequest.builder()
			.email(user.getEmailAddress())
			.username(user.getUserName())
			.firstName(user.getFirstName())
			.lastName(user.getLastName())
			.metadata(extractMetadata(user))
			.build());

		return toReadableUser(createdUser);
	}

	@Override
	public ReadableUser updateUser(PersistableUser user) {
		var updatedUser = client.updateUser(user.getId(),
				UpdateUserRequest.builder()
					.firstName(user.getFirstName())
					.lastName(user.getLastName())
					.enabled(user.isActive())
					.metadata(extractMetadata(user))
					.build());
		return toReadableUser(updatedUser);
	}

	private static Map<String, String> extractMetadata(PersistableUser user) {
		HashMap<String, String> m = new HashMap<>();
		if (Objects.nonNull(user.getOrg())) {
			m.put("org", user.getOrg());
		}
		if (Objects.nonNull(user.getStore())) {
			m.put("store", user.getStore());
		}
		return m;
	}

	private static ReadableUser toReadableUser(UserDto u) {
		ReadableUser readableUser = new ReadableUser();
		readableUser.setId(u.id().toString());
		readableUser.setEmailAddress(u.email());
		readableUser.setUserName(u.username());
		readableUser.setFirstName(u.firstName());
		readableUser.setLastName(u.lastName());
		readableUser.setOrg((String) u.metadata().getOrDefault("org", null));
		readableUser.setStore((String) u.metadata().getOrDefault("store", null));
		readableUser.setActive(u.enabled());
		return readableUser;
	}

	@Override
	public ReadableUser current(String id) {
		return toReadableUser(client.getUser(id));
	}

	@Override
	public ReadableUserList list(Map<String, String> filters, Integer pageNumber, Integer pageSize) {
		PageResponse<UserDto> response = client.listUsers(filters, new PageRequest(pageNumber, pageSize));
		ReadableUserList list = new ReadableUserList();
		list.setTotalElements(response.totalElements());
		list.setTotalPages(response.totalPages());
		list.setSize(response.size());
		list.setPageNumber(response.number());
		list.setContent(response.content().stream().map(UserAccountServiceImpl::toReadableUser).toList());
		return list;
	}

	@Override
	public void deleteUser(String userId) {
		client.deleteUser(userId);
	}

	@Override
	public void enableUser(String userId) {
		client.enableUser(userId);
	}

	@Override
	public void disableUser(String userId) {
		client.disableUser(userId);
	}

	@Override
	public ReadableUser findOne(String userId) {
		return toReadableUser(client.getUser(userId));
	}

	@Override
	public void changePassword(String userId, UserPassword request) {
		client.resetPassword(userId, request.getChangePassword());
	}

}
