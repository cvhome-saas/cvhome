package com.asrevo.cvhome.cua.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.testsupport.http.ApiClient.expect;
import static com.asrevo.cvhome.testsupport.http.ApiClient.json;
import static com.asrevo.cvhome.testsupport.http.ApiClient.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The storefront's registration call, through real HTTP: an empty 201, the two typed conflicts, validation as
 * field errors, and — the case that matters most on a shared pod — that a username is unique per store only.
 */
@DatabaseIntegrationTest
class PublicRegistrationControllerIntegrationTest {

    private static final String PATH = "/api/v1/public/registration";

    /** Two stores of the {@code test-stores} seed, each with a shopper {@code user} / {@code user@mail.com}. */
    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String OTHER_STORE = "65f023632bc46470c104b75f";

    private static final String CODE = "code";

    @LocalServerPort
    private int port;

    private ApiClient api;

    private static String body(String username, String email) {
        return String.format("""
                {"username": "%s", "email": "%s", "password": "secret-1", "firstName": "Jane", "lastName": "Doe"}""",
                username, email);
    }

    /** A fresh username and a matching fresh email, so a case never collides with an earlier run. */
    private static String freshBody(String prefix, String domain) {
        String username = ApiClient.slug(prefix);
        return body(username, String.format("%s@%s", username, domain));
    }

    private ResponseEntity<String> register(String store, String body) {
        return api.send(HttpMethod.POST, scoped(PATH, store), null, body);
    }

    @BeforeEach
    void client() {
        api = new ApiClient(port);
    }

    @Test
    void aNewShopperIsCreatedWithAnEmptyBody() {
        ResponseEntity<String> response = register(STORE, freshBody("jane", "example.com"));

        expect(response, HttpStatus.CREATED);
        assertThat(response.getBody()).isNullOrEmpty();
    }

    @Test
    void theSeededUsernameIsAUsernameConflict() {
        ResponseEntity<String> response = register(STORE, body("user", "free@example.com"));

        expect(response, HttpStatus.CONFLICT);
        assertThat(json(response).path(CODE).asString()).isEqualTo("CUA.REGISTRATION.USERNAME_TAKEN");
    }

    @Test
    void theSeededEmailIsAnEmailConflict() {
        ResponseEntity<String> response = register(STORE, body(ApiClient.slug("free"), "user@mail.com"));

        expect(response, HttpStatus.CONFLICT);
        assertThat(json(response).path(CODE).asString()).isEqualTo("CUA.REGISTRATION.EMAIL_TAKEN");
    }

    @Test
    void aMalformedBodyNamesTheFieldsThatFailed() {
        ResponseEntity<String> response = register(STORE, """
                {"username": "jo", "email": "not-an-email", "password": "123"}""");

        expect(response, HttpStatus.BAD_REQUEST);
        JsonNode problem = json(response);
        assertThat(problem.path(CODE).asString()).isEqualTo("COMMON.VALIDATION_FAILED");
        assertThat(problem.path("fieldErrors").findValuesAsString("field"))
                .containsExactlyInAnyOrder("username", "email", "password");
    }

    /** Uniqueness is scoped to the store: a name taken on one store is free on another. */
    @Test
    void aUsernameTakenOnOneStoreIsFreeOnAnother() {
        String username = ApiClient.slug("shared");
        expect(register(STORE, body(username, String.format("%s@a.example.com", username))), HttpStatus.CREATED);

        expect(register(OTHER_STORE, body(username, String.format("%s@b.example.com", username))), HttpStatus.CREATED);
        expect(register(STORE, body(username, String.format("%s@c.example.com", username))), HttpStatus.CONFLICT);
    }

    @Test
    void withoutAStoreNothingIsRegistered() {
        ResponseEntity<String> response = api.send(HttpMethod.POST, PATH, null, freshBody("nowhere", "example.org"));

        assertThat(response.getStatusCode().is4xxClientError()).as(response.getBody()).isTrue();
    }

}
