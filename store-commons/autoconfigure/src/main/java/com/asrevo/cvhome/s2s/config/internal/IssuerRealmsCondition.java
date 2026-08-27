package com.asrevo.cvhome.s2s.config.internal;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Activates the multi-issuer decoder when realms are configured, and yields to Boot's own single-issuer
 * autoconfiguration when {@code jwk-set-uri} or {@code issuer-uri} names one instead. Both shapes cannot be
 * honoured at once, and Boot's is the more specific request.
 */
public class IssuerRealmsCondition extends SpringBootCondition {

    private static final String ISSUERS_PROPERTY = "issuers property";

    private static final String JWK_SET_URI_PROPERTY = "jwk-set-uri property";

    private static final String ISSUER_URI_PROPERTY = "issuer-uri property";

    private static final String ISSUERS_KEY = "spring.security.oauth2.resourceserver.jwt.issuers";

    private static final String ISSUER_URI_KEY = "spring.security.oauth2.resourceserver.jwt.issuer-uri";

    private static final String JWK_SET_URI_KEY = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri";

    private static Map<String, Object> getIssuers(Environment environment) {
        return Binder.get(environment)
                .bind(ISSUERS_KEY, Bindable.mapOf(String.class, Object.class))
                .orElseGet(Map::of);
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition("OpenID Connect Issuer Realms Condition");
        Environment environment = context.getEnvironment();
        String issuerUri = environment.getProperty(ISSUER_URI_KEY);
        String jwkSetUri = environment.getProperty(JWK_SET_URI_KEY);

        if (getIssuers(environment).isEmpty()) {
            return ConditionOutcome.noMatch(message.didNotFind(ISSUERS_PROPERTY).atAll());
        }
        if (StringUtils.hasText(jwkSetUri)) {
            return ConditionOutcome.noMatch(message.found(JWK_SET_URI_PROPERTY).items(jwkSetUri));
        }
        if (StringUtils.hasText(issuerUri)) {
            return ConditionOutcome.noMatch(message.found(ISSUER_URI_PROPERTY).items(issuerUri));
        }
        return ConditionOutcome.match(message.foundExactly(ISSUERS_PROPERTY));
    }

}
