package com.asrevo.cvhome.sso.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.idp.BrokerOutcome;
import com.asrevo.cvhome.sso.idp.BrokerRefusedException;
import com.asrevo.cvhome.sso.idp.BrokeredIdentity;
import com.asrevo.cvhome.sso.idp.IdentityBrokerService;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;
import com.asrevo.cvhome.sso.idp.IdpPreset;

import lombok.extern.slf4j.Slf4j;

/**
 * A plain-OAuth2 provider's user, resolved to a local account. GitHub keeps a member's address private by default
 * and answers {@code null} for {@code email}; the {@code /user/emails} call is what the {@code user:email} scope is for.
 */
@Service
@Slf4j
public class BrokeredOAuth2UserService extends DefaultOAuth2UserService {

    static final String GITHUB_EMAILS = "https://api.github.com/user/emails";

    private final IdentityProviderService providers;

    private final IdentityBrokerService broker;

    private final RestClient http;

    public BrokeredOAuth2UserService(IdentityProviderService providers, IdentityBrokerService broker,
                                     @Qualifier("defaultRestClientBuilder") RestClient.Builder httpBuilder) {
        this.providers = providers;
        this.broker = broker;
        this.http = httpBuilder.build();
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User remote = super.loadUser(userRequest);
        String alias = userRequest.getClientRegistration().getRegistrationId();
        IdentityProvider provider = providers.byAlias(alias).orElseThrow(() -> new OAuth2AuthenticationException(
                new OAuth2Error("idp_unknown", "No such identity provider.", null)));
        Map<String, Object> attributes = new HashMap<>(remote.getAttributes());
        if (provider.getPreset() == IdpPreset.GITHUB && attributes.get(BrokeredAttributes.EMAIL) == null) {
            fetchGithubEmail(userRequest.getAccessToken().getTokenValue()).ifPresent(email -> {
                attributes.put(BrokeredAttributes.EMAIL, email);
                attributes.put(BrokeredAttributes.EMAIL_VERIFIED, true);
            });
        }
        BrokeredIdentity identity = BrokeredAttributes.extract(provider, remote.getName(), attributes);
        BrokerOutcome outcome = broker.resolve(provider, identity);
        if (outcome.needsConfirmation()) {
            throw new BrokerRefusedException(outcome.pending());
        }
        return new BrokeredPrincipal(outcome.user().getUsername(), alias, attributes, null, null);
    }

    /** GitHub's primary, verified address, when the scope allows the call. */
    private java.util.Optional<String> fetchGithubEmail(String accessToken) {
        try {
            List<Map<String, Object>> emails = http.get().uri(GITHUB_EMAILS)
                    .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken))
                    .retrieve().body(new org.springframework.core.ParameterizedTypeReference<>() {
                    });
            if (emails == null) {
                return java.util.Optional.empty();
            }
            return emails.stream()
                    .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
                    .map(e -> String.valueOf(e.get(BrokeredAttributes.EMAIL)))
                    .findFirst();
        } catch (RuntimeException e) {
            log.warn("GitHub /user/emails could not be read: {}", e.getMessage());
            return java.util.Optional.empty();
        }
    }

}
