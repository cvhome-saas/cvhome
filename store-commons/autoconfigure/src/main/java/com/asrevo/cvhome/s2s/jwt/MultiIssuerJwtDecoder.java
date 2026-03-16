package com.asrevo.cvhome.s2s.jwt;

import com.asrevo.cvhome.s2s.utils.UrlNormalize;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * A {@link JwtDecoder} that supports multiple JWT issuers for synchronous environments.
 * It lazily initializes and caches a specific {@link JwtDecoder} for each issuer based on
 * the 'iss' claim in the JWT.
 */
public class MultiIssuerJwtDecoder implements JwtDecoder {

	private final Map<String, JwtDecoder> issuerDecoders = new ConcurrentHashMap<>();

	private final Set<String> supportedIssuerUris;

	private final Function<String, JwtDecoder> decoderFactory;

	/**
	 * Constructs a {@code MultiIssuerJwtDecoder}.
	 * @param supportedIssuerUris A set of trusted issuer URIs that this decoder will
	 * handle. An attempt to decode a token from an issuer not in this set will result in
	 * an error. Must not be null.
	 * @param decoderFactory A function that takes an issuer URI (String) and returns a
	 * {@link JwtDecoder} configured for that issuer. This factory is invoked lazily when
	 * a token from a new, supported issuer is encountered, and its result is cached. Must
	 * not be null. The factory should throw an exception if it cannot create a decoder
	 * for a given supported issuer URI.
	 */
	public MultiIssuerJwtDecoder(Set<String> supportedIssuerUris, Function<String, JwtDecoder> decoderFactory) {
		Objects.requireNonNull(supportedIssuerUris, "supportedIssuerUris cannot be null");
		Objects.requireNonNull(decoderFactory, "decoderFactory cannot be null");

		this.supportedIssuerUris = supportedIssuerUris.stream()
			.map(UrlNormalize::normalizeUri)
			.collect(Collectors.toSet());
		this.decoderFactory = decoderFactory;
	}

	@Override
	public Jwt decode(String token) throws JwtException {
		Objects.requireNonNull(token, "token cannot be null");
		try {
			String issuer = extractIssuer(token);
			JwtDecoder delegateDecoder = getDecoderForIssuer(issuer);
			return delegateDecoder.decode(token);
		}
		catch (JwtException e) {
			throw e;
		}
		catch (Exception e) {
			throw new JwtException("Failed to decode JWT: " + e.getMessage(), e);
		}
	}

	/**
	 * Retrieves or creates a {@link JwtDecoder} for the given issuer.
	 * @param issuer The issuer URI.
	 * @return The {@link JwtDecoder} for the issuer.
	 * @throws JwtException if the issuer is unsupported or the factory fails to create a
	 * decoder.
	 */
	private JwtDecoder getDecoderForIssuer(String issuer) throws JwtException {
		if (!this.supportedIssuerUris.contains(issuer)) {
			throw new JwtException(String.format(
					"Unsupported issuer: '%s'. Issuer not in the configured list of"
							+ " supported issuers: %s.",
					issuer, this.supportedIssuerUris));
		}
		JwtDecoder delegateDecoder = this.issuerDecoders.computeIfAbsent(issuer, this.decoderFactory);
		if (delegateDecoder == null) {
			throw new JwtException(String.format("Decoder factory returned null for supported issuer: '%s'. This"
					+ " indicates an issue with the factory configuration.", issuer));
		}
		return delegateDecoder;
	}

	/**
	 * Extracts the 'iss' (issuer) claim from the JWT.
	 * @param token The JWT string.
	 * @return The issuer URI.
	 * @throws JwtException if the token cannot be parsed or the 'iss' claim is missing or
	 * empty.
	 */
	private String extractIssuer(String token) throws JwtException {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
			String issuer = claimsSet.getIssuer();
			if (issuer == null || issuer.trim().isEmpty()) {
				throw new JwtException("Missing or empty 'iss' (issuer) claim in token");
			}
			return issuer;
		}
		catch (ParseException e) {
			throw new JwtException("Failed to parse token to extract issuer: " + e.getMessage(), e);
		}
	}

}
