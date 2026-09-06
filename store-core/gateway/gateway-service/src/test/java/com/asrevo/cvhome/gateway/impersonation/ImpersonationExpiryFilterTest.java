package com.asrevo.cvhome.gateway.impersonation;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** The expiry check runs on every request, before the chain — so the context the chain loads is the restored one. */
class ImpersonationExpiryFilterTest {

    private final ImpersonationService impersonation = mock(ImpersonationService.class);

    private final WebFilterChain chain = mock(WebFilterChain.class);

    private final ImpersonationExpiryFilter filter = new ImpersonationExpiryFilter(impersonation);

    @Test
    void asksTheServiceThenContinuesTheChain() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/dashboard").build());
        when(impersonation.expireIfDue(exchange)).thenReturn(Mono.empty());
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        var order = inOrder(impersonation, chain);
        order.verify(impersonation).expireIfDue(exchange);
        order.verify(chain).filter(any());
    }

}
