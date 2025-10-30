package com.asrevo.cvhome.kc.utils;

import java.security.PrivateKey;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.AccessToken;

public record AccessTokenGenerator(KeycloakSession session, String realmName) {

	public String generate() {
		AccessToken token = new AccessToken();
		token.audience("auth");
		token.subject("auth");
		token.type("Bearer");

		token.issuedNow();
		RealmModel realmModel = session.realms().getRealmByName(realmName);
		KeyWrapper rs256 = session.keys().getActiveKey(realmModel, KeyUse.SIG, "RS256");

		PrivateKey privateKey = (PrivateKey) rs256.getPrivateKey();

		return new JWSBuilder().kid(rs256.getKid()).type("JWT").jsonContent(token).rsa256(privateKey);
	}
}
