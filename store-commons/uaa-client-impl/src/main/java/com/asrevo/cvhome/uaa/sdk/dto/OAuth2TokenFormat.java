package com.asrevo.cvhome.uaa.sdk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OAuth2TokenFormat {

	/**
	 * Self-contained tokens use a protected, time-limited data structure that contains
	 * token metadata and claims of the user and/or client. JSON Web Token (JWT) is a
	 * widely used format.
	 */
	public static final OAuth2TokenFormat SELF_CONTAINED = new OAuth2TokenFormat("self-contained");

	/**
	 * Reference (opaque) tokens are unique identifiers that serve as a reference to the
	 * token metadata and claims of the user and/or client, stored at the provider.
	 */
	public static final OAuth2TokenFormat REFERENCE = new OAuth2TokenFormat("reference");

	private String value;

}
