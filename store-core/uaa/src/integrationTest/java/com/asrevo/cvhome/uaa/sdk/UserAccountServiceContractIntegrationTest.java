package com.asrevo.cvhome.uaa.sdk;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaUserNotFoundException;
import com.asrevo.cvhome.uaa.domain.user.InviteUserRequest;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.service.UserAccountService;
import com.asrevo.cvhome.uaa.service.impl.UserAccountServiceImpl;
import com.asrevo.cvhome.uaa.support.UaaClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link UserAccountService} — the contract every cvhome service actually holds — against a real uaa.
 *
 * <p>
 * This layer exists to narrow vocabulary. {@code AdminUserClient} declares {@code UaaApiException} on every method,
 * because at the transport any endpoint can answer anything; each method here names what its own operation can mean
 * and folds the rest into {@link UaaApiUnavailableException}. That judgement is the thing worth testing, and it can
 * only be observed by making a real uaa answer a real failure — which is why tenancy's tests, where this interface
 * is mocked, cannot reach it.
 * </p>
 *
 * <p>
 * It also carries the {@code org} / {@code store} metadata convention. uaa stores those as free-form user metadata
 * and enforces nothing, so this mapping is the only thing keeping one organization's users distinguishable from
 * another's — a round trip that dropped them would make every tenancy guard downstream inoperable.
 * </p>
 */
@DatabaseIntegrationTest
class UserAccountServiceContractIntegrationTest {

    private static final String HTTP_LOCALHOST_D = "http://localhost:%d";
    private static final String ORG = "org";
    private static final String ADA = "Ada";
    private static final String LOVELACE = "Lovelace";
    private static final String S_00000000_0000_0000_0000_0000000000FE = "00000000-0000-0000-0000-0000000000fe";
    private static final String ORG_1 = "org-1";

    private static final String STORE_1 = "store-1";

    private static final String PASSWORD = "Str0ng-passw0rd!";

    private static final String REPLACEMENT_PASSWORD = "An0ther-passw0rd!";

