package com.asrevo.cvhome.cua.config;

import com.asrevo.cvhome.cua.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.asrevo.cvhome.cua.repo.SocialLoginConfigRepository;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class AppSecurityConfig {

	private final SocialLoginConfigRepository socialLoginConfigRepository;

	private final CustomOAuth2UserService customOAuth2UserService;

	@Bean
	SecurityFilterChain appSecurity(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.requestMatchers("/.well-known/**")
			.permitAll()
			.requestMatchers(EndpointRequest.toAnyEndpoint())
			.permitAll()
			.requestMatchers("/login", "/assets/**", "/media/**", "/img/**", "/webfonts/**", "/js/**", "/css/**",
					"/*.css", "/*.js", "/favicon.ico", "/api/v1/me")
			.permitAll()
			.requestMatchers("/swagger-ui.html")
			.permitAll()
			.requestMatchers("/swagger-ui/**")
			.permitAll()
			.requestMatchers("/v3/api-docs/**")
			.permitAll()
			.anyRequest()
			.authenticated())
			.formLogin(it -> it.loginPage("/login"))
			.oauth2Login(it -> it.loginPage("/login")
				.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)))
			.csrf(AbstractHttpConfigurer::disable)
			.requestCache(cache -> cache.requestCache(requestCache()))

			.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
		return http.build();
	}

	@Bean
	public DynamicClientRegistrationRepository clientRegistrationRepository() {
		return new DynamicClientRegistrationRepository(socialLoginConfigRepository);
	}

	@Bean
	public RequestCache requestCache() {
		HttpSessionRequestCache cache = new HttpSessionRequestCache();
		RequestMatcher getRequests = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/**");
		RequestMatcher notFavicon = new NegatedRequestMatcher(
				PathPatternRequestMatcher.withDefaults().matcher("/favicon.*"));
		RequestMatcher notError = new NegatedRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher("/error"));
		RequestMatcher saveRequestMatcher = new AndRequestMatcher(getRequests, notFavicon, notError);
		cache.setRequestMatcher(saveRequestMatcher);
		return cache;
	}

}
