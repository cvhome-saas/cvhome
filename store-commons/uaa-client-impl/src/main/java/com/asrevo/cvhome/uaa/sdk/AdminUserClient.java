package com.asrevo.cvhome.uaa.sdk;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.asrevo.cvhome.uaa.sdk.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.PageRequest;
import com.asrevo.cvhome.uaa.sdk.dto.PageResponse;
import com.asrevo.cvhome.uaa.sdk.dto.ResetUserPasswordRequest;
import com.asrevo.cvhome.uaa.sdk.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.UserDto;

import tools.jackson.core.type.TypeReference;

public class AdminUserClient extends AbstractAdminClient {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    private static final String PATH_SEPARATOR = "/";

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
        HttpRequest request = authenticatedRequestBuilder(usersApiUrl + PATH_SEPARATOR + id).GET().build();
        return sendAndParse(request, UserDto.class);
    }

    public UserDto createUser(CreateUserRequest req) {
        HttpRequest request = authenticatedRequestBuilder(usersApiUrl)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        return sendAndParse(request, UserDto.class);
    }

    public UserDto updateUser(String id, UpdateUserRequest req) {
        HttpRequest request = authenticatedRequestBuilder(usersApiUrl + PATH_SEPARATOR + id)
                .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        return sendAndParse(request, UserDto.class);
    }

    public boolean usernameExist(String username) {
        String url = usersApiUrl + "/exists?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
        HttpRequest request = authenticatedRequestBuilder(url).GET().build();
        return sendAndParse(request, Boolean.class);
    }

    public void enableUser(String id) {
        HttpRequest request = authenticatedRequestBuilder(usersApiUrl + PATH_SEPARATOR + id + "/enable")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        sendAndVerify(request);
    }

    public void disableUser(String id) {
        HttpRequest request = authenticatedRequestBuilder(usersApiUrl + PATH_SEPARATOR + id + "/disable")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        sendAndVerify(request);
    }

    public void deleteUser(String id) {
        HttpRequest request = authenticatedRequestBuilder(usersApiUrl + PATH_SEPARATOR + id).DELETE().build();
        sendAndVerify(request);
    }

    public void resetPassword(String id, String newPassword) {
        HttpRequest request = authenticatedRequestBuilder(usersApiUrl + PATH_SEPARATOR + id + "/reset-password")
                .PUT(HttpRequest.BodyPublishers
                        .ofString(objectMapper.writeValueAsString(new ResetUserPasswordRequest(newPassword))))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        sendAndVerify(request);
    }

    public void assignRoles(String id, List<String> roles) {
        HttpRequest request = authenticatedRequestBuilder(usersApiUrl + PATH_SEPARATOR + id + "/roles")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        sendAndVerify(request);
    }

    public void removeRoles(String id, List<String> roles) {
        HttpRequest request = authenticatedRequestBuilder(usersApiUrl + PATH_SEPARATOR + id + "/roles/remove")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        sendAndVerify(request);
    }

    public Set<String> getAssignableRoles() {
        HttpRequest request = authenticatedRequestBuilder(usersApiUrl + "/assignable-roles").GET().build();
        return sendAndParse(request, new TypeReference<>() {
        });
    }

}
