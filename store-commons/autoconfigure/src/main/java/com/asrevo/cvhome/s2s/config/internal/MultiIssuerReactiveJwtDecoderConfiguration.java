package com.asrevo.cvhome.s2s.config.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.reactive.JwkSetUriReactiveJwtDecoderBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.util.StringUtils;

import com.asrevo.cvhome.s2s.jwt.IssuerRealm;
import com.asrevo.cvhome.s2s.jwt.IssuerRegistry;
import com.asrevo.cvhome.s2s.jwt.MultiIssuerReactiveJwtDecoder;
import com.asrevo.cvhome.s2s.jwt.RealmIssuerValidator;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Import(MultiIssuerReactiveJwtDecoderConfiguration.MultiIssuerReactiveJwtDecoderConfigurationImpl.class)
@SuppressWarnings("java:S1118")
public class MultiIssuerReactiveJwtDecoderConfiguration {

    static class MultiIssuerReactiveJwtDecoderConfigurationImpl {

        private final List<OAuth2TokenValidator<Jwt>> additionalValidators;

        MultiIssuerReactiveJwtDecoderConfigurationImpl(
                ObjectProvider<OAuth2TokenValidator<Jwt>> additionalValidators) {
            this.additionalValidators = additionalValidators.orderedStream().toList();
        }

        @Bean
        @Conditional(IssuerRealmsCondition.class)
        MultiIssuerReactiveJwtDecoder multiIssuerJwtDecoder(IssuerRegistry registry,
                ObjectProvider<JwkSetUriReactiveJwtDecoderBuilderCustomizer> customizers) {

            Function<IssuerRealm, ReactiveJwtDecoder> decoderFactory = realm -> {
                NimbusReactiveJwtDecoder.JwkSetUriReactiveJwtDecoderBuilder builder = builderFor(realm);
                customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
                NimbusReactiveJwtDecoder jwtDecoder = builder.build();
                jwtDecoder.setJwtValidator(getValidators(new RealmIssuerValidator(realm)));
                return jwtDecoder;
            };

            return new MultiIssuerReactiveJwtDecoder(registry, decoderFactory);
        }

        /** Same reasoning as the servlet twin: a known JWKS beats discovery, discovery stays as the fallback. */
        private static NimbusReactiveJwtDecoder.JwkSetUriReactiveJwtDecoderBuilder builderFor(IssuerRealm realm) {
            if (StringUtils.hasText(realm.jwkSetUri())) {
                return NimbusReactiveJwtDecoder.withJwkSetUri(realm.jwkSetUri());
            }
            return NimbusReactiveJwtDecoder.withIssuerLocation(realm.issuerLocation());
        }

        private OAuth2TokenValidator<Jwt> getValidators(OAuth2TokenValidator<Jwt> issuerValidator) {
            List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
            validators.add(JwtValidators.createDefault());
            validators.add(issuerValidator);
            validators.addAll(this.additionalValidators);
            return new DelegatingOAuth2TokenValidator<>(validators);
        }

    }

}
