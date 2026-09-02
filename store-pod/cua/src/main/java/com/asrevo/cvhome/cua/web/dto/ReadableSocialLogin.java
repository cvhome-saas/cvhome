package com.asrevo.cvhome.cua.web.dto;

/**
 * A social provider a store has enabled, as the storefront's login page needs it: enough to render a button and
 * link it to {@code /cua/oauth2/authorization/{registrationId}}. Never the app id or secret — those stay in
 * {@link ReadableSocialLoginConfig}, behind the private API.
 */
public record ReadableSocialLogin(String providerId, String name, String registrationId) {
}
