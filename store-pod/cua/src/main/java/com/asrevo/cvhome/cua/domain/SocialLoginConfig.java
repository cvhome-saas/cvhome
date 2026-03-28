package com.asrevo.cvhome.cua.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "social_login_configs")
@Data
@NoArgsConstructor
public class SocialLoginConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "client_id", nullable = false)
	private String clientId;

	@Column(name = "provider_id", nullable = false)
	private String providerId; // google, facebook, etc.

	@Column(name = "app_id", nullable = false)
	private String appId; // OAuth2 Client ID for the provider

	@Column(name = "app_secret", nullable = false)
	private String appSecret; // OAuth2 Client Secret for the provider

	@Column(name = "scopes")
	private String scopes; // Comma-separated scopes

}
