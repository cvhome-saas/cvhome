package com.asrevo.cvhome.uaa.sdk;

import com.asrevo.cvhome.uaa.sdk.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
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

	public PageResponse<UserDto> listUsers(Map<String, String> metadataFilters) {
		String url = usersApiUrl;
		if (metadataFilters != null && !metadataFilters.isEmpty()) {
			StringJoiner sj = new StringJoiner("&", "?", "");
			for (Map.Entry<String, String> entry : metadataFilters.entrySet()) {
				String key = URLEncoder.encode("metadata[" + entry.getKey() + "]", StandardCharsets.UTF_8);
				String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
				sj.add(key + "=" + value);
			}
			url += sj.toString();
		}

		HttpRequest request = authenticatedRequestBuilder(url).GET().build();

		return sendAndParsePage(request, new TypeReference<>() {
		});
	}

	public UserDto getUser(UUID id) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id).GET().build();
		return sendAndParse(request, UserDto.class);
	}

	public UserDto createUser(CreateUserRequest req) {
		try {
			HttpRequest request = authenticatedRequestBuilder(usersApiUrl)
				.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
				.header("Content-Type", "application/json")
				.build();
			return sendAndParse(request, UserDto.class);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public UserDto updateUser(UUID id, UpdateUserRequest req) {
		try {
			HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id)
				.PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
				.header("Content-Type", "application/json")
				.build();
			return sendAndParse(request, UserDto.class);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteUser(UUID id) {
		HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id).DELETE().build();
		sendAndVerify(request);
	}

	public void resetPassword(UUID id, String newPassword) {
		try {
			HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id + "/reset-password")
				.PUT(HttpRequest.BodyPublishers
					.ofString(objectMapper.writeValueAsString(new ResetUserPasswordRequest(newPassword))))
				.header("Content-Type", "application/json")
				.build();
			sendAndVerify(request);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void assignRoles(UUID id, List<String> roles) {
		try {
			HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id + "/roles")
				.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
				.header("Content-Type", "application/json")
				.build();
			sendAndVerify(request);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void removeRoles(UUID id, List<String> roles) {
		try {
			HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/" + id + "/roles/remove")
				.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
				.header("Content-Type", "application/json")
				.build();
			sendAndVerify(request);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
