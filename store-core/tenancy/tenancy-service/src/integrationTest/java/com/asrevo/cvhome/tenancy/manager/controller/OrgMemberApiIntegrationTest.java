package com.asrevo.cvhome.tenancy.manager.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.tenancy.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.MEMBERS_STORE;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_MEMBERS;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_NEIGHBOUR;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.expect;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.json;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.path;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.slug;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.with;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Members and invitations, over real HTTP.
 *
 * <p>
 * Everything except {@code accept} takes its organization from the <em>identity</em>, never from a parameter, and
 * these tests are what says so: the neighbouring organization's administrator holds a perfectly valid token and
 * still cannot see, revoke or remove anything of this one's, because there is no query string to change.
 * </p>
 *
 * <p>
 * {@code accept} is the exception and is deliberately unguarded beyond authentication: the invitee is not yet a
 * member, so no org-scoped check could pass. The bearer token in the link is the authorization, which is why the
 * cases below prove a spent one stops working.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class OrgMemberApiIntegrationTest {

    private static final String BASE = "/api/v1/org-member";

    private static final String LIST = path(BASE, "list");

    private static final String INVITATIONS = path(BASE, "invitations");

    private static final String ACCEPT = path(INVITATIONS, "accept");

    private static final String RESEND = path(INVITATIONS, "resend");

    private static final String REVOKE = path(INVITATIONS, "revoke");

    private static final String EMAIL = "email";

    private static final String TOKEN = "token";

    private static final String INVITATION = "invitation";

    private static final String INVITATION_ID = "invitationId";

    private static final String USER_ID = "userId";

    private static final String REMOVED = "removed";

    private static final String ID = "id";

    private static final String DOMAIN = "@example.com";

    private static final String SEEDED_MEMBER = "member-one";

    private static final String STATUS = "status";

    /**
     * The uaa id an accepted invitation is recorded against: {@code accept} takes it from the authentication's name,
     * which for a staff token is the {@code sub} claim {@link com.asrevo.cvhome.testsupport.security.Tokens#staff}
     * mints — the lower-cased role, then the store.
     */
    private static final String STORE_ADMIN_SUBJECT = String.format("role_store_admin@%s", MEMBERS_STORE);

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private TenancyApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new TenancyApiTestSupport(port, signer);
    }

    private String admin() {
        return api.orgAdmin(ORG_MEMBERS);
    }

    private String neighbour() {
        return api.orgAdmin(ORG_NEIGHBOUR);
    }

    /** Invites a fresh address and answers the one-time token. */
    private String inviteSomeone(String token) {
        var response = api.post(with(INVITATIONS, EMAIL, address("invitee")), token, null);
        expect(response, HttpStatus.OK);
        return json(response).get(TOKEN).asString();
    }

    /** A fresh address on the fixture domain, unique per run so a live invitation never collides. */
    private static String address(String prefix) {
        return String.format("%s%s", slug(prefix), DOMAIN);
    }

    private static String userIdsOf(JsonNode members) {
        return members.valueStream().map(it -> it.get(USER_ID).asString()).toList().toString();
    }

    @Test
    void anAdminSeesTheirOwnOrganizationsMembers() {
        JsonNode members = json(api.get(LIST, admin()));

        assertThat(userIdsOf(members)).contains(SEEDED_MEMBER);
    }

    /** The org comes from the identity, so a neighbouring admin has no way to address this organization at all. */
    @Test
    void aNeighbouringAdminSeesNoneOfThisOrganizationsMembers() {
        JsonNode members = json(api.get(LIST, neighbour()));

        assertThat(userIdsOf(members)).doesNotContain(SEEDED_MEMBER);
    }

    @Test
    void aStoreAdminMayNotReadTheMemberList() {
        expect(api.get(LIST, api.storeAdmin(ORG_MEMBERS, MEMBERS_STORE)), HttpStatus.FORBIDDEN);
    }

    @Test
    void anUnauthenticatedCallerIsRefused() {
        expect(api.get(LIST, null), HttpStatus.UNAUTHORIZED);
    }

    /**
     * The token is returned once and only once: every later read answers the invitation without it, because anyone
     * who can list invitations would otherwise be able to accept them.
     */
    @Test
    void invitingReturnsTheTokenOnceAndNeverAgain() {
        String email = address("once");

        JsonNode created = json(api.post(with(INVITATIONS, EMAIL, email), admin(), null));

        assertThat(created.get(TOKEN).asString()).isNotBlank();
        assertThat(created.get(INVITATION).get(EMAIL).asString()).isEqualTo(email);
        JsonNode listed = json(api.get(INVITATIONS, admin()));
        assertThat(listed.toString()).contains(email).doesNotContain(created.get(TOKEN).asString());
    }

    @Test
    void aSecondLiveInvitationToTheSameAddressIsAConflict() {
        String email = address("dupe");

        expect(api.post(with(INVITATIONS, EMAIL, email), admin(), null), HttpStatus.OK);
        expect(api.post(with(INVITATIONS, EMAIL, email), admin(), null), HttpStatus.CONFLICT);
    }

    /** Addresses are normalised, so the same person in a different case is the same live invitation. */
    @Test
    void anAddressInADifferentCaseIsTheSameInvitation() {
        String email = String.format("%s@Example.com", slug("Case"));

        expect(api.post(with(INVITATIONS, EMAIL, email), admin(), null), HttpStatus.OK);
        expect(api.post(with(INVITATIONS, EMAIL, email.toUpperCase()), admin(), null), HttpStatus.CONFLICT);
    }

    /** "Resend" usually means the first link went astray, and a link that went astray should stop working. */
    @Test
    void resendingRotatesTheTokenAndInvalidatesTheOldOne() {
        String email = address("rotate");
        String first = json(api.post(with(INVITATIONS, EMAIL, email), admin(), null)).get(TOKEN).asString();

        String second = json(api.post(with(RESEND, EMAIL, email), admin(), null))
                .get(TOKEN).asString();

        assertThat(second).isNotEqualTo(first);
        expect(api.post(with(ACCEPT, TOKEN, first), admin(), null),
                HttpStatus.UNPROCESSABLE_CONTENT);
        expect(api.post(with(ACCEPT, TOKEN, second), admin(), null), HttpStatus.OK);
    }

    @Test
    void acceptingAnInvitationAddsTheCallerToTheOrganization() {
        String token = inviteSomeone(admin());

        JsonNode accepted = json(api.post(with(ACCEPT, TOKEN, token),
                api.storeAdmin(ORG_MEMBERS, MEMBERS_STORE), null));

        assertThat(accepted.get(STATUS).asString()).isEqualTo("ACCEPTED");
        assertThat(userIdsOf(json(api.get(LIST, admin())))).contains(STORE_ADMIN_SUBJECT);
    }

    /** One error for a token that never existed and one that is spent, so the endpoint cannot probe which. */
    @Test
    void anUnknownTokenIsRefusedTheSameWayAsASpentOne() {
        String token = inviteSomeone(admin());
        expect(api.post(with(ACCEPT, TOKEN, token), admin(), null), HttpStatus.OK);

        expect(api.post(with(ACCEPT, TOKEN, token), admin(), null),
                HttpStatus.UNPROCESSABLE_CONTENT);
        expect(api.post(with(ACCEPT, TOKEN, "never-existed"), admin(), null),
                HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void revokingAnInvitationTakesItOutOfUse() {
        String email = address("revoked");
        JsonNode created = json(api.post(with(INVITATIONS, EMAIL, email), admin(), null));
        String id = created.get(INVITATION).get(ID).asString();

        JsonNode revoked = json(api.post(with(REVOKE, INVITATION_ID, id), admin(), null));

        assertThat(revoked.get(STATUS).asString()).isEqualTo("REVOKED");
        expect(api.post(with(ACCEPT, TOKEN, created.get(TOKEN).asString()), admin(), null),
                HttpStatus.UNPROCESSABLE_CONTENT);
    }

    /**
     * Revoke looks the invitation up <em>within the caller's organization</em>, so a neighbouring admin holding the
     * id cannot revoke it — the id is not a capability.
     */
    @Test
    void aNeighbouringAdminCannotRevokeThisOrganizationsInvitation() {
        JsonNode created = json(api.post(with(INVITATIONS, EMAIL, address("foreign")), admin(), null));
        String id = created.get(INVITATION).get(ID).asString();

        expect(api.post(with(REVOKE, INVITATION_ID, id), neighbour(), null),
                HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void removingAMemberReportsWhetherThereWasOneToRemove() {
        String token = inviteSomeone(admin());
        String joiner = STORE_ADMIN_SUBJECT;
        expect(api.post(with(ACCEPT, TOKEN, token),
                api.storeAdmin(ORG_MEMBERS, MEMBERS_STORE), null), HttpStatus.OK);

        JsonNode removed = json(api.send(HttpMethod.DELETE, with(BASE, USER_ID, joiner), admin(), null));
        JsonNode again = json(api.send(HttpMethod.DELETE, with(BASE, USER_ID, joiner), admin(), null));

        assertThat(removed.get(REMOVED).asBoolean()).isTrue();
        assertThat(again.get(REMOVED).asBoolean()).isFalse();
    }

    /** A neighbouring admin removing this organization's member removes nothing — the org is not a parameter. */
    @Test
    void aNeighbouringAdminCannotRemoveThisOrganizationsMember() {
        JsonNode result = json(api.send(HttpMethod.DELETE, with(BASE, USER_ID, SEEDED_MEMBER), neighbour(), null));

        assertThat(result.get(REMOVED).asBoolean()).isFalse();
        assertThat(userIdsOf(json(api.get(LIST, admin())))).contains(SEEDED_MEMBER);
    }

}
