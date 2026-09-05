package com.asrevo.cvhome.uaa.sdk;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.api.errors.UaaApiException;
import com.asrevo.cvhome.uaa.api.errors.UaaUserNotFoundException;
import com.asrevo.cvhome.uaa.sdk.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.PageRequest;
import com.asrevo.cvhome.uaa.sdk.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.UserDto;
import com.asrevo.cvhome.uaa.sdk.dto.UserSearchFilters;
import com.asrevo.cvhome.uaa.support.UaaClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The admin SDK against the uaa it exists to talk to.
 *
 * <p>
 * Nothing else in the repo verifies these two against each other. The SDK is a plain {@code java.net.http} client
 * with its own URL building, its own Jackson mapping and its own translation of uaa's problem body into typed
 * exceptions; tenancy is its only production consumer and tenancy's tests mock it out entirely. So every one of
 * those three layers could drift from the server and no test would notice until a console operation failed.
 * </p>
 *
 * <p>
 * The error translation is the part worth the most. It used to collapse every failure into
 * {@code new ApiException("API call failed with status 404: {…}")} — the whole response reduced to a string, so a
 * caller could neither branch on the code nor tell a refusal from an outage. It now decodes the body against
 * {@code UaaApiErrors.CATALOG}, and that resolution can only be exercised against a real uaa answering a real
 * problem body.
 * </p>
 */
@DatabaseIntegrationTest
class AdminSdkContractIntegrationTest {

    private static final String HTTP_LOCALHOST_D = "http://localhost:%d";
    private static final String GRACE = "Grace";
    private static final String ABSENT_D = "absent-%d";
    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    private static final String ORG = "org";

    private static final String ORG_1 = "org-1";

    @LocalServerPort
    private int port;

    private AdminUserClient users;

    private AdminClientClient clients;

    @BeforeEach
    void setUp() {
        String baseUrl = String.format(HTTP_LOCALHOST_D, port);
        users = new AdminUserClient(baseUrl, UaaClient.ADMIN_SDK, UaaClient.LCL_SECRET);
        clients = new AdminClientClient(baseUrl, UaaClient.ADMIN_SDK, UaaClient.LCL_SECRET);
    }

    @Test
    void theSdkObtainsItsOwnTokenAndListsUsers() throws Exception {
        var page = users.listUsers(Map.of(), PageRequest.of(0, 20));

        // Client-credentials, obtained by the SDK's own token manager rather than handed in by the test.
        assertThat(page.content()).isNotEmpty();
        assertThat(page.totalElements()).isPositive();
    }

    @Test
    void auserIsCreatedReadBackUpdatedAndDeletedThroughTheSdk() throws Exception {
        String username = String.format("sdk-%d@example.com", System.nanoTime());
        UserDto created = users.createUser(CreateUserRequest.builder()
                .username(username)
                .email(username)
                .firstName("Ada")
                .lastName("Lovelace")
                .roles(Set.of())
                .metadata(Map.of(ORG, ORG_1))
                .build());

        assertThat(created.username()).isEqualTo(username);
        assertThat(created.metadata()).containsEntry(ORG, ORG_1);
        assertThat(users.getUser(created.id().toString()).email()).isEqualTo(username);

        UserDto updated = users.updateUser(created.id().toString(),
                UpdateUserRequest.builder().firstName(GRACE).build());
        assertThat(updated.firstName()).isEqualTo(GRACE);

        users.deleteUser(created.id().toString());
        assertThatThrownBy(() -> users.getUser(created.id().toString()))
                .isInstanceOf(UaaUserNotFoundException.class);
    }

    /**
     * The whole reason the SDK decodes the problem body: a caller has to be able to branch on <em>which</em>
     * failure it was, and a user that is not there is a different thing from uaa being unreachable.
     */
    @Test
    void amissingUserArrivesAsItsOwnTypeRatherThanAstringifiedStatus() {
        assertThatThrownBy(() -> users.getUser("00000000-0000-0000-0000-0000000000ff"))
                .isInstanceOf(UaaUserNotFoundException.class)
                .isInstanceOf(UaaApiException.class);
    }

    @Test
    void enableDisableAndTheExistenceCheckAllReachTheServer() throws Exception {
        String username = String.format("sdk-toggle-%d@example.com", System.nanoTime());
        UserDto created = users.createUser(CreateUserRequest.builder()
                .username(username).email(username).roles(Set.of()).metadata(Map.of()).build());

        assertThat(users.usernameExist(username)).isTrue();
        assertThat(users.usernameExist(String.format(ABSENT_D, System.nanoTime()))).isFalse();

        users.disableUser(created.id().toString());
        assertThat(users.getUser(created.id().toString()).enabled()).isFalse();
        users.enableUser(created.id().toString());
        assertThat(users.getUser(created.id().toString()).enabled()).isTrue();

        users.deleteUser(created.id().toString());
    }

    @Test
    void rolesAreAssignedAndRemovedThroughTheSdk() throws Exception {
        String username = String.format("sdk-roles-%d@example.com", System.nanoTime());
        UserDto created = users.createUser(CreateUserRequest.builder()
                .username(username).email(username).roles(Set.of()).metadata(Map.of()).build());
        Set<String> assignable = users.getAssignableRoles();
        assertThat(assignable).isNotEmpty().doesNotContain(SUPER_ADMIN_ROLE);
        String role = assignable.iterator().next();

        users.assignRoles(created.id().toString(), List.of(role));
        assertThat(users.getUser(created.id().toString()).roles()).contains(role);

        users.removeRoles(created.id().toString(), List.of(role));
        assertThat(users.getUser(created.id().toString()).roles()).doesNotContain(role);

        users.deleteUser(created.id().toString());
    }

    @Test
    void thesearchAndCountEndpointsDecodeTheirOwnShapes() throws Exception {
        assertThat(users.searchUsers(UserSearchFilters.none(), PageRequest.of(0, 5)).content()).isNotEmpty();
        assertThat(users.counts().total()).isPositive();
    }

    @Test
    void themetadataFilterNarrowsToTheOrganizationItNames() throws Exception {
        String username = String.format("sdk-meta-%d@example.com", System.nanoTime());
        UserDto created = users.createUser(CreateUserRequest.builder()
                .username(username).email(username).roles(Set.of()).metadata(Map.of(ORG, ORG_1)).build());

        var mine = users.listUsers(Map.of(ORG, ORG_1), PageRequest.of(0, 50));
        var theirs = users.listUsers(Map.of(ORG, String.format(ABSENT_D, System.nanoTime())),
                PageRequest.of(0, 50));

        assertThat(mine.content()).extracting(UserDto::username).contains(username);
        assertThat(theirs.content()).isEmpty();

        users.deleteUser(created.id().toString());
    }

    @Test
    void theclientRegistryIsListedAndReadThroughTheSdk() throws Exception {
        var page = clients.listClients(PageRequest.of(0, 20));

        assertThat(page.content()).isNotEmpty();
        String id = page.content().getFirst().id();
        assertThat(clients.getClient(id).clientId()).isNotBlank();
        assertThat(clients.getOptions()).isNotEmpty();
    }

    @Test
    void wrongClientCredentialsFailAsAnSdkErrorRatherThanAnullToken() {
        AdminUserClient wrong = new AdminUserClient(String.format(HTTP_LOCALHOST_D, port),
                UaaClient.ADMIN_SDK, "not-the-secret");

        assertThatThrownBy(() -> wrong.counts()).isInstanceOf(UaaApiException.class);
    }

}
