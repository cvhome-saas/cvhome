package com.asrevo.cvhome.sso.dto;

/**
 * A registration and, once, its secret. {@code clientSecret} is {@code null} for a public client and never appears in
 * any later read — only its hash is stored.
 */
public record CreatedClient(ClientDetails client, String clientSecret) {
}
