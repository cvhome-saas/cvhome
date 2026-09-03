package com.asrevo.cvhome.uaa.idp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A brokered login end to end against the stub provider: the redirect out, the code back, the token exchange uaa
 * makes with itself, and the session that results — for a new account, a returning one, one that must confirm with
 * its password, one the provider may not link, and a provider that is switched off.
 */
@DatabaseIntegrationTest
@Import(StubIdpConfiguration.class)
class BrokeredLoginIntegrationTest {

    private static final String IDPS = "/api/v1/admin/identity-providers";

    private static final String ME = "/api/v1/auth/me";

    private static final String START = "/oauth2/authorization/stub";

    private static final String LINK_CONFIRM = "/api/v1/auth/link-confirm";

    private static final String USERNAME = "username";

    private static final String LOCATION_LOGIN_ERROR = "/login?error=";

    private static final String LOGIN_PAGE = "/login";

    private static final String CALLBACK = "/login/oauth2/code/stub";

    private static final String ID = "id";

    private static final String ONE = "%s/%s";

    private static final String CONFIRM = "CONFIRM";

    private static final String GRACE_MAIL = "grace@stub.example";

    private static final String GRACE = "Grace";

    private static final String ADMIN = "Admin";

    private static final String PROVIDER_ALIAS = "providerAlias";

    private static final String STUB = "stub";

    private static final String DISCOVER = "/api/v1/public/idps/discover";

    private static final String PROVIDER = "provider";

    @LocalServerPort
    private int port;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    private String register(String linking, boolean jit) throws IOException, InterruptedException {
        String issuer = String.format("http://localhost:%d%s", port, StubIdp.PATH);
        String body = String.format("""
                {"alias": "stub", "preset": "GENERIC_OIDC", "displayName": "Stub", "clientId": "uaa-test",
                 "clientSecret": "secret", "issuerUri": "%1$s", "authorizationUri": "%1$s/authorize",
                 "tokenUri": "%1$s/token", "userInfoUri": "%1$s/userinfo", "jwkSetUri": "%1$s/jwks",
                 "scopes": ["openid", "email", "profile"], "emailDomains": ["stub.example"], "accountLinking": "%2$s",
                 "jitProvisioning": %3$s, "defaultRoles": ["USER"], "trustEmailVerified": true}""", issuer, linking, jit);
        HttpResponse<String> created = uaa.bearer(UaaClient.POST, IDPS, body, uaa.superAdminToken());
        assertThat(created.statusCode()).as(created.body()).isEqualTo(201);
        return UaaClient.body(created).get(ID).asText();
    }

    private void remove(String id) throws IOException, InterruptedException {
        uaa.bearer(UaaClient.DELETE, String.format(ONE, IDPS, id), null, uaa.superAdminToken());
    }

    /** Drives the browser's three hops and answers the last redirect's location. */
    private String loginThroughStub() throws IOException, InterruptedException {
        HttpResponse<String> start = uaa.session(UaaClient.GET, START, null);
        assertThat(start.statusCode()).as(start.body().substring(0, Math.min(400, start.body().length()))).isEqualTo(302);
        String toProvider = UaaClient.location(start);
        assertThat(toProvider).contains(String.format("%s/authorize", StubIdp.PATH)).contains("code_challenge").contains("nonce");

        HttpResponse<String> provider = uaa.send(java.net.http.HttpRequest.newBuilder(URI.create(toProvider)).GET().build());
        assertThat(provider.statusCode()).as("%s -> %s", toProvider,
                provider.body().substring(0, Math.min(300, provider.body().length()))).isEqualTo(302);
        String callback = UaaClient.location(provider);
        assertThat(callback).contains(CALLBACK);

        HttpResponse<String> back = uaa.send(java.net.http.HttpRequest.newBuilder(URI.create(callback)).GET().build());
        assertThat(back.statusCode()).as(back.body()).isEqualTo(302);
        return UaaClient.location(back);
    }

    @Test
    void aNewPersonIsProvisionedAndSignedIn() throws IOException, InterruptedException {
        String id = register(CONFIRM, true);
        try {
            StubIdp.willReturn("sub-new", GRACE_MAIL, true, GRACE, "Hopper");
            uaa.clearCookies();

            String landed = loginThroughStub();
            assertThat(landed).doesNotContain(LOCATION_LOGIN_ERROR);

            JsonNode me = UaaClient.body(uaa.session(UaaClient.GET, ME, null));
            assertThat(me.get(USERNAME).asText()).isEqualTo(GRACE_MAIL);
            assertThat(me.get("firstName").asText()).isEqualTo(GRACE);
            assertThat(me.get("roles").toString()).contains("USER");

            // Second time round: the identity is known, nothing is created twice.
            uaa.clearCookies();
            assertThat(loginThroughStub()).doesNotContain(LOCATION_LOGIN_ERROR);
            assertThat(UaaClient.body(uaa.session(UaaClient.GET, ME, null)).get(USERNAME).asText()).isEqualTo(GRACE_MAIL);
        } finally {
            remove(id);
        }
    }

