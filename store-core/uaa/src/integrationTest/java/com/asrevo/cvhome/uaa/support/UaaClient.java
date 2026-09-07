package com.asrevo.cvhome.uaa.support;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The HTTP side of every uaa integration test.
 *
 * <p>
 * uaa issues the tokens, so a test cannot sign its own: an operator token is a real {@code client_credentials}
 * exchange against the seeded clients, and an operator <em>session</em> is a real {@code POST /login} with the CSRF
 * cookie the page set — the same two paths the platform takes. Redirects are never followed; the redirect target is
 * usually the assertion.
 * </p>
 */
public final class UaaClient {

    /** The seeded platform-admin SDK client — scope {@code super_admin}, secret from {@code application.yml}. */
    public static final String ADMIN_SDK = "admin-sdk";

    /** The seeded store-core service client — scope {@code store_core}, no admin authority. */
    public static final String STORE_CORE = "store-core@service.store-core.internal";

    /** The console's authorization-code client. */
    public static final String WEB_APP = "web-app";

    /** The gateway's impersonation client — the only holder of the token-exchange grant. */
    public static final String IMPERSONATION = "console-impersonation";

    /** Where web-app's code comes back; one of the seeded redirect URIs. */
    public static final String WEB_APP_REDIRECT = "http://gateway.com:8000/login/oauth2/code/uaa";

    public static final String LCL_SECRET = "hLwOF59NEOdMzYYrfxUbQEGVK1uTczj7";

    public static final String SUPER_ADMIN = "super-admin";

    public static final String ORG1_ADMIN = "org1-admin";

    public static final String ORG1_STORE1_ADMIN = "org1-store1-admin";

    public static final String SUPPORT = "support";

    /** Every seeded account's password. */
    public static final String PASSWORD = "admin";

    public static final String XSRF_COOKIE = "XSRF-TOKEN";

    public static final String XSRF_HEADER = "X-XSRF-TOKEN";

    public static final String GET = "GET";

    public static final String POST = "POST";

    public static final String PUT = "PUT";

    public static final String DELETE = "DELETE";

    public static final String LOGIN = "/login";

    public static final String LOCATION = "location";

    public static final String ME = "/api/v1/auth/me";

    private static final String AUTHORIZATION = "Authorization";

    private static final String TOKEN_ENDPOINT = "/oauth2/token";

    private static final String GRANT_TYPE = "grant_type";

    private static final String SCOPE = "scope";

    private static final String CODE = "code";

    private static final String REDIRECT_URI = "redirect_uri";

    private static final String ACCESS_TOKEN = "access_token";

    private static final String TOKEN_ANSWERED = "token endpoint answered %d: %s";

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String FORM = "application/x-www-form-urlencoded";

    private static final String JSON_TYPE = "application/json";

    private static final ObjectMapper JSON = new ObjectMapper();

    private final int port;

    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .cookieHandler(cookies)
            .build();

    public UaaClient(int port) {
        this.port = port;
    }

    public String url(String path) {
        return String.format("http://localhost:%d%s", port, path);
    }

    // --- tokens ---------------------------------------------------------------------------------------------------

    public String clientCredentialsToken(String clientId, String secret, String scope) throws IOException, InterruptedException {
        HttpResponse<String> response = clientPost(clientId, secret, TOKEN_ENDPOINT,
                Map.of(GRANT_TYPE, "client_credentials", SCOPE, scope));
        if (response.statusCode() != 200) {
            throw new IllegalStateException(String.format(TOKEN_ANSWERED, response.statusCode(), response.body()));
        }
        return JSON.readTree(response.body()).get(ACCESS_TOKEN).asText();
    }

