package com.asrevo.cvhome.uaa.sdk;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.asrevo.cvhome.uaa.api.errors.UaaApiException;
import com.asrevo.cvhome.uaa.sdk.dto.CreateResetLinkRequest;
import com.asrevo.cvhome.uaa.sdk.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.InvitationResponse;
import com.asrevo.cvhome.uaa.sdk.dto.InviteUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.PageRequest;
import com.asrevo.cvhome.uaa.sdk.dto.PageResponse;
import com.asrevo.cvhome.uaa.sdk.dto.ResetUserPasswordRequest;
import com.asrevo.cvhome.uaa.sdk.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.UserCounts;
import com.asrevo.cvhome.uaa.sdk.dto.UserDto;
import com.asrevo.cvhome.uaa.sdk.dto.UserSearchFilters;

import tools.jackson.core.type.TypeReference;

public class AdminUserClient extends AbstractAdminClient {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    private static final String PATH_SEPARATOR = "/";

    private static final String PATH_WITH_ID_TEMPLATE = "%s%s%s";

    private static final String API_PATH = "%s/api/v1/admin/users";

    private final String usersApiUrl;

    public AdminUserClient(String baseUrl, String clientId, String clientSecret) {
        super(baseUrl, clientId, clientSecret);
        this.usersApiUrl = String.format(API_PATH, baseUrl);
    }

    /**
     * Overload taking the {@link java.net.http.HttpClient}, so a test can exercise the error paths without a uaa to
     * talk to.
     */
    public AdminUserClient(String baseUrl, String clientId, String clientSecret, java.net.http.HttpClient httpClient) {
        super(baseUrl, clientId, clientSecret, httpClient);
        this.usersApiUrl = String.format(API_PATH, baseUrl);
    }

    /**
     * A page of accounts filtered by metadata alone — kept because callers depend on it;
     * {@link #searchUsers(UserSearchFilters, PageRequest)} is the one to reach for otherwise.
     */
    public PageResponse<UserDto> listUsers(Map<String, String> metadataFilters, PageRequest pageRequest)
            throws UaaApiException {
        return searchUsers(UserSearchFilters.ofMetadata(metadataFilters), pageRequest);
    }

    public UserDto getUser(String id) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(
                String.format(PATH_WITH_ID_TEMPLATE, usersApiUrl, PATH_SEPARATOR, id)).GET().build();
        return sendAndParse(request, UserDto.class);
    }

    public UserDto createUser(CreateUserRequest req) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(usersApiUrl)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        return sendAndParse(request, UserDto.class);
    }

    public UserDto updateUser(String id, UpdateUserRequest req) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format(PATH_WITH_ID_TEMPLATE, usersApiUrl, PATH_SEPARATOR, id))
                .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        return sendAndParse(request, UserDto.class);
    }

    public boolean usernameExist(String username) throws UaaApiException {
        String url = String.format("%s/exists?username=%s", usersApiUrl, URLEncoder.encode(username, StandardCharsets.UTF_8));
        HttpRequest request = authenticatedRequestBuilder(url).GET().build();
        return sendAndParse(request, Boolean.class);
    }

    public void enableUser(String id) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s%s%s/enable", usersApiUrl, PATH_SEPARATOR, id))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        sendAndVerify(request);
    }

    public void disableUser(String id) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s%s%s/disable", usersApiUrl, PATH_SEPARATOR, id))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        sendAndVerify(request);
    }

    public void deleteUser(String id) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(
                String.format(PATH_WITH_ID_TEMPLATE, usersApiUrl, PATH_SEPARATOR, id)).DELETE().build();
        sendAndVerify(request);
    }

    public void resetPassword(String id, String newPassword) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s%s%s/reset-password", usersApiUrl, PATH_SEPARATOR, id))
                .PUT(HttpRequest.BodyPublishers
                        .ofString(objectMapper.writeValueAsString(new ResetUserPasswordRequest(newPassword))))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        sendAndVerify(request);
    }

    /**
     * A page of accounts, narrowed. Metadata filters go out as the bracketed {@code metadata[<key>]} parameters uaa
     * slices off the query string by hand; a blank value is dropped rather than sent, since it would match nothing.
     */
    public PageResponse<UserDto> searchUsers(UserSearchFilters filters, PageRequest pageRequest) throws UaaApiException {
        StringJoiner sj = new StringJoiner("&", "?", "");
        if (filters != null) {
            addIfPresent(sj, "q", filters.q());
            addIfPresent(sj, "status", filters.status());
            addIfPresent(sj, "role", filters.role());
            if (filters.metadata() != null) {
                for (Map.Entry<String, String> entry : filters.metadata().entrySet()) {
                    addIfPresent(sj, String.format("metadata[%s]", entry.getKey()), entry.getValue());
                }
            }
        }
        if (pageRequest != null) {
            sj.add(String.format("page=%d", pageRequest.page()));
            sj.add(String.format("size=%d", pageRequest.size()));
        }
        String url = sj.length() > 1 ? usersApiUrl + sj : usersApiUrl;
        HttpRequest request = authenticatedRequestBuilder(url).GET().build();
        return sendAndParsePage(request, new TypeReference<>() {
        });
    }

    /** How many accounts are in each state. */
    public UserCounts counts() throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s/counts", usersApiUrl)).GET().build();
        return sendAndParse(request, UserCounts.class);
    }

    /**
     * Invites someone: creates the account without a password and answers the one-time link once.
     *
     * <p>
     * uaa keeps only the token's hash, so the link in the response is the only readable copy that will ever exist.
     * Hand it to the person; never log it.
     * </p>
     */
    public InvitationResponse inviteUser(InviteUserRequest req) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s/invitations", usersApiUrl))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        return sendAndParse(request, InvitationResponse.class);
    }

    /**
     * A one-time password-reset link for an existing account, answered once.
     *
     * @param revokeSessions ends the account's sessions and tokens now rather than leaving them alive until the new
     *                       password is set — what an incident wants, and what a routine reset does not need
     */
    public InvitationResponse createResetLink(String id, boolean revokeSessions) throws UaaApiException {
        String url = String.format("%s%s%s/password-reset-links", usersApiUrl, PATH_SEPARATOR, id);
        HttpRequest request = authenticatedRequestBuilder(url)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(new CreateResetLinkRequest(revokeSessions))))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        return sendAndParse(request, InvitationResponse.class);
    }

    private static void addIfPresent(StringJoiner sj, String key, String value) {
        if (value != null && !value.isBlank()) {
            sj.add(String.format("%s=%s", URLEncoder.encode(key, StandardCharsets.UTF_8),
                    URLEncoder.encode(value, StandardCharsets.UTF_8)));
        }
    }

    public void assignRoles(String id, List<String> roles) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s%s%s/roles", usersApiUrl, PATH_SEPARATOR, id))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        sendAndVerify(request);
    }

    public void removeRoles(String id, List<String> roles) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s%s%s/roles/remove", usersApiUrl, PATH_SEPARATOR, id))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        sendAndVerify(request);
    }

    public Set<String> getAssignableRoles() throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s/assignable-roles", usersApiUrl)).GET().build();
        return sendAndParse(request, new TypeReference<>() {
        });
    }

}
