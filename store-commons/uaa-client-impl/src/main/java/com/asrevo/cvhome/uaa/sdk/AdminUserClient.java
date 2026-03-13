package com.asrevo.cvhome.uaa.sdk;

import com.asrevo.cvhome.uaa.sdk.dto.*;
import tools.jackson.core.type.TypeReference;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

public class AdminUserClient extends AbstractAdminClient {

	private final String usersApiUrl;

	public AdminUserClient(String baseUrl, String clientId, String clientSecret) {
		super(baseUrl, clientId, clientSecret);
		this.usersApiUrl = baseUrl + "/api/v1/admin/users";
	}

	private String buildUrl(String baseUrl, Map<String, String> metadataFilters) {
		StringJoiner sj = new StringJoiner("&", "?", "");
		if (metadataFilters != null && !metadataFilters.isEmpty()) {
			for (Map.Entry<String, String> entry : metadataFilters.entrySet()) {
				String key = URLEncoder.encode("metadata[" + entry.getKey() + "]", StandardCharsets.UTF_8);
				String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
				sj.add(key + "=" + value);
			}
		}
		return sj.length() > 1 ? baseUrl + sj : baseUrl;
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

	public UserDto getUser(UUID id, Map<String, String> metadataFilters) {
		HttpRequest request = authenticatedRequestBuilder(buildUrl(usersApiUrl + "/" + id, metadataFilters)).GET()
			.build();
		return sendAndParse(request, UserDto.class);
	}

	public UserDto createUser(CreateUserRequest req) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl)
			.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
			.header("Content-Type", "application/json")
			.build();
		return sendAndParse(request, UserDto.class);
	}

	public UserDto updateUser(UUID id, UpdateUserRequest req, Map<String, String> metadataFilters) {
		HttpRequest request = authenticatedRequestBuilder(buildUrl(usersApiUrl + "/" + id, metadataFilters))
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

	public void enableUser(UUID id, Map<String, String> metadataFilters) {
		HttpRequest request = authenticatedRequestBuilder(buildUrl(usersApiUrl + "/" + id + "/enable", metadataFilters))
			.POST(HttpRequest.BodyPublishers.noBody())
			.build();
		sendAndVerify(request);
	}

	public void disableUser(UUID id, Map<String, String> metadataFilters) {
		HttpRequest request = authenticatedRequestBuilder(
				buildUrl(usersApiUrl + "/" + id + "/disable", metadataFilters))
			.POST(HttpRequest.BodyPublishers.noBody())
			.build();
		sendAndVerify(request);
	}

	public void deleteUser(UUID id, Map<String, String> metadataFilters) {
		HttpRequest request = authenticatedRequestBuilder(buildUrl(usersApiUrl + "/" + id, metadataFilters)).DELETE()
			.build();
		sendAndVerify(request);
	}

	public void resetPassword(UUID id, String newPassword, Map<String, String> metadataFilters) {
		HttpRequest request = authenticatedRequestBuilder(
				buildUrl(usersApiUrl + "/" + id + "/reset-password", metadataFilters))
			.PUT(HttpRequest.BodyPublishers
				.ofString(objectMapper.writeValueAsString(new ResetUserPasswordRequest(newPassword))))
			.header("Content-Type", "application/json")
			.build();
		sendAndVerify(request);
	}

	public void assignRoles(UUID id, List<String> roles, Map<String, String> metadataFilters) {
		HttpRequest request = authenticatedRequestBuilder(buildUrl(usersApiUrl + "/" + id + "/roles", metadataFilters))
			.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
			.header("Content-Type", "application/json")
			.build();
		sendAndVerify(request);
	}

	public void removeRoles(UUID id, List<String> roles, Map<String, String> metadataFilters) {
		HttpRequest request = authenticatedRequestBuilder(
				buildUrl(usersApiUrl + "/" + id + "/roles/remove", metadataFilters))
			.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
			.header("Content-Type", "application/json")
			.build();
		sendAndVerify(request);
	}

}