    /** A form POST authenticated as an OAuth2 client with HTTP Basic — the token, introspection and revocation calls. */
    public HttpResponse<String> clientPost(String clientId, String secret, String path, Map<String, String> fields)
            throws IOException, InterruptedException {
        String credentials = String.format("%s:%s", clientId, secret);
        String basic = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create(url(path)))
                .header(AUTHORIZATION, String.format("Basic %s", basic))
                .header(CONTENT_TYPE, FORM)
                .POST(HttpRequest.BodyPublishers.ofString(form(fields)))
                .build();
        return send(request);
    }

    public String superAdminToken() throws IOException, InterruptedException {
        return clientCredentialsToken(ADMIN_SDK, LCL_SECRET, "super_admin");
    }

    public String storeCoreToken() throws IOException, InterruptedException {
        return clientCredentialsToken(STORE_CORE, LCL_SECRET, "store_core");
    }

    /**
     * A person's access token, the way the gateway obtains one: sign in, run the authorization-code flow with PKCE
     * against {@code web-app}, redeem the code. Leaves this client signed in as that person.
     */
    public String userAccessToken(String username) throws IOException, InterruptedException, NoSuchAlgorithmException {
        String verifier = "0123456789abcdef0123456789abcdef0123456789abcdef";
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        login(username, PASSWORD);
        String authorize = UriComponentsBuilder.fromPath("/oauth2/authorize")
                .queryParam("response_type", CODE).queryParam("client_id", WEB_APP)
                .queryParam(REDIRECT_URI, WEB_APP_REDIRECT).queryParam(SCOPE, "openid").queryParam("state", "s")
                .queryParam("code_challenge", challenge).queryParam("code_challenge_method", "S256")
                .build().toUriString();
        HttpResponse<String> granted = session(GET, authorize, null);
        if (granted.statusCode() != 302) {
            throw new IllegalStateException(String.format("authorize answered %d: %s", granted.statusCode(), granted.body()));
        }
        String code = UriComponentsBuilder.fromUriString(location(granted)).build().getQueryParams().getFirst(CODE);
        HttpResponse<String> tokens = clientPost(WEB_APP, LCL_SECRET, TOKEN_ENDPOINT, Map.of(GRANT_TYPE, "authorization_code",
                CODE, Objects.requireNonNull(code), REDIRECT_URI, WEB_APP_REDIRECT, "code_verifier", verifier));
        if (tokens.statusCode() != 200) {
            throw new IllegalStateException(String.format(TOKEN_ANSWERED, tokens.statusCode(), tokens.body()));
        }
        return JSON.readTree(tokens.body()).get(ACCESS_TOKEN).asText();
    }

    // --- sessions -------------------------------------------------------------------------------------------------

    /** Loads the login page (which plants the CSRF cookie) and posts the form; the caller asserts the redirect. */
    public HttpResponse<String> login(String username, String password) throws IOException, InterruptedException {
        send(HttpRequest.newBuilder(URI.create(url(LOGIN))).GET().build());
        return postForm(LOGIN, Map.of("username", username, "password", password, "_csrf", csrfToken()));
    }

    public HttpResponse<String> postForm(String path, Map<String, String> fields) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url(path)))
                .header(CONTENT_TYPE, FORM)
                .POST(HttpRequest.BodyPublishers.ofString(form(fields)))
                .build();
        return send(request);
    }

    /** The CSRF cookie value, or an empty string when the server has not planted one. */
    public String csrfToken() {
        return cookies.getCookieStore().getCookies().stream()
                .filter(c -> XSRF_COOKIE.equals(c.getName()))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElse("");
    }

    /** Every cookie the client holds, for a test that needs to explain a 403. */
    public String cookieDump() {
        return cookies.getCookieStore().getCookies().stream()
                .map(c -> String.format("%s=%s(domain=%s,path=%s)", c.getName(), c.getValue(), c.getDomain(), c.getPath()))
                .collect(java.util.stream.Collectors.joining(" "));
    }

    public void clearCookies() {
        cookies.getCookieStore().removeAll();
    }

    // --- JSON calls -----------------------------------------------------------------------------------------------

    /** A JSON call on the current session (cookies + CSRF header), no bearer token. */
    public HttpResponse<String> session(String method, String path, String body) throws IOException, InterruptedException {
        return json(method, path, body, null, true);
    }

    /** The same call with the CSRF header deliberately left off — what a cross-site page could send. */
    public HttpResponse<String> sessionWithoutCsrf(String method, String path, String body)
            throws IOException, InterruptedException {
        return json(method, path, body, null, false);
    }

    /** A JSON call with a bearer token and no session. */
    public HttpResponse<String> bearer(String method, String path, String body, String token)
            throws IOException, InterruptedException {
        return json(method, path, body, Objects.requireNonNull(token), true);
    }

    /** Anonymous: no token, no cookies. */
    public HttpResponse<String> anonymous(String method, String path) throws IOException, InterruptedException {
        return anonymous(method, path, null);
    }

    /** Anonymous with a JSON body — the public accept endpoints. */
    public HttpResponse<String> anonymous(String method, String path, String body) throws IOException, InterruptedException {
        clearCookies();
        return json(method, path, body, null, false);
    }

    private HttpResponse<String> json(String method, String path, String body, String token, boolean csrfHeader)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url(path))).header("Accept", JSON_TYPE);
        if (csrfHeader) {
            request.header(XSRF_HEADER, csrfToken());
        }
        if (token != null) {
            request.header(AUTHORIZATION, String.format("Bearer %s", token));
        }
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header(CONTENT_TYPE, JSON_TYPE).method(method, HttpRequest.BodyPublishers.ofString(body));
        }
        return send(request.build());
    }

    public HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static JsonNode body(HttpResponse<String> response) throws IOException {
        return JSON.readTree(response.body());
    }

    /** The payload of a compact JWT, undecoded and unverified — for asserting what uaa put in it. */
    public static JsonNode claims(String jwt) throws IOException {
        String payload = jwt.split("\\.")[1];
        return JSON.readTree(Base64.getUrlDecoder().decode(payload));
    }

    public static String location(HttpResponse<String> response) {
        return response.headers().firstValue(LOCATION).orElseThrow();
    }

    public static String form(Map<String, String> fields) {
        return fields.entrySet().stream()
                .map(e -> String.format("%s=%s", URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8),
                        URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)))
                .collect(Collectors.joining("&"));
    }

}
