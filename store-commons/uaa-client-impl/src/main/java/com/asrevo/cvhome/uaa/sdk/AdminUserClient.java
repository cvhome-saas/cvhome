package com.asrevo.cvhome.uaa.sdk;

import com.asrevo.cvhome.uaa.sdk.dto.*;
import tools.jackson.core.type.TypeReference;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class AdminUserClient extends AbstractAdminClient {

	private final String usersApiUrl;

	public AdminUserClient(String baseUrl, String clientId, String clientSecret) {
		super(baseUrl, clientId, clientSecret);
		this.usersApiUrl = baseUrl + "/api/v1/admin/users";
	}

	public PageResponse<UserDto> listUsers(Map<String, String> metadataFilters, PageRequest pageRequest) {
		String url = usersApiUrl;
		StringJoiner sj = new StringJoiner("&", "?", "");
		if (metadataFilters != null && !metadataFilters.isEmpty()) {
			for (Map.Entry<String, String> entry : metadataFilters.entrySet()) {
				String key = URLEncoder.encode("metadata[" + entry.getKey() + "]", StandardCharsets.UTF_8);
				String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
				sj.add(key + "=" + value);
			}
		}

		if (pageRequest != null) {
			sj.add("page=" + pageRequest.page());
			sj.add("size=" + pageRequest.size());
		}

		if (sj.length() > 1) {
			url += sj.toString();
		}

		HttpRequest request = authenticatedRequestBuilder(url).GET().build();

		return sendAndParsePage(request, new TypeReference<>() {
		});
	}

	public UserDto getUser(String id) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id).GET().build();
		return sendAndParse(request, UserDto.class);
	}

	public UserDto createUser(CreateUserRequest req) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl)
			.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
			.header("Content-Type", "application/json")
			.build();
		return sendAndParse(request, UserDto.class);
	}

	public UserDto updateUser(String id, UpdateUserRequest req) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id)
			.PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
			.header("Content-Type", "application/json")
			.build();
		return sendAndParse(request, UserDto.class);
	}

	public boolean usernameExist(String username) {
		String url = usersApiUrl + "/exists?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
		HttpRequest request = authenticatedRequestBuilder(url).GET().build();
		return sendAndParse(request, Boolean.class);
	}

	public void enableUser(String id) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id + "/enable")
			.POST(HttpRequest.BodyPublishers.noBody())
			.build();
		sendAndVerify(request);
	}

	public void disableUser(String id) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id + "/disable")
			.POST(HttpRequest.BodyPublishers.noBody())
			.build();
		sendAndVerify(request);
	}

	public void deleteUser(String id) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id).DELETE().build();
		sendAndVerify(request);
	}

	public void resetPassword(String id, String newPassword) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id + "/reset-password")
			.PUT(HttpRequest.BodyPublishers
				.ofString(objectMapper.writeValueAsString(new ResetUserPasswordRequest(newPassword))))
			.header("Content-Type", "application/json")
			.build();
		sendAndVerify(request);
	}

	public void assignRoles(String id, List<String> roles) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id + "/roles")
			.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
			.header("Content-Type", "application/json")
			.build();
		sendAndVerify(request);
	}

	public void removeRoles(String id, List<String> roles) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id + "/roles/remove")
			.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
			.header("Content-Type", "application/json")
			.build();
		sendAndVerify(request);
	}

	public Set<String> getAssignableRoles() {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/assignable-roles").GET().build();
		return sendAndParse(request, new TypeReference<>() {
		});
	}

}
