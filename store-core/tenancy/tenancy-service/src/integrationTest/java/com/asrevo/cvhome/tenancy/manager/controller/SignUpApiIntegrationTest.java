package com.asrevo.cvhome.tenancy.manager.controller;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.tenancy.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.expect;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.json;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.path;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Public signup over real HTTP — no token, which is the whole point of it.
 *
 * <p>
 * This is the only endpoint on the platform an anonymous caller may reach, and it creates a tenant, so the two
 * things worth proving here are that it is genuinely reachable without a session and that being reachable is not
 * the same as being open. Before {@code @Valid} it was: empty names, {@code not-an-email}, a one-character
 * password and a mismatched confirmation all answered 200 with an account created.
 * </p>
 *
 * <p>
 * The assertions are about the <em>field paths</em> as much as the statuses. The console binds a server error to
 * the control that caused it by that path, and a 400 whose {@code fieldErrors[]} name nothing is a toast the
 * visitor cannot act on.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class SignUpApiIntegrationTest {

    private static final String CREATE = path("/api/v1/signup", "public", "create");

    private static final String PASSWORD = "correct-horse-8";

    private static final String FIELD_ERRORS = "fieldErrors";

    private static final String FIELD = "field";

    private static final String FIRST_NAME = "Ada";

    private static final String LAST_NAME = "Lovelace";

    private static final String EMAIL = "ada@example.com";

    private static final String EMAIL_TEMPLATE = "%s@example.com";

    private static final String CREATED_USER_ID = "created-user-id";

    private static final String EMAIL_PATH = "user.emailAddress";

    private static final String PASSWORD_PATH = "user.password";

    /** Contains the family name typed two fields above it, which is the shape a length rule accepts. */
    private static final String PERSONAL_PASSWORD = "lovelace2026";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private UserAccountService userAccountService;

    private TenancyApiTestSupport api;

    private static String body(String firstName, String lastName, String email, String password, String repeat) {
        return String.format("""
                {"user":{"firstName":"%s","lastName":"%s","emailAddress":"%s",\
                "password":"%s","repeatPassword":"%s"}}""", firstName, lastName, email, password, repeat);
    }

    /** The paths the response blames, which is what the console binds its controls by. */
    private static List<String> blamedFields(ResponseEntity<String> response) {
        JsonNode problem = json(response);
        return problem.get(FIELD_ERRORS).valueStream().map(it -> it.get(FIELD).asString()).toList();
    }

    @BeforeEach
    void setUp() {
        api = new TenancyApiTestSupport(port, signer);
        Mockito.reset(userAccountService);
    }

    /** Anonymous: {@code null} where every other test in this package passes a token. */
    private ResponseEntity<String> signUp(String body) {
        return api.post(CREATE, null, body);
    }

    @Test
    void anAnonymousVisitorCanCreateAnOrganizationAndItsFirstAdministrator() throws Exception {
        ReadableUser created = new ReadableUser();
        created.setId(CREATED_USER_ID);
        when(userAccountService.createUser(any())).thenReturn(created);
        String email = String.format(EMAIL_TEMPLATE, slug("founder"));

        expect(signUp(body(FIRST_NAME, LAST_NAME, email, PASSWORD, PASSWORD)), HttpStatus.OK);

        ArgumentCaptor<PersistableUser> user = ArgumentCaptor.forClass(PersistableUser.class);
        verify(userAccountService).createUser(user.capture());
        assertThat(user.getValue().getRoles()).containsExactly("ORG_ADMIN");
        assertThat(user.getValue().getOrg()).isNotBlank();
        // The privileged field a public body used to be able to set, and the one the old overwrite forgot.
        assertThat(user.getValue().getStore()).isNull();
    }

    @Test
    void anAddressPostedInMixedCaseBecomesOneAccountAndNotTwo() throws Exception {
        ReadableUser created = new ReadableUser();
        created.setId(CREATED_USER_ID);
        when(userAccountService.createUser(any())).thenReturn(created);
        String email = String.format(EMAIL_TEMPLATE, slug("Founder"));

        expect(signUp(body(FIRST_NAME, LAST_NAME, email.toUpperCase(Locale.ROOT), PASSWORD, PASSWORD)),
                HttpStatus.OK);

        ArgumentCaptor<PersistableUser> user = ArgumentCaptor.forClass(PersistableUser.class);
        verify(userAccountService).createUser(user.capture());
        // uaa's unique constraint is case-sensitive, so the lowercasing is what stops ADA@ and ada@ from being two
        // accounts everybody involved believes to be one.
        assertThat(user.getValue().getUserName()).isEqualTo(email.toLowerCase(Locale.ROOT));
    }

    @Test
    void anEmptyBodyIsRefusedRatherThanCreatingATenant() {
        ResponseEntity<String> response = signUp("""
                {"user":{}}""");

        expect(response, HttpStatus.BAD_REQUEST);
        assertThat(blamedFields(response))
                .contains("user.firstName", "user.lastName", EMAIL_PATH, PASSWORD_PATH);
        verifyNothingWasCreated();
    }

    @Test
    void anAddressThatIsNotOneIsRefused() {
        ResponseEntity<String> response = signUp(body(FIRST_NAME, LAST_NAME, "not-an-email", PASSWORD, PASSWORD));

        expect(response, HttpStatus.BAD_REQUEST);
        assertThat(blamedFields(response)).containsExactly(EMAIL_PATH);
        verifyNothingWasCreated();
    }

    @Test
    void aMismatchedConfirmationIsRefusedAndBlamesTheConfirmation() {
        // Read by nothing before this: "a" and "b" were accepted and the account created with the first of them.
        ResponseEntity<String> response = signUp(body(FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, "other-pass-9"));

        expect(response, HttpStatus.BAD_REQUEST);
        assertThat(blamedFields(response)).containsExactly("user.repeatPassword");
        verifyNothingWasCreated();
    }

    @Test
    void aPasswordContainingTheNameAboveItIsRefusedAndBlamesThePassword() {
        ResponseEntity<String> response =
                signUp(body(FIRST_NAME, LAST_NAME, EMAIL, PERSONAL_PASSWORD, PERSONAL_PASSWORD));

        expect(response, HttpStatus.BAD_REQUEST);
        assertThat(blamedFields(response)).containsExactly(PASSWORD_PATH);
        verifyNothingWasCreated();
    }

    @Test
    void anAddressThatAlreadyHasAnAccountIsItsOwnConflict() throws Exception {
        when(userAccountService.createUser(any())).thenThrow(UaaConflictException.class);

        ResponseEntity<String> response = signUp(body(FIRST_NAME, LAST_NAME, "taken@example.com", PASSWORD, PASSWORD));

        expect(response, HttpStatus.CONFLICT);
        JsonNode problem = json(response);
        // The console used to have to guess that a fieldless 409 meant this, which was also the answer an
        // over-long address produced — so it told people to sign in to an account that did not exist.
        assertThat(problem.get("code").asString()).isEqualTo("CONTROL_PLANE.SIGNUP.EMAIL_TAKEN");
        assertThat(blamedFields(response)).containsExactly(EMAIL_PATH);
    }

    private void verifyNothingWasCreated() {
        try {
            verify(userAccountService, never()).createUser(any());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
