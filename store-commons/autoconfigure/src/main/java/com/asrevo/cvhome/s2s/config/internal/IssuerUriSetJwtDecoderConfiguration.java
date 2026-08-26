package com.asrevo.cvhome.s2s.config.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.JwkSetUriJwtDecoderBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;

import com.asrevo.cvhome.s2s.jwt.IssuerRealm;
import com.asrevo.cvhome.s2s.jwt.IssuerRegistry;
import com.asrevo.cvhome.s2s.jwt.MultiIssuerJwtDecoder;
import com.asrevo.cvhome.s2s.jwt.RealmIssuerValidator;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(IssuerUriSetJwtDecoderConfiguration.IssuerUriSetJwtDecoderConfigurationImpl.class)
@SuppressWarnings("java:S1118")
public class IssuerUriSetJwtDecoderConfiguration {

    @Configuration
    static class IssuerUriSetJwtDecoderConfigurationImpl {

        private final List<OAuth2TokenValidator<Jwt>> additionalValidators;

        IssuerUriSetJwtDecoderConfigurationImpl(ObjectProvider<OAuth2TokenValidator<Jwt>> additionalValidators) {
            this.additionalValidators = additionalValidators.orderedStream().toList();
        }

        @Bean
        @Conditional(IssuerUriSetCondition.class)
        MultiIssuerJwtDecoder jwtDecoderByIssuerUri(IssuerRegistry registry,
                                                    ObjectProvider<JwkSetUriJwtDecoderBuilderCustomizer> customizers) {

            Function<IssuerRealm, JwtDecoder> decoderFactory = realm -> {
                NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder builder = builderFor(realm);
                customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
                NimbusJwtDecoder jwtDecoder = builder.build();
                jwtDecoder.setJwtValidator(getValidators(new RealmIssuerValidator(realm)));
                return jwtDecoder;
            };
            return new MultiIssuerJwtDecoder(registry, decoderFactory);
        }

        /**
         * A configured {@code jwk-set-uri} is preferred over OIDC discovery for three reasons: discovery is a
         * blocking network call on the first authenticated request, its failure is not cached so every request
         * retries it, and {@code withIssuerLocation} asserts that the discovered {@code issuer} string equals the
         * location asked for — too literal for a realm reachable at several equivalent URIs. Discovery remains
         * the fallback so a realm can be declared with nothing but its issuer.
         */
        private static NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder builderFor(IssuerRealm realm) {
            if (StringUtils.hasText(realm.jwkSetUri())) {
                return NimbusJwtDecoder.withJwkSetUri(realm.jwkSetUri());
            }
            return NimbusJwtDecoder.withIssuerLocation(realm.issuerLocation());
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
