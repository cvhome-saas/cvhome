package com.asrevo.cvhome.testsupport.http;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real HTTP against a {@code RANDOM_PORT} context: never throws on 4xx/5xx, so a test asserts the status it expects.
 * Every store-scoped call goes through {@link #scoped(String, String)} — the same {@code ?store=&lang=} contract a
 * real client sends.
 */
public final class ApiClient {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final String QUERY = "?";

    private static final String AMP = "&";

    private final RestClient client;

    public ApiClient(int port) {
        this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d", port))
                .defaultStatusHandler(s -> true, (req, res) -> { })
                .build();
    }

    /**
     * Appends {@code store=<store>&lang=en}.
     */
    public static String scoped(String path, String store) {
        return scoped(path, store, "en");
    }

    public static String scoped(String path, String store, String lang) {
        String separator = path.contains(QUERY) ? AMP : QUERY;
        return String.format("%s%sstore=%s&lang=%s", path, separator, store, lang);
    }

    /**
     * Joins segments with {@code /}: {@code path("/api/v1/private/content", "pages", id, "publish")}.
     */
    public static String path(Object... segments) {
        return Arrays.stream(segments).map(String::valueOf).collect(Collectors.joining("/"));
    }

    public static String query(String path, String query) {
        return String.format("%s%s%s", path, path.contains(QUERY) ? AMP : QUERY, query);
    }

    public static JsonNode json(ResponseEntity<String> response) {
        return JSON.readTree(response.getBody());
    }

    public static String slug(String prefix) {
        return String.format("%s-%s", prefix, UUID.randomUUID().toString().substring(0, 8));
    }

    public static void expect(ResponseEntity<String> response, HttpStatus status) {
        assertThat(response.getStatusCode()).as(response.getBody()).isEqualTo(status);
    }

    public ResponseEntity<String> get(String url, String token) {
        RestClient.RequestHeadersSpec<?> spec = client.get().uri(url);
        if (token != null) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, bearer(token));
        }
        return spec.retrieve().toEntity(String.class);
    }

    public ResponseEntity<String> send(HttpMethod method, String url, String token, String body) {
        RestClient.RequestBodySpec spec = client.method(method).uri(url).contentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, bearer(token));
        }
        if (body != null) {
            spec = spec.body(body);
        }
        return spec.retrieve().toEntity(String.class);
    }

    public ResponseEntity<String> upload(String url, String token, String filename, byte[] bytes) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("files", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        return client.post().uri(url).header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.MULTIPART_FORM_DATA).body(form).retrieve().toEntity(String.class);
    }

    private static String bearer(String token) {
        return String.format("Bearer %s", token);
    }

}
