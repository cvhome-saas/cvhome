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

    private static final String PATH_WITH_ID_TEMPLATE = "%s%s%s";

    private final String usersApiUrl;

    public AdminUserClient(String baseUrl, String clientId, String clientSecret) {
        super(baseUrl, clientId, clientSecret);
        this.usersApiUrl = String.format("%s/api/v1/admin/users", baseUrl);
    }

    public PageResponse<UserDto> listUsers(Map<String, String> metadataFilters, PageRequest pageRequest) {
        String url = usersApiUrl;
        StringJoiner sj = new StringJoiner("&", "?", "");
        if (metadataFilters != null && !metadataFilters.isEmpty()) {
            for (Map.Entry<String, String> entry : metadataFilters.entrySet()) {
                String key = URLEncoder.encode(String.format("metadata[%s]", entry.getKey()), StandardCharsets.UTF_8);
                String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                sj.add(String.format("%s=%s", key, value));
            }
        }

        if (pageRequest != null) {
            sj.add(String.format("page=%d", pageRequest.page()));
            sj.add(String.format("size=%d", pageRequest.size()));
        }

        if (sj.length() > 1) {
            url += sj.toString();
        }

        HttpRequest request = authenticatedRequestBuilder(url).GET().build();

        return sendAndParsePage(request, new TypeReference<>() {
        });
    }

    public UserDto getUser(String id) {
        HttpRequest request = authenticatedRequestBuilder(
                String.format(PATH_WITH_ID_TEMPLATE, usersApiUrl, PATH_SEPARATOR, id)).GET().build();
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
        HttpRequest request = authenticatedRequestBuilder(String.format(PATH_WITH_ID_TEMPLATE, usersApiUrl, PATH_SEPARATOR, id))
                .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        return sendAndParse(request, UserDto.class);
    }

    public boolean usernameExist(String username) {
        String url = String.format("%s/exists?username=%s", usersApiUrl, URLEncoder.encode(username, StandardCharsets.UTF_8));
        HttpRequest request = authenticatedRequestBuilder(url).GET().build();
        return sendAndParse(request, Boolean.class);
    }

    public void enableUser(String id) {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s%s%s/enable", usersApiUrl, PATH_SEPARATOR, id))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        sendAndVerify(request);
    }

    public void disableUser(String id) {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s%s%s/disable", usersApiUrl, PATH_SEPARATOR, id))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        sendAndVerify(request);
    }

    public void deleteUser(String id) {
        HttpRequest request = authenticatedRequestBuilder(
                String.format(PATH_WITH_ID_TEMPLATE, usersApiUrl, PATH_SEPARATOR, id)).DELETE().build();
        sendAndVerify(request);
    }

    public void resetPassword(String id, String newPassword) {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s%s%s/reset-password", usersApiUrl, PATH_SEPARATOR, id))
                .PUT(HttpRequest.BodyPublishers
                        .ofString(objectMapper.writeValueAsString(new ResetUserPasswordRequest(newPassword))))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        sendAndVerify(request);
    }

    public void assignRoles(String id, List<String> roles) {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s%s%s/roles", usersApiUrl, PATH_SEPARATOR, id))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        sendAndVerify(request);
    }

    public void removeRoles(String id, List<String> roles) {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s%s%s/roles/remove", usersApiUrl, PATH_SEPARATOR, id))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        sendAndVerify(request);
    }

    public Set<String> getAssignableRoles() {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s/assignable-roles", usersApiUrl)).GET().build();
        return sendAndParse(request, new TypeReference<>() {
        });
    }

}
