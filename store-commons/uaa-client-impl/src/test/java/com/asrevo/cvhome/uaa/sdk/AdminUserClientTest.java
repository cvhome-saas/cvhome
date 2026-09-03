package com.asrevo.cvhome.uaa.sdk;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import com.asrevo.cvhome.uaa.sdk.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.InviteUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.PageRequest;
import com.asrevo.cvhome.uaa.sdk.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.UserSearchFilters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The request each call actually puts on the wire.
 *
 * <p>
 * This is where the SDK's real risk lives: a query string assembled by hand. A filter that is spelled wrong, a
 * page parameter uaa does not read, or a metadata key that loses its brackets does not fail — it answers 200 with
 * the wrong rows, and the caller acts on them. So every assertion here is about the URI, the method and the body,
 * against a transport that answers whatever the test says.
 * </p>
 */
class AdminUserClientTest {

    private static final String BASE_URL = "http://uaa:9999";

    private static final String USERS = String.format("%s/api/v1/admin/users", BASE_URL);

    private static final String ID = "11111111-2222-3333-4444-555555555555";

    private static final String ONE = String.format("%s/%s", USERS, ID);

    private static final String EMPTY_BODY = "{}";

    private static final String USERNAME = "ada";

    private static final String EMAIL = "ada@example.com";

    private static final String FIRST_NAME = "Ada";

    private static final String LAST_NAME = "Lovelace";

    private static final String ROLE = "STORE_ADMIN";

    private static final String ORG_KEY = "org";

    private static final String ORG = "o1";

    private static final String POST = "POST";

    private static final String PUT = "PUT";

    private static final String USER_JSON = """
            {"id": "11111111-2222-3333-4444-555555555555", "username": "ada", "email": "ada@example.com",
             "firstName": "Ada", "lastName": "Lovelace", "enabled": true, "status": "ACTIVE", "emailVerified": true,
             "roles": ["STORE_ADMIN"], "metadata": {}, "lastSignInAt": "2026-09-01T10:15:30Z"}""";

    private static final String PAGE_JSON = String.format("""
            {"content": [%s], "number": 0, "size": 20, "totalElements": 1, "totalPages": 1, "last": true,
             "first": true, "empty": false}""", USER_JSON);

    private static final String TOKEN_JSON = """
            {"access_token": "token", "token_type": "Bearer", "expires_in": 900, "scope": "super_admin"}""";

    private final List<HttpRequest> sent = new ArrayList<>();

    private AdminUserClient client;

    private String nextBody = EMPTY_BODY;

    @BeforeEach
    void setUp() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        Answer<HttpResponse<String>> answer = invocation -> {
            HttpRequest request = invocation.getArgument(0);
            if (request.uri().toString().endsWith("/oauth2/token")) {
                return response(200, TOKEN_JSON);
            }
            sent.add(request);
            return response(200, nextBody);
        };
        when(httpClient.send(any(HttpRequest.class), any())).thenAnswer(answer);
        client = new AdminUserClient(BASE_URL, "admin-sdk", "s3cr3t", httpClient);
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> response(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        when(response.uri()).thenReturn(URI.create(USERS));
        return response;
    }

    private HttpRequest lastRequest() {
        return sent.get(sent.size() - 1);
    }

    private String lastUri() {
        return lastRequest().uri().toString();
    }

    @Test
    void everyCallCarriesTheClientCredentialsToken() throws Exception {
        nextBody = USER_JSON;

        client.getUser(ID);

        assertThat(lastRequest().headers().firstValue("Authorization")).contains("Bearer token");
        assertThat(lastUri()).isEqualTo(ONE);
        assertThat(lastRequest().method()).isEqualTo("GET");
    }

    @Test
    void searchSendsOnlyTheFiltersThatAreSet() throws Exception {
        nextBody = PAGE_JSON;

        client.searchUsers(new UserSearchFilters(USERNAME, "PENDING", ROLE, Map.of(ORG_KEY, ORG)),
                new PageRequest(2, 25));

        assertThat(lastUri())
                .contains(String.format("q=%s", USERNAME))
                .contains("status=PENDING")
                .contains(String.format("role=%s", ROLE))
                .contains("page=2")
                .contains("size=25");
        // Bracketed, and encoded — uaa slices these off the raw query string by hand.
        assertThat(lastUri()).contains("metadata%5Borg%5D=o1");
    }

    @Test
    void aBlankFilterIsDroppedRatherThanSentEmpty() throws Exception {
        nextBody = PAGE_JSON;

        client.searchUsers(new UserSearchFilters("  ", null, null, Map.of(ORG_KEY, "")), new PageRequest(0, 20));

        assertThat(lastUri()).doesNotContain("q=").doesNotContain("metadata");
    }

