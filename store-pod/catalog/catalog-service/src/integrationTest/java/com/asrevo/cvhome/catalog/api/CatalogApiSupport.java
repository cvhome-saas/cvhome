package com.asrevo.cvhome.catalog.api;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

/**
 * The catalog API's URL and role conventions over the shared {@link ApiClient} / {@link Tokens}.
 *
 * <p>
 * Both stores are seeded by the {@code test-stores} profile ({@code init-sql/stores/&lt;id&gt;/*.sql}) with their own
 * types, brands, category tree, products and merchandising groups — which is what makes the isolation cases real
 * rather than a comparison of two empty stores.
 * </p>
 */
public final class CatalogApiSupport {

    /** The fashion store: categories, brands and 45 products of its own. */
    public static final String STORE_A = Tokens.STORE_1;

    /** Another seeded store, whose rows must never be reachable with a store-A token. */
    public static final String STORE_B = Tokens.STORE_2;

    public static final String ADMIN = Tokens.ROLE_STORE_ADMIN;

    /** Reads the console but manages nothing: catalog grants {@code STORE-POD.CATALOG.*} to admins only. */
    public static final String MODERATOR = Tokens.ROLE_STORE_MODERATOR;

    public static final String V1 = "/api/v1";

    public static final String V1_PRIVATE = "/api/v1/private";

    public static final String V2 = "/api/v2";

    public static final String V2_PRIVATE = "/api/v2/private";

    public static final String ID = "id";

    public static final String CODE = "code";

    public static final String CONTENT = "content";

    public static final String EXISTS = "exists";

    public static final String NAME = "name";

    public static final String SKU = "sku";

    public static final String DESCRIPTIONS = "descriptions";

    public static final String DESCRIPTION = "description";

    public static final String PRODUCTS = "products";

    public static final String CATEGORY = "category";

    public static final String TOTAL_ELEMENTS = "totalElements";

    private final ApiClient client;

    private final Tokens tokens;

    public CatalogApiSupport(int port, TestJwtSigner signer) {
        this.client = new ApiClient(port);
        this.tokens = new Tokens(signer);
    }

    public static String scoped(String path, String store) {
        return ApiClient.scoped(path, store);
    }

    public static String scoped(String path, String store, String language) {
        return ApiClient.scoped(path, store, language);
    }

    public static String path(Object... segments) {
        return ApiClient.path(segments);
    }

    public static String query(String path, String query) {
        return ApiClient.query(path, query);
    }

    public static JsonNode json(ResponseEntity<String> response) {
        return ApiClient.json(response);
    }

    public static String slug(String prefix) {
        return ApiClient.slug(prefix);
    }

    public static void expect(ResponseEntity<String> response, HttpStatus status) {
        ApiClient.expect(response, status);
    }

    public String token(String role, String store) {
        return tokens.staff(role, store);
    }

    public ResponseEntity<String> get(String url, String token) {
        return client.get(url, token);
    }

    public ResponseEntity<String> send(HttpMethod method, String url, String token, String body) {
        return client.send(method, url, token, body);
    }

    public ResponseEntity<String> upload(String url, String token, String filename, byte[] bytes) {
        return client.upload(url, token, filename, bytes);
    }

}
