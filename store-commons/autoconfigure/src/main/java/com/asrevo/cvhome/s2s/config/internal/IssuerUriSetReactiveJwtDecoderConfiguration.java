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

import com.asrevo.cvhome.s2s.jwt.IssuerUriSetConfigrationProperties;
import com.asrevo.cvhome.s2s.jwt.MultiIssuerReactiveJwtDecoder;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Import(IssuerUriSetReactiveJwtDecoderConfiguration.IssuerUriSetReactiveJwtDecoderConfigurationImpl.class)
public class IssuerUriSetReactiveJwtDecoderConfiguration {

    static class IssuerUriSetReactiveJwtDecoderConfigurationImpl {

        private final IssuerUriSetConfigrationProperties properties;

        private final List<OAuth2TokenValidator<Jwt>> additionalValidators;

        IssuerUriSetReactiveJwtDecoderConfigurationImpl(IssuerUriSetConfigrationProperties properties,
                                                        ObjectProvider<OAuth2TokenValidator<Jwt>> additionalValidators) {
            this.properties = properties;
            this.additionalValidators = additionalValidators.orderedStream().toList();
        }

        @Bean
        @Conditional(IssuerUriSetCondition.class)
        MultiIssuerReactiveJwtDecoder jwtDecoderByIssuerUri(
                ObjectProvider<JwkSetUriReactiveJwtDecoderBuilderCustomizer> customizers) {
            Function<String, ReactiveJwtDecoder> stringReactiveJwtDecoderFunction = issuer -> {
                NimbusReactiveJwtDecoder.JwkSetUriReactiveJwtDecoderBuilder builder = NimbusReactiveJwtDecoder
                        .withIssuerLocation(issuer);
                customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
                NimbusReactiveJwtDecoder jwtDecoder = builder.build();
                jwtDecoder.setJwtValidator(getValidators(JwtValidators.createDefaultWithIssuer(issuer)));
                return jwtDecoder;
            };

            return new MultiIssuerReactiveJwtDecoder(this.properties.getIssuerUriSet(),
                    stringReactiveJwtDecoderFunction);
        }

        private OAuth2TokenValidator<Jwt> getValidators(OAuth2TokenValidator<Jwt> defaultValidator) {

            List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
            validators.add(defaultValidator);

            validators.addAll(this.additionalValidators);
            return new DelegatingOAuth2TokenValidator<>(validators);
        }

    }

}
