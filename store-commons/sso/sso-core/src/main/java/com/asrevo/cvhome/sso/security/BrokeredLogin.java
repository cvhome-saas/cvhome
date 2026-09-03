package com.asrevo.cvhome.sso.security;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.sso.idp.DynamicClientRegistrationRepository;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** The six pieces {@code oauth2Login} is wired from, so the security chain takes one parameter for them. */
@Component
@RequiredArgsConstructor
@Getter
public class BrokeredLogin {

    private final BrokeredLoginSuccessHandler success;

    private final IdpLoginFailureHandler failure;

    private final BrokeredOAuth2UserService oauth2Users;

    private final BrokeredOidcUserService oidcUsers;

    private final DynamicClientRegistrationRepository registrations;

    private final IdentityProviderService providers;

    public LoginHintAuthorizationRequestResolver resolver() {
        return new LoginHintAuthorizationRequestResolver(registrations, providers);
    }

}
