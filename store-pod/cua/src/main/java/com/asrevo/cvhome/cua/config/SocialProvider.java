package com.asrevo.cvhome.cua.config;

import lombok.Getter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@Getter
public enum SocialProvider {

	GOOGLE("google", "Google",
			(registrationId) -> ClientRegistrations.fromOidcIssuerLocation("https://accounts.google.com")
				.registrationId(registrationId)
				.scope("openid", "profile", "email"),
			"email", "name"),

	FACEBOOK("facebook", "Facebook",
			(registrationId) -> ClientRegistration.withRegistrationId(registrationId)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
				.scope("email", "public_profile")
				.authorizationUri("https://www.facebook.com/v19.0/dialog/oauth")
				.tokenUri("https://graph.facebook.com/v19.0/oauth/access_token")
				.userInfoUri("https://graph.facebook.com/me?fields=id,name,email,picture")
				.userNameAttributeName("id")
				.clientName("Facebook")
				.scope("email", "public_profile"),
			"email", "name"),

	GITHUB("github", "GitHub",
			(registrationId) -> ClientRegistration.withRegistrationId(registrationId)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
				.scope("read:user", "user:email")
				.authorizationUri("https://github.com/login/oauth/authorize")
				.tokenUri("https://github.com/login/oauth/access_token")
				.userInfoUri("https://api.github.com/user")
				.userNameAttributeName("id")
				.clientName("GitHub")
				.scope("read:user", "user:email"),
			"email", "name");

	private final String providerId;

	private final String clientName;

	private final Function<String, ClientRegistration.Builder> builderFunction;

	private final String emailAttribute;

	private final String nameAttribute;

	SocialProvider(String providerId, String clientName, Function<String, ClientRegistration.Builder> builderFunction,
			String emailAttribute, String nameAttribute) {
		this.providerId = providerId;
		this.clientName = clientName;
		this.builderFunction = builderFunction;
		this.emailAttribute = emailAttribute;
		this.nameAttribute = nameAttribute;
	}

	public static SocialProvider fromProviderId(String providerId) {
		return Arrays.stream(values())
			.filter(p -> p.getProviderId().equalsIgnoreCase(providerId))
			.findFirst()
			.orElse(null);
	}

	public String getEmail(Map<String, Object> attributes) {
		Object email = attributes.get(emailAttribute);
		if (email == null) {
			// Fallback to id if email is not present
			return (String) attributes.get("id");
		}
		return (String) email;
	}

	public String getName(Map<String, Object> attributes) {
		return (String) attributes.get(nameAttribute);
	}

	public ClientRegistration.Builder createBuilder(String registrationId) {
		return builderFunction.apply(registrationId);
	}

}
