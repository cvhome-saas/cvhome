package com.asrevo.cvhome.sso.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.web.servlet.function.RouterFunctions.route;

/**
 * Forwards the deep links of an embedded single-page application to its {@code index.html}.
 *
 * <p>
 * Conditional on that file existing, because only one deployment has an SPA: uaa serves the admin console, cua
 * renders no HTML at all and hands every page to the storefront. Without the condition, cua answered every
 * dotless path — {@code /login} included — with a 500 for a resource it does not ship.
 * </p>
 */
@Slf4j
@Controller
@ConditionalOnResource(resources = "classpath:static/index.html")
@RequiredArgsConstructor
public class StaticController {

    private static final String ERROR = "/error";

    @Bean
    RouterFunction<ServerResponse> indexRouter() {
        return route(request -> {
            String path = request.path();
            if (path.startsWith("/api/") || path.startsWith("/oauth2/") || path.startsWith("/actuator")) {
                return false;
            }
            // The container's error dispatch. Answering it with the app would turn every refusal into a 200 page.
            if (ERROR.equals(path)) {
                return false;
            }
            return !path.contains(".");
        }, request -> ServerResponse.ok().body(new ClassPathResource("static/index.html")));
    }

}
