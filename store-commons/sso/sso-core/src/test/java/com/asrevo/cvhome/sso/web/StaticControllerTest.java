package com.asrevo.cvhome.sso.web;

import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Which paths the embedded single-page application is allowed to answer.
 *
 * <p>
 * The predicate is a deny-list of everything the server owns, and each entry is there because letting the SPA
 * answer it broke something: {@code /api/}, {@code /oauth2/} and {@code /actuator} are the server's own, and
 * {@code /error} is the container's error dispatch — answering that with the app turns every refusal into a 200
 * page, which is exactly how a 403 once became a sign-in screen with no error on it.
 * </p>
 *
 * <p>
 * Everything else without a dot is a deep link and gets {@code index.html}; anything with a dot is a static asset
 * the resource handler serves.
 * </p>
 */
class StaticControllerTest {

    private final RouterFunction<ServerResponse> router = new StaticController().indexRouter();

    @ParameterizedTest(name = "{0} -> index.html: {1}")
    @CsvSource({
        "/users, true",
        "/, true",
        "/clients/new, true",
        "/login, true",
        "/api/v1/admin/users, false",
        "/oauth2/authorize, false",
        "/actuator/health, false",
        "/error, false",
        "/main.js, false",
        "/assets/logo.svg, false",
    })
    void onlyAdeepLinkTheServerDoesNotOwnIsForwardedToTheApp(String path, boolean forwarded) {
        assertThat(route(path)).as(path).isEqualTo(forwarded);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({"/users", "/"})
    void aforwardedPathIsAnsweredWithTheAppsIndex(String path) throws Exception {
        ServerRequest request = request(path);
        Optional<HandlerFunction<ServerResponse>> handler = router.route(request);

        assertThat(handler).isPresent();
        assertThat(handler.get().handle(request).statusCode()).isEqualTo(HttpStatus.OK);
    }

    private boolean route(String path) {
        return router.route(request(path)).isPresent();
    }

    private static ServerRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        return ServerRequest.create(request, java.util.List.of());
    }

}