    @Test
    void anExistingAccountConfirmsWithItsPasswordAndIsLinked() throws IOException, InterruptedException {
        String id = register(CONFIRM, false);
        try {
            StubIdp.willReturn("sub-org1", "org1-admin@mail.com", true, "Org1", ADMIN);
            uaa.clearCookies();

            assertThat(loginThroughStub()).endsWith(String.format("%slink_required", LOCATION_LOGIN_ERROR));
            // The page the browser lands on. It is also what plants the CSRF cookie the confirmation POST needs:
            // the brokered redirects never reach the filter that writes it.
            assertThat(uaa.session(UaaClient.GET, LOGIN_PAGE, null).statusCode()).isEqualTo(200);
            JsonNode context = UaaClient.body(uaa.session(UaaClient.GET, "/api/v1/public/login/context", null));
            assertThat(context.get("pendingLink").get(PROVIDER_ALIAS).asText()).isEqualTo(STUB);

            HttpResponse<String> wrong = uaa.session(UaaClient.POST, LINK_CONFIRM, "{\"password\": \"nope\"}");
            assertThat(wrong.statusCode()).as(wrong.body()).isEqualTo(400);

            HttpResponse<String> ok = uaa.session(UaaClient.POST, LINK_CONFIRM, "{\"password\": \"admin\"}");
            assertThat(ok.statusCode()).as(ok.body()).isEqualTo(200);
            assertThat(UaaClient.body(ok).get(USERNAME).asText()).isEqualTo(UaaClient.ORG1_ADMIN);
            assertThat(UaaClient.body(uaa.session(UaaClient.GET, ME, null)).get(USERNAME).asText()).isEqualTo(UaaClient.ORG1_ADMIN);

            // Linked now: the next brokered login needs no password.
            uaa.clearCookies();
            assertThat(loginThroughStub()).doesNotContain(LOCATION_LOGIN_ERROR);
            JsonNode identities = UaaClient.body(uaa.session(UaaClient.GET, "/api/v1/account/identities", null));
            assertThat(identities).hasSize(1);
            assertThat(identities.get(0).get(PROVIDER_ALIAS).asText()).isEqualTo(STUB);
        } finally {
            remove(id);
        }
    }

    @Test
    void rejectAndDisabledRefuse() throws IOException, InterruptedException {
        String id = register("REJECT", false);
        try {
            StubIdp.willReturn("sub-org2", "org2-admin@mail.com", true, "Org2", ADMIN);
            uaa.clearCookies();
            assertThat(loginThroughStub()).endsWith(String.format("%sidp_rejected", LOCATION_LOGIN_ERROR));

            uaa.bearer(UaaClient.POST, String.format("%s/%s/disable", IDPS, id), null, uaa.superAdminToken());
            uaa.clearCookies();
            /*
             * No registration at all: whatever the disabled alias answers, it is not a redirect to the provider.
             * (It is a refusal, not a page: nothing is registered at that path any more.)
             */
            HttpResponse<String> gone = uaa.session(UaaClient.GET, START, null);
            assertThat(gone.headers().firstValue("location").orElse("")).doesNotContain(StubIdp.PATH);
            assertThat(gone.body()).doesNotContain(StubIdp.PATH);
            assertThat(UaaClient.body(uaa.anonymous(UaaClient.GET, "/api/v1/public/idps")).toString()).doesNotContain(STUB);
        } finally {
            remove(id);
        }
    }

    @Test
    void discoveryAndTheGate() throws IOException, InterruptedException {
        String id = register(CONFIRM, true);
        try {
            HttpResponse<String> found = uaa.anonymous(UaaClient.POST, DISCOVER,
                    "{\"email\": \"someone@stub.example\"}");
            assertThat(UaaClient.body(found).get(PROVIDER).get("alias").asText()).isEqualTo(STUB);
            HttpResponse<String> none = uaa.anonymous(UaaClient.POST, DISCOVER, "{\"email\": \"x@other.org\"}");
            assertThat(UaaClient.body(none).get(PROVIDER).isNull()).isTrue();

            assertThat(uaa.bearer(UaaClient.GET, IDPS, null, uaa.storeCoreToken()).statusCode()).isEqualTo(403);
            HttpResponse<String> read = uaa.bearer(UaaClient.GET, String.format(ONE, IDPS, id), null, uaa.superAdminToken());
            assertThat(read.body()).doesNotContain("\"secret\"").contains("\"hasClientSecret\":true")
                    .contains(CALLBACK);
        } finally {
            remove(id);
        }
    }

}