    private UserAccountService accounts;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        accounts = new UserAccountServiceImpl(new AdminUserClient(
                String.format(HTTP_LOCALHOST_D, port), UaaClient.ADMIN_SDK, UaaClient.LCL_SECRET));
    }

    @Test
    void theOrgAndStoreSurviveAfullRoundTrip() throws Exception {
        ReadableUser created = accounts.createUser(user(newUsername("round-trip")));

        // uaa keeps these as free-form metadata and enforces neither; this mapping is what makes them mean anything.
        assertThat(created.getOrg()).isEqualTo(ORG_1);
        assertThat(created.getStore()).isEqualTo(STORE_1);
        assertThat(accounts.findOne(created.getId()).getOrg()).isEqualTo(ORG_1);

        accounts.deleteUser(created.getId());
    }

    @Test
    void auserIsUpdatedEnabledDisabledAndDeleted() throws Exception {
        ReadableUser created = accounts.createUser(user(newUsername("lifecycle")));

        PersistableUser update = user(created.getUserName());
        update.setUserName(created.getUserName());
        ReadableUser updated = accounts.updateUser(withId(update, created.getId()));
        assertThat(updated.getId()).isEqualTo(created.getId());

        accounts.disableUser(created.getId());
        assertThat(accounts.findOne(created.getId()).isActive()).isFalse();
        accounts.enableUser(created.getId());
        assertThat(accounts.findOne(created.getId()).isActive()).isTrue();

        accounts.deleteUser(created.getId());
        assertThatThrownBy(() -> accounts.findOne(created.getId()))
                .isInstanceOf(UaaUserNotFoundException.class);
    }

    @Test
    void thelistNarrowsByTheMetadataFilterItIsGiven() throws Exception {
        ReadableUser created = accounts.createUser(user(newUsername("filtered")));

        var mine = accounts.list(Map.of(ORG, ORG_1), 0, 50);
        var theirs = accounts.list(Map.of(ORG, newUsername("absent-org")), 0, 50);

        assertThat(mine.getContent()).extracting(ReadableUser::getId).contains(created.getId());
        assertThat(theirs.getContent()).isEmpty();

        accounts.deleteUser(created.getId());
    }

    @Test
    void thesearchAndCountsDecodeTheirOwnShapes() throws Exception {
        assertThat(accounts.search(com.asrevo.cvhome.uaa.domain.user.UserSearchFilters.none(), 0, 5)
                .getContent()).isNotEmpty();
        assertThat(accounts.counts().total()).isPositive();
        assertThat(accounts.getAssignableRoles()).isNotEmpty();
    }

    @Test
    void apasswordIsChangedThroughTheService() throws Exception {
        ReadableUser created = accounts.createUser(user(newUsername("password")));

        accounts.changePassword(created.getId(), new UserPassword(PASSWORD, REPLACEMENT_PASSWORD));

        accounts.deleteUser(created.getId());
    }

    /**
     * Setting the password the account already has is refused by uaa's password history — correctly. What the
     * caller is told is the problem: {@code changePassword} declares only "not found" and "unavailable", so a
     * policy refusal has nowhere to go and arrives as {@code UaaApiUnavailableException}. A console shows the
     * operator "uaa is unavailable" for a password uaa looked at and rejected.
     *
     * <p>
     * Pinned as the behaviour it currently has, not as the behaviour it should have: fixing it means adding a
     * refusal type to {@code UserAccountService.changePassword}, which changes a published contract and belongs in
     * its own change.
     * </p>
     */
    @Test
    void apolicyRefusalOnAchangeIsCurrentlyReportedAsUnavailable() throws Exception {
        ReadableUser created = accounts.createUser(user(newUsername("reuse")));

        assertThatThrownBy(() -> accounts.changePassword(created.getId(), new UserPassword(PASSWORD, PASSWORD)))
                .isInstanceOf(UaaApiUnavailableException.class);

        accounts.deleteUser(created.getId());
    }

    @Test
    void anInvitationAndAresetLinkAreBothIssued() throws Exception {
        String username = newUsername("invited");
        var invited = accounts.invite(new InviteUserRequest(username, username, ADA, LOVELACE,
                List.of(), Map.of(ORG, ORG_1)));

        assertThat(invited.link()).isNotBlank();

        var reset = accounts.createResetLink(invited.user().getId(), false);
        assertThat(reset.link()).isNotBlank();

        accounts.deleteUser(invited.user().getId());
    }

    @Test
    void amissingUserIsNotFoundRatherThanUnavailable() {
        // The narrowing this layer exists for: "no such user" is a fact, not an outage.
        assertThatThrownBy(() -> accounts.findOne(S_00000000_0000_0000_0000_0000000000FE))
                .isInstanceOf(UaaUserNotFoundException.class)
                .isNotInstanceOf(UaaApiUnavailableException.class);
    }

    @Test
    void auaaThatCannotBeReachedIsUnavailableRatherThanNotFound() {
        UserAccountService unreachable = new UserAccountServiceImpl(new AdminUserClient(
                String.format(HTTP_LOCALHOST_D, deadPort()), UaaClient.ADMIN_SDK, UaaClient.LCL_SECRET));

        // The other half of the same judgement: nothing was learned about the user, so callers must not treat it
        // as "no such user" and delete the local row.
        assertThatThrownBy(() -> unreachable.findOne(S_00000000_0000_0000_0000_0000000000FE))
                .isInstanceOf(UaaApiUnavailableException.class);
    }

    private static PersistableUser user(String username) {
        PersistableUser user = new PersistableUser();
        user.setUserName(username);
        user.setEmailAddress(username);
        user.setFirstName(ADA);
        user.setLastName(LOVELACE);
        user.setOrg(ORG_1);
        user.setStore(STORE_1);
        user.setActive(true);
        user.setRoles(Set.of());
        // createUser creates the account and then sets the password in a second call; without one the second call
        // is refused and the caller is told "unavailable" for an account that now exists.
        user.setPassword(PASSWORD);
        user.setRepeatPassword(PASSWORD);
        return user;
    }

    private static PersistableUser withId(PersistableUser user, String id) {
        user.setId(id);
        return user;
    }

    private static String newUsername(String prefix) {
        return String.format("svc-%s-%d@example.com", prefix, System.nanoTime());
    }

    private static int deadPort() {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
