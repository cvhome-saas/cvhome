package com.asrevo.cvhome.uaa.mapper;

import com.asrevo.cvhome.uaa.dto.ClientDetails;
import com.asrevo.cvhome.uaa.dto.ClientDetailsSettings;
import com.asrevo.cvhome.uaa.dto.ClientDetailsTokens;
import com.asrevo.cvhome.uaa.service.ClientAuthMethod;
import com.asrevo.cvhome.uaa.service.OAuthGrantType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientClientDetailsMapper {

	public static ClientDetails toClientDetails(RegisteredClient client) {
		Set<ClientAuthMethod> clientAuthenticationMethods = client.getClientAuthenticationMethods()
			.stream()
			.map(ClientAuthenticationMethod::getValue)
			.map(ClientAuthMethod::from)
			.collect(Collectors.toSet());
		Set<OAuthGrantType> authorizationGrantTypes = client.getAuthorizationGrantTypes()
			.stream()
			.map(AuthorizationGrantType::getValue)
			.map(OAuthGrantType::from)
			.collect(Collectors.toSet());
		return new ClientDetails(client.getId(), client.getClientId(), client.getClientName(),
				clientAuthenticationMethods, authorizationGrantTypes, client.getRedirectUris(),
				client.getPostLogoutRedirectUris(), client.getScopes(), toClientSetting(client.getClientSettings()),
				toTokenSetting(client.getTokenSettings()));
	}

	private static ClientDetailsTokens toTokenSetting(TokenSettings settings) {
		return new ClientDetailsTokens(settings.getAuthorizationCodeTimeToLive(), settings.getAccessTokenTimeToLive(),
				settings.getAccessTokenFormat(), settings.getDeviceCodeTimeToLive(), settings.isReuseRefreshTokens(),
				settings.getRefreshTokenTimeToLive(), settings.getIdTokenSignatureAlgorithm(),
				settings.isX509CertificateBoundAccessTokens());
	}

	private static ClientDetailsSettings toClientSetting(ClientSettings settings) {
		return new ClientDetailsSettings(settings.isRequireProofKey(), settings.isRequireAuthorizationConsent(),
				settings.getJwkSetUrl(), (SignatureAlgorithm) settings.getTokenEndpointAuthenticationSigningAlgorithm(),
				settings.getX509CertificateSubjectDN());
	}

	public static RegisteredClient toRegisteredClient(ClientDetails details) {
		return toRegisteredClient(details, null);
	}

	public static RegisteredClient toRegisteredClient(ClientDetails details, RegisteredClient existingClient) {
		var tokenSettingsBuilder = TokenSettings.builder();
		ClientDetailsTokens clientDetailsTokens = details.tokenSettings();
		if (clientDetailsTokens != null) {
			if (Objects.nonNull(clientDetailsTokens.authorizationCodeTimeToLive()))
				tokenSettingsBuilder.authorizationCodeTimeToLive(clientDetailsTokens.authorizationCodeTimeToLive());
			if (Objects.nonNull(clientDetailsTokens.accessTokenTimeToLive()))
				tokenSettingsBuilder.accessTokenTimeToLive(clientDetailsTokens.accessTokenTimeToLive());
			if (Objects.nonNull(clientDetailsTokens.accessTokenFormat()))
				tokenSettingsBuilder.accessTokenFormat(clientDetailsTokens.accessTokenFormat());
			if (Objects.nonNull(clientDetailsTokens.deviceCodeTimeToLive()))
				tokenSettingsBuilder.deviceCodeTimeToLive(clientDetailsTokens.deviceCodeTimeToLive());
			if (Objects.nonNull(clientDetailsTokens.refreshTokenTimeToLive()))
				tokenSettingsBuilder.refreshTokenTimeToLive(clientDetailsTokens.refreshTokenTimeToLive());
			if (Objects.nonNull(clientDetailsTokens.idTokenSignatureAlgorithm()))
				tokenSettingsBuilder.idTokenSignatureAlgorithm(clientDetailsTokens.idTokenSignatureAlgorithm());
			tokenSettingsBuilder.reuseRefreshTokens(clientDetailsTokens.reuseRefreshTokens());
			tokenSettingsBuilder
				.x509CertificateBoundAccessTokens(clientDetailsTokens.x509CertificateBoundAccessTokens());
		}
		var clientSettingsBuilder = ClientSettings.builder();
		ClientDetailsSettings clientDetailsSettings = details.clientSettings();
		if (clientDetailsSettings != null) {

			clientSettingsBuilder.requireProofKey(clientDetailsSettings.requireProofKey());

			clientSettingsBuilder.requireAuthorizationConsent(clientDetailsSettings.requireAuthorizationConsent());
			if (Objects.nonNull(clientDetailsSettings.jwkSetUrl()))
				clientSettingsBuilder.jwkSetUrl(clientDetailsSettings.jwkSetUrl());
			if (Objects.nonNull(clientDetailsSettings.tokenEndpointAuthenticationSigningAlgorithm()))
				clientSettingsBuilder.tokenEndpointAuthenticationSigningAlgorithm(
						clientDetailsSettings.tokenEndpointAuthenticationSigningAlgorithm());
			if (Objects.nonNull(clientDetailsSettings.x509CertificateSubjectDN()))
				clientSettingsBuilder.x509CertificateSubjectDN(clientDetailsSettings.x509CertificateSubjectDN());

		}
		RegisteredClient.Builder builder = RegisteredClient.withId(details.id())
			.clientId(details.clientId())
			.clientName(details.clientName());

		if (existingClient != null) {
			builder.clientSecret(existingClient.getClientSecret());
		}

		builder.clientAuthenticationMethods(clientAuthenticationMethods -> {
			if (details.clientAuthenticationMethods() != null) {
				Set<ClientAuthenticationMethod> newClientAuthenticationMethods = details.clientAuthenticationMethods()
					.stream()
					.map(ClientAuthMethod::value)
					.map(ClientAuthenticationMethod::new)
					.collect(Collectors.toSet());
				clientAuthenticationMethods.addAll(newClientAuthenticationMethods);
			}
		}).authorizationGrantTypes(authorizationGrantTypes -> {
			if (details.authorizationGrantTypes() != null) {
				Set<AuthorizationGrantType> newAuthorizationGrantTypes = details.authorizationGrantTypes()
					.stream()
					.map(OAuthGrantType::value)
					.map(AuthorizationGrantType::new)
					.collect(Collectors.toSet());
				authorizationGrantTypes.addAll(newAuthorizationGrantTypes);
			}
		}).redirectUris(redirectUris -> {
			if (details.redirectUris() != null) {
				redirectUris.addAll(details.redirectUris());
			}
		}).postLogoutRedirectUris(postLogoutRedirectUris -> {
			if (details.postLogoutRedirectUris() != null) {
				postLogoutRedirectUris.addAll(details.postLogoutRedirectUris());
			}
		}).scopes(scopes -> {
			if (details.scopes() != null) {
				scopes.addAll(details.scopes());
			}
		}).tokenSettings(tokenSettingsBuilder.build()).clientSettings(clientSettingsBuilder.build());

		return builder.build();
	}

}
