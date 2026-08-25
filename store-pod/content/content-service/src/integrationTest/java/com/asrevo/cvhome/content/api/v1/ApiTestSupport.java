package com.asrevo.cvhome.content.api.v1;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

/**
 * The content API's URL and field conventions over the shared {@link ApiClient} / {@link Tokens}.
 */
final class ApiTestSupport {

    static final String PRIVATE = "/api/v1/private/content";

    static final String STOREFRONT = "/api/v1/storefront";

    static final String ID = "id";

    static final String CODE = "code";

    static final String STATUS = "status";

    static final String CONTENT = "content";

    static final String DESCRIPTION = "description";

    static final String TITLE = "title";

    static final String BODY = "body";

    static final String VERSION = "version";

    static final String EN = "en";

    static final String PUBLISHED = "PUBLISHED";

    static final String ROLE_STORE_ADMIN = Tokens.ROLE_STORE_ADMIN;

    static final String ROLE_STORE_MODERATOR = Tokens.ROLE_STORE_MODERATOR;

    private final ApiClient client;

    private final Tokens tokens;

    ApiTestSupport(int port, TestJwtSigner signer) {
        this.client = new ApiClient(port);
        this.tokens = new Tokens(signer);
    }

    static String scoped(String path, String store) {
        return ApiClient.scoped(path, store);
    }

    static String path(Object... segments) {
        return ApiClient.path(segments);
    }

    static String query(String path, String query) {
        return ApiClient.query(path, query);
    }

    static JsonNode json(ResponseEntity<String> r) {
        return ApiClient.json(r);
    }

    static String slug(String prefix) {
        return ApiClient.slug(prefix);
    }

    static void expect(ResponseEntity<String> r, HttpStatus status) {
        ApiClient.expect(r, status);
    }

    String token(String role, String store) {
        return tokens.staff(role, store);
    }

    ResponseEntity<String> get(String url, String token) {
        return client.get(url, token);
    }

    ResponseEntity<String> send(HttpMethod method, String url, String token, String body) {
        return client.send(method, url, token, body);
    }

    ResponseEntity<String> upload(String url, String token, String filename, byte[] bytes) {
        return client.upload(url, token, filename, bytes);
    }

}
