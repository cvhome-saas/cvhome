package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

public class IssuerUriSetCondition extends SpringBootCondition {

    private static final String ISSUER_URI_SET_PROPERTY = "issuer-uri-set property";

    private static final String JWK_SET_URI_PROPERTY = "jwk-set-uri property";

    private static final String ISSUER_URI_PROPERTY = "issuer-uri property";

    private static String getIssuerUriSet(Environment environment) {
        String p = environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri-set");
        if (StringUtils.hasText(p)) {
            return p;
        }
        String p0 = environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri-set[0]");
        if (StringUtils.hasText(p0)) {
            return p0;
        }
        return null;
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition("OpenID Connect Issuer URI Set Condition");
        Environment environment = context.getEnvironment();
        String issuerUri = environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        String jwkSetUri = environment.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri");

        if (!StringUtils.hasText(getIssuerUriSet(environment))) {
            return ConditionOutcome.noMatch(message.didNotFind(ISSUER_URI_SET_PROPERTY).atAll());
        }
        if (StringUtils.hasText(jwkSetUri)) {
            return ConditionOutcome.noMatch(message.found(JWK_SET_URI_PROPERTY).items(jwkSetUri));
        }
        if (StringUtils.hasText(issuerUri)) {
            return ConditionOutcome.noMatch(message.found(ISSUER_URI_PROPERTY).items(issuerUri));
        }
        return ConditionOutcome.match(message.foundExactly(ISSUER_URI_SET_PROPERTY));
    }

}
