package com.asrevo.cvhome.uaa.security;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.uaa.domain.IdentityProvider;
import com.asrevo.cvhome.uaa.idp.BrokerOutcome;
import com.asrevo.cvhome.uaa.idp.BrokerRefusedException;
import com.asrevo.cvhome.uaa.idp.BrokeredIdentity;
import com.asrevo.cvhome.uaa.idp.IdentityBrokerService;
import com.asrevo.cvhome.uaa.idp.IdentityProviderService;

import lombok.RequiredArgsConstructor;

/** An OpenID Connect provider's user — the id token's claims, plus userinfo when the scope asks — resolved to an account. */
@Service
@RequiredArgsConstructor
public class BrokeredOidcUserService extends OidcUserService {

    private final IdentityProviderService providers;

    private final IdentityBrokerService broker;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser remote = super.loadUser(userRequest);
        String alias = userRequest.getClientRegistration().getRegistrationId();
        IdentityProvider provider = providers.byAlias(alias).orElseThrow(() -> new OAuth2AuthenticationException(
                new OAuth2Error("idp_unknown", "No such identity provider.", null)));
        BrokeredIdentity identity = BrokeredAttributes.extract(provider, remote.getSubject(), remote.getClaims());
        BrokerOutcome outcome = broker.resolve(provider, identity);
        if (outcome.needsConfirmation()) {
            throw new BrokerRefusedException(outcome.pending());
        }
        return new BrokeredPrincipal(outcome.user().getUsername(), alias, remote.getClaims(), remote.getIdToken(),
                remote.getUserInfo());
    }

}
