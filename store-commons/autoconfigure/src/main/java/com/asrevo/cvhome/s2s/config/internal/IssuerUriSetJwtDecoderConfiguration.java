package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.s2s.jwt.IssuerUriSetConfigrationProperties;
import com.asrevo.cvhome.s2s.jwt.MultiIssuerJwtDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.JwkSetUriJwtDecoderBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(IssuerUriSetJwtDecoderConfiguration.IssuerUriSetJwtDecoderConfigurationImpl.class)
public class IssuerUriSetJwtDecoderConfiguration {

	@Configuration
	static class IssuerUriSetJwtDecoderConfigurationImpl {

		private final IssuerUriSetConfigrationProperties properties;

		private final List<OAuth2TokenValidator<Jwt>> additionalValidators;

		IssuerUriSetJwtDecoderConfigurationImpl(IssuerUriSetConfigrationProperties properties,
				ObjectProvider<OAuth2TokenValidator<Jwt>> additionalValidators) {
			this.properties = properties;
			this.additionalValidators = additionalValidators.orderedStream().toList();
		}

		@Bean
		@Conditional(IssuerUriSetCondition.class)
		MultiIssuerJwtDecoder jwtDecoderByIssuerUri(ObjectProvider<JwkSetUriJwtDecoderBuilderCustomizer> customizers) {

			Function<String, JwtDecoder> stringReactiveJwtDecoderFunction = issuer -> {
				NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder builder = NimbusJwtDecoder.withIssuerLocation(issuer);
				customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
				NimbusJwtDecoder jwtDecoder = builder.build();
				jwtDecoder.setJwtValidator(getValidators(JwtValidators.createDefaultWithIssuer(issuer)));
				return jwtDecoder;
			};
			return new MultiIssuerJwtDecoder(this.properties.getIssuerUriSet(), stringReactiveJwtDecoderFunction);
		}

		private OAuth2TokenValidator<Jwt> getValidators(OAuth2TokenValidator<Jwt> defaultValidator) {
			List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
			validators.add(defaultValidator);
			validators.addAll(this.additionalValidators);
			return new DelegatingOAuth2TokenValidator<>(validators);
		}

	}

}
