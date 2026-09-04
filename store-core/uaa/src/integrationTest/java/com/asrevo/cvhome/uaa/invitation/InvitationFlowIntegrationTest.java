package com.asrevo.cvhome.uaa.invitation;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The whole invitation: issued once with a link, previewed and accepted anonymously, the account signs in, the
 * link is spent, and the outbox delivered the event.
 */
@DatabaseIntegrationTest
class InvitationFlowIntegrationTest {

    private static final String USERS = "/api/v1/admin/users";

    private static final String INVITATIONS = "/api/v1/admin/users/invitations";

    private static final String PUBLIC_INVITATION = "/api/v1/public/invitations/%s";

    private static final String PUBLIC_ACCEPT = "/api/v1/public/invitations/%s/accept";

    private static final String USER_PATH = "/api/v1/admin/users/%s";

    private static final String EMAIL = "invited@example.com";

    private static final String STRONG = "Welcome-Passw0rd-2026";

    private static final String PASSWORD_BODY = "{\"password\": \"%s\"}";

    private static final String TOKEN_PARAM = "token=";

    private static final String STATUS = "status";

    private static final String PENDING = "PENDING";

    private static final String USER = "user";

    private static final String USERNAME = "username";

    private static final String COMPLETED = "COMPLETED";

    private static final String OUTBOX_STATUS = """
            select status from uaa.outbox_record where record_type like '%InvitationIssuedEvent%' order by created_at desc limit 1""";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    @Test
    void invitePreviewAcceptSignInAndTheLinkIsSpent() throws Exception {
        String admin = uaa.superAdminToken();
        String body = String.format("""
                {"email": "%s", "firstName": "Invited", "roles": ["STORE_ADMIN"], "metadata": {"org": "org-x"}}""", EMAIL);
        HttpResponse<String> issued = uaa.bearer(UaaClient.POST, INVITATIONS, body, admin);
        assertThat(issued.statusCode()).as(issued.body()).isEqualTo(201);
        JsonNode link = UaaClient.body(issued);
        String userId = link.get(USER).get("id").asText();
        assertThat(link.get(USER).get(STATUS).asText()).isEqualTo(PENDING);
        assertThat(link.get("invitation").get(STATUS).asText()).isEqualTo(PENDING);
        String url = link.get("link").asText();
        /*
         * The console's route on the console's origin, not uaa's own page on the issuer. uaa still mints and
         * redeems the token; only the page that collects the password moved, and a link that still pointed here
         * would land a merchant on the identity server instead of their console. `/invitation` rather than
         * `/accept-invitation` because the console already has a route by that name for organization member
         * invitations, which are a different token system entirely.
         */
        assertThat(url).contains("/invitation?token=").doesNotContain("/accept-invitation");
        String token = url.substring(url.indexOf(TOKEN_PARAM) + TOKEN_PARAM.length());
        assertThat(jdbc.queryForObject("select token_hash from uaa.invitations where user_id = ?::uuid", String.class, userId))
                .isNotEqualTo(token);

        // Pending shows in the counts and the status filter.
        JsonNode counts = UaaClient.body(uaa.bearer(UaaClient.GET, String.format("%s/counts", USERS), null, admin));
        assertThat(counts.get("pending").asLong()).isGreaterThanOrEqualTo(1);
        JsonNode pending = UaaClient.body(uaa.bearer(UaaClient.GET, String.format("%s?status=PENDING", USERS), null, admin));
        assertThat(pending.get("content").findValuesAsText(USERNAME)).contains(EMAIL);

        // Anonymous preview: whose account, and what the password must satisfy.
        HttpResponse<String> preview = uaa.anonymous(UaaClient.GET, String.format(PUBLIC_INVITATION, token));
        assertThat(preview.statusCode()).isEqualTo(200);
        assertThat(UaaClient.body(preview).get("kind").asText()).isEqualTo("INVITATION");
        assertThat(UaaClient.body(preview).get("password").get("minLength").asInt()).isEqualTo(12);

        // A weak password is a 400 naming the rules, and the invitation stays usable.
        String accept = String.format(PUBLIC_ACCEPT, token);
        HttpResponse<String> weak = uaa.anonymous(UaaClient.POST, accept, String.format(PASSWORD_BODY, "short"));
        assertThat(weak.statusCode()).as(weak.body()).isEqualTo(400);
        assertThat(weak.body()).contains("UAA.PASSWORD.POLICY_VIOLATION");

        String strong = String.format(PASSWORD_BODY, STRONG);
        HttpResponse<String> accepted = uaa.anonymous(UaaClient.POST, accept, strong);
        assertThat(accepted.statusCode()).as(accepted.body()).isEqualTo(200);
        assertThat(UaaClient.body(accepted).get(USERNAME).asText()).isEqualTo(EMAIL);

        // Spent: the same 404 as never-existed.
        assertThat(uaa.anonymous(UaaClient.GET, String.format(PUBLIC_INVITATION, token)).statusCode()).isEqualTo(404);
        assertThat(uaa.anonymous(UaaClient.POST, accept, strong).statusCode()).isEqualTo(404);

        // The account is active, verified, and signs in.
        JsonNode user = UaaClient.body(uaa.bearer(UaaClient.GET, String.format(USER_PATH, userId), null, admin));
        assertThat(user.get(STATUS).asText()).isEqualTo("ACTIVE");
        assertThat(user.get("emailVerified").asBoolean()).isTrue();
        HttpResponse<String> login = uaa.login(EMAIL, STRONG);
        assertThat(UaaClient.location(login)).doesNotContain("error");
        assertThat(uaa.session(UaaClient.GET, UaaClient.ME, null).statusCode()).isEqualTo(200);

        // The outbox delivered the event to the logging consumer.
        assertThat(outboxStatus()).isEqualTo(COMPLETED);
        List<String> audited = jdbc.queryForList(
                "select event_type from uaa.audit_events where target_name = ? order by id", String.class, EMAIL);
        assertThat(audited).contains("user.created", "invitation.created", "invitation.accepted", "user.activated");

        // Resend on an activated account is refused; delete publishes and succeeds.
        assertThat(uaa.bearer(UaaClient.POST, String.format("%s/invitations/resend", String.format(USER_PATH, userId)), null, admin)
                .statusCode()).isEqualTo(422);
        assertThat(uaa.bearer(UaaClient.DELETE, String.format(USER_PATH, userId), null, admin).statusCode()).isEqualTo(200);
    }

    @Test
    void aSecondInviteForTheSameEmailIsAConflictNamingTheField() throws Exception {
        String admin = uaa.superAdminToken();
        String body = "{\"email\": \"org1-admin@mail.com\"}";

        HttpResponse<String> conflict = uaa.bearer(UaaClient.POST, INVITATIONS, body, admin);

        assertThat(conflict.statusCode()).isEqualTo(409);
        assertThat(conflict.body()).contains("UAA.USER.EMAIL_TAKEN").contains("\"field\"");
    }

    private String outboxStatus() throws InterruptedException {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(15));
        String status = null;
        while (Instant.now().isBefore(deadline)) {
            List<String> rows = jdbc.queryForList(OUTBOX_STATUS, String.class);
            status = rows.isEmpty() ? null : rows.getFirst();
            if (COMPLETED.equals(status)) {
                return status;
            }
            Thread.sleep(500);
        }
        return status;
    }

}
