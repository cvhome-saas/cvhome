package com.asrevo.cvhome.checkout.api.v1;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.checkout.api.v1.order.ExternalOrderSignalApi;
import com.asrevo.cvhome.checkout.services.order.ExternalOrderSignalService;
import com.asrevo.cvhome.checkout.services.order.IOrderSignalService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The client half and the server half of the signal contract are two interfaces the compiler cannot relate. This
 * test does: every client exchange resolves to the same full path as the controller mapping of the same name, and
 * every controller method is gated by the signal token.
 */
class ExternalOrderSignalServiceContractTest {

    private static final String SIGNAL_TOKEN = "STORE-POD.CHECKOUT.SIGNAL";

    private static String clientPath(Method method) {
        String base = ExternalOrderSignalService.class.getAnnotation(HttpExchange.class).value();
        return base + method.getAnnotation(PostExchange.class).value();
    }

    private static String serverPath(Method method) {
        String base = ExternalOrderSignalApi.class.getAnnotation(RequestMapping.class).value()[0];
        return base + AnnotatedElementUtils.findMergedAnnotation(method, PostMapping.class).value()[0];
    }

    @Test
    void everyClientCallLandsOnAControllerMethodWithTheSamePath() {
        Map<String, Method> server = Arrays.stream(ExternalOrderSignalApi.class.getDeclaredMethods())
                .filter(m -> AnnotatedElementUtils.hasAnnotation(m, PostMapping.class))
                .collect(Collectors.toMap(Method::getName, Function.identity()));

        Method[] client = ExternalOrderSignalService.class.getDeclaredMethods();
        assertThat(client).isNotEmpty();
        for (Method call : client) {
            Method handler = server.get(call.getName());
            assertThat(handler).as("controller method for %s", call.getName()).isNotNull();
            assertThat(serverPath(handler)).isEqualTo(clientPath(call));
            assertThat(handler.getParameterTypes()).isEqualTo(call.getParameterTypes());
        }
        assertThat(IOrderSignalService.class.getDeclaredMethods()).hasSize(client.length);
    }

    @Test
    void everySignalEndpointIsGatedByTheSignalToken() {
        for (Method handler : ExternalOrderSignalApi.class.getDeclaredMethods()) {
            if (!AnnotatedElementUtils.hasAnnotation(handler, PostMapping.class)) {
                continue;
            }
            PreAuthorize gate = AnnotatedElementUtils.findMergedAnnotation(handler, PreAuthorize.class);
            assertThat(gate).as("%s must be gated", handler.getName()).isNotNull();
            assertThat(gate.value()).contains(SIGNAL_TOKEN);
        }
    }
}
