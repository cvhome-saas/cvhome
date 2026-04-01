package com.asrevo.cvhome.checkout.config;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.s2s.jwt.UaaJwtGrantedAuthoritiesConverter;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;
import com.asrevo.cvhome.s2s.services.AccessEvaluator;
import com.asrevo.cvhome.s2s.services.AccessEvaluatorImpl;
import com.asrevo.cvhome.s2s.services.StoreSecurityServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) {
		http.authorizeHttpRequests(
				it -> it.requestMatchers("/api/*/private/**").authenticated().anyRequest().permitAll())
			.oauth2ResourceServer(it -> it.jwt(Customizer.withDefaults()))
			.csrf(AbstractHttpConfigurer::disable);
		return http.build();
	}

	@Bean
	public JwtAuthenticationConverter converter() {
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		UaaJwtGrantedAuthoritiesConverter uaaJwtGrantedAuthoritiesConverter = new UaaJwtGrantedAuthoritiesConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(uaaJwtGrantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

	@Bean
	@Lazy
	public AccessEvaluator accessEvaluator(ExternalMerchantStoreService externalMerchantStoreService,
			PodInfoProperties podInfoProperties) {
		return new AccessEvaluatorImpl(new StoreSecurityServiceImpl(podInfoProperties, (it) -> {
			ReadableMerchantStore merchantStore = externalMerchantStoreService
				.getStore(new StoreMerchantId(it.getId().toString()));
			return new ManagerOrgId(merchantStore.getOrg());
		}));
	}
	/*
	 * @Bean public CorsConfigurationSource corsConfigurationSource() { final
	 * CorsConfiguration configuration = new CorsConfiguration();
	 *
	 * configuration.setAllowedOrigins(List.of("http://localhost")); // www - obligatory
	 * // configuration.setAllowedOrigins(ImmutableList.of("*")); //set access from all
	 * domains configuration.setAllowedMethods(List.of("OPTIONS","GET", "POST", "PUT",
	 * "DELETE")); configuration.setAllowCredentials(true);
	 * configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control",
	 * "Content-Type"));
	 *
	 * final UrlBasedCorsConfigurationSource source = new
	 * UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**",
	 * configuration);
	 *
	 * return source; }
	 */

}