    @Test
    void listUsersIsTheSameCallWithMetadataOnly() throws Exception {
        nextBody = PAGE_JSON;

        var page = client.listUsers(Map.of("store", "s1"), new PageRequest(0, 20));

        assertThat(lastUri()).contains("metadata%5Bstore%5D=s1").contains("page=0").contains("size=20");
        assertThat(page.content()).singleElement().satisfies(user -> {
            assertThat(user.username()).isEqualTo(USERNAME);
            assertThat(user.status()).isEqualTo("ACTIVE");
            assertThat(user.emailVerified()).isTrue();
            assertThat(user.lastSignInAt()).isNotNull();
        });
    }

    @Test
    void countsReadsItsOwnSubPath() throws Exception {
        nextBody = """
                {"total": 9, "active": 5, "pending": 2, "locked": 1, "disabled": 1}""";

        var counts = client.counts();

        assertThat(lastUri()).isEqualTo(String.format("%s/counts", USERS));
        assertThat(counts.total()).isEqualTo(9);
        assertThat(counts.pending()).isEqualTo(2);
    }

    @Test
    void invitePostsTheRequestAndReadsTheLinkBack() throws Exception {
        nextBody = String.format("""
                {"user": %s, "invitation": {"id": "%s"}, "link": "https://uaa/accept-invitation?token=opaque",
                 "expiresAt": "2026-09-08T10:15:30Z"}""", USER_JSON, UUID.randomUUID());

        var response = client.inviteUser(new InviteUserRequest(EMAIL, USERNAME, FIRST_NAME, LAST_NAME,
                List.of(ROLE), Map.of(ORG_KEY, ORG)));

        assertThat(lastUri()).isEqualTo(String.format("%s/invitations", USERS));
        assertThat(lastRequest().method()).isEqualTo(POST);
        assertThat(lastRequest().headers().firstValue("Content-Type")).contains("application/json");
        assertThat(response.link()).contains("token=opaque");
        assertThat(response.issuedLink().expiresAt()).isNotNull();
        assertThat(response.user().username()).isEqualTo(USERNAME);
    }

    @Test
    void theResetLinkAsksToRevokeSessionsInTheBody() throws Exception {
        nextBody = """
                {"user": null, "invitation": null, "link": "https://uaa/reset-password?token=opaque",
                 "expiresAt": "2026-09-01T11:15:30Z"}""";

        var response = client.createResetLink(ID, true);

        assertThat(lastUri()).isEqualTo(String.format("%s/password-reset-links", ONE));
        assertThat(lastRequest().method()).isEqualTo(POST);
        assertThat(response.link()).contains("reset-password");
    }

    @Test
    void theMutationsHitThePathsTheirNamesPromise() throws Exception {
        nextBody = USER_JSON;
        client.createUser(new CreateUserRequest(USERNAME, EMAIL, FIRST_NAME, LAST_NAME, Set.of(), Map.of()));
        assertThat(lastUri()).isEqualTo(USERS);
        assertThat(lastRequest().method()).isEqualTo(POST);

        client.updateUser(ID, new UpdateUserRequest(FIRST_NAME, LAST_NAME, true, Set.of(), Map.of()));
        assertThat(lastUri()).isEqualTo(ONE);
        assertThat(lastRequest().method()).isEqualTo(PUT);

        nextBody = EMPTY_BODY;
        client.enableUser(ID);
        assertThat(lastUri()).isEqualTo(String.format("%s/enable", ONE));

        client.disableUser(ID);
        assertThat(lastUri()).isEqualTo(String.format("%s/disable", ONE));

        client.deleteUser(ID);
        assertThat(lastUri()).isEqualTo(ONE);
        assertThat(lastRequest().method()).isEqualTo("DELETE");

        client.resetPassword(ID, "n3wp4ss");
        assertThat(lastUri()).isEqualTo(String.format("%s/reset-password", ONE));
        assertThat(lastRequest().method()).isEqualTo(PUT);

        client.assignRoles(ID, List.of(ROLE));
        assertThat(lastUri()).isEqualTo(String.format("%s/roles", ONE));

        client.removeRoles(ID, List.of(ROLE));
        assertThat(lastUri()).isEqualTo(String.format("%s/roles/remove", ONE));
    }

    @Test
    void theUsernameCheckEncodesWhatItIsAsking() throws Exception {
        nextBody = "true";

        assertThat(client.usernameExist("ada lovelace@example.com")).isTrue();
        assertThat(lastUri()).contains("/exists?username=ada+lovelace%40example.com");
    }

    @Test
    void assignableRolesComeBackAsASet() throws Exception {
        nextBody = """
                ["STORE_ADMIN", "STORE_MODERATOR"]""";

        assertThat(client.getAssignableRoles()).containsExactlyInAnyOrder(ROLE, "STORE_MODERATOR");
        assertThat(lastUri()).isEqualTo(String.format("%s/assignable-roles", USERS));
    }

}
