package com.asrevo.cvhome.s2s.config.internal;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.mock.env.MockEnvironment;

import com.asrevo.cvhome.fargate.task.EcsTask;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * The two pieces of ECS plumbing and the condition that decides whether multi-issuer JWT support is wired at all.
 *
 * <p>
 * {@link IssuerRealmsCondition} is the interesting one. It matches only when {@code issuers} is configured
 * <em>and</em> neither of Spring's single-issuer properties is — because those two are what Boot's own
 * auto-configuration keys on, and having both would build two decoders for the same requests. The refusal
 * messages name which property won, since "why is my JWT decoder the wrong one" is otherwise unanswerable from a
 * running service.
 * </p>
 */
class EcsAndConditionTest {

    private static final String ISSUERS = "spring.security.oauth2.resourceserver.jwt.issuers";
    private static final String ISSUER_URI = "spring.security.oauth2.resourceserver.jwt.issuer-uri";
    private static final String JWK_SET_URI = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri";
    private static final String AN_ISSUER = "https://uaa.gateway.com:9002";
    private static final String ONE_ISSUER_KEY = "%s.uaa.issuer-uri";
    private static final String JWKS_URL = "%s/jwks";
    private static final String JWKS_MESSAGE = "jwk-set-uri property";
    private static final String ISSUER_URI_MESSAGE = "issuer-uri property";

    private static ConditionOutcome outcomeFor(Map<String, String> properties) {
        MockEnvironment environment = new MockEnvironment();
        properties.forEach(environment::setProperty);
        ConditionContext context = Mockito.mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);
        return new IssuerRealmsCondition().getMatchOutcome(context, null);
    }

    @Test
    void noIssuersConfiguredMeansNoMultiIssuerSupport() {
        ConditionOutcome outcome = outcomeFor(Map.of());

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage()).contains("issuers property");
    }

    @Test
    void issuersAloneIsWhatTurnsItOn() {
        ConditionOutcome outcome = outcomeFor(Map.of(ONE_ISSUER_KEY.formatted(ISSUERS), AN_ISSUER));

        assertThat(outcome.isMatch()).isTrue();
    }

    @Test
    void aSingleIssuerPropertyAlongsideIssuersRefusesAndNamesWhichOneWon() {
        // Both would build two decoders for the same requests; the message is how you find out which is live.
        ConditionOutcome withJwks = outcomeFor(Map.of(ONE_ISSUER_KEY.formatted(ISSUERS), AN_ISSUER,
                JWK_SET_URI, JWKS_URL.formatted(AN_ISSUER)));
        ConditionOutcome withIssuerUri = outcomeFor(Map.of(ONE_ISSUER_KEY.formatted(ISSUERS), AN_ISSUER,
                ISSUER_URI, AN_ISSUER));

        assertThat(withJwks.isMatch()).isFalse();
        assertThat(withJwks.getMessage()).contains(JWKS_MESSAGE);
        assertThat(withIssuerUri.isMatch()).isFalse();
        assertThat(withIssuerUri.getMessage()).contains(ISSUER_URI_MESSAGE);
    }

    @Test
    void theJwkSetUriIsCheckedBeforeTheIssuerUriSoOneMessageIsReported() {
        ConditionOutcome outcome = outcomeFor(Map.of(ONE_ISSUER_KEY.formatted(ISSUERS), AN_ISSUER,
                JWK_SET_URI, JWKS_URL.formatted(AN_ISSUER), ISSUER_URI, AN_ISSUER));

        assertThat(outcome.getMessage()).contains(JWKS_MESSAGE).doesNotContain(ISSUER_URI_MESSAGE);
    }

    @Test
    void theEcsHealthIndicatorReportsTheTaskItWasGiven() {
        EcsTaskHealthIndicator indicator =
                new EcsTaskHealthIndicator(new EcsTask(), JsonMapper.builder().build());

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
        assertThat(indicator.health().getDetails()).isNotNull();
    }

    @Test
    void ataskThatCannotBeSerialisedIsReportedAsDownRatherThanBreakingTheHealthEndpoint() {
        // An actuator endpoint that throws takes the whole health check with it, including the liveness probe.
        ObjectMapper broken = Mockito.mock(ObjectMapper.class);
        when(broken.convertValue(Mockito.any(), Mockito.<TypeReference<Map<String, Object>>>any()))
                .thenThrow(new IllegalStateException("cannot convert"));

        EcsTaskHealthIndicator indicator = new EcsTaskHealthIndicator(new EcsTask(), broken);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
    }
}
