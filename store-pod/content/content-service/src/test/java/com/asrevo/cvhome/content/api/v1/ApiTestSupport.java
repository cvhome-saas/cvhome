package com.asrevo.cvhome.content.api.v1;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.s2s.config.ServletTestCustomSecurityConfig;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The HTTP plumbing the integration tests share: JWTs for a role on a store, a lenient RestClient, JSON parsing and
 * the URL conventions of the content API.
 */
final class ApiTestSupport {

    static final String PRIVATE = "/api/v1/private/content";

    static final String STOREFRONT = "/api/v1/storefront";

    static final String ORG = "32a034a43cd77581d105c87a";

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

    static final String ROLE_STORE_ADMIN = "ROLE_STORE_ADMIN";

    static final String ROLE_STORE_MODERATOR = "ROLE_STORE_MODERATOR";

    private static final String QUERY = "?";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final int port;

    private final ServletTestCustomSecurityConfig.JwtSigner signer;

    ApiTestSupport(int port, ServletTestCustomSecurityConfig.JwtSigner signer) {
        this.port = port;
        this.signer = signer;
    }

    String token(String role, String store) {
        try {
            return signer.createJwt(Map.of(
                    "sub", String.format("%s@%s", role.toLowerCase(), store),
                    "name", String.format("Test %s", role),
                    "roles", List.of(role),
                    "scope", "store_pod",
                    "org", ORG,
                    "store", store,
                    "exp", Instant.now().plusSeconds(3600).getEpochSecond()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    RestClient client() {
        return RestClient.builder().baseUrl(String.format("http://localhost:%d", port))
                .defaultStatusHandler(s -> true, (req, res) -> { })
                .build();
    }

    /**
     * Appends {@code store=&lang=en} to a path.
     */
    static String scoped(String path, String store) {
        String separator = path.contains(QUERY) ? "&" : QUERY;
        return String.format("%s%sstore=%s&lang=en", path, separator, store);
    }

    /**
     * Joins path segments with {@code /}: {@code path(PRIVATE, "pages", id, "publish")}.
     */
    static String path(Object... segments) {
        return java.util.Arrays.stream(segments).map(String::valueOf).collect(java.util.stream.Collectors.joining("/"));
    }

    /**
     * Appends a query string to a path.
     */
    static String query(String path, String query) {
        return String.format("%s%s%s", path, QUERY, query);
    }

    ResponseEntity<String> get(String url, String token) {
        var spec = client().get().uri(url);
        if (token != null) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, bearer(token));
        }
        return spec.retrieve().toEntity(String.class);
    }

    ResponseEntity<String> send(HttpMethod method, String url, String token, String body) {
        var spec = client().method(method).uri(url)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON);
        if (body != null) {
            spec = spec.body(body);
        }
        return spec.retrieve().toEntity(String.class);
    }

    ResponseEntity<String> upload(String url, String token, String filename, byte[] bytes) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("files", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        return client().post().uri(url).header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.MULTIPART_FORM_DATA).body(form).retrieve().toEntity(String.class);
    }

    static JsonNode json(ResponseEntity<String> r) {
        return JSON.readTree(r.getBody());
    }

    static String slug(String prefix) {
        return String.format("%s-%s", prefix, UUID.randomUUID().toString().substring(0, 8));
    }

    static void expect(ResponseEntity<String> r, HttpStatus status) {
        assertThat(r.getStatusCode()).as(r.getBody()).isEqualTo(status);
    }

    private static String bearer(String token) {
        return String.format("Bearer %s", token);
    }

}
