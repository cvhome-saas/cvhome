package com.asrevo.cvhome.cua.config;

import java.util.Map;
import java.util.function.Function;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import lombok.Getter;

@Getter
public enum SocialProvider {

    GOOGLE("google", "Google",
            registrationId -> ClientRegistrations.fromOidcIssuerLocation("https://accounts.google.com")
                    .registrationId(registrationId)
                    .clientName("GoogleClient")
                    .redirectUri(Constants.DEFAULT_REDIRECT_URI)
                    .scope(Constants.OPENID_KEY, Constants.PROFILE_KEY, Constants.EMAIL_KEY)),

    FACEBOOK("facebook", "Facebook",
            registrationId -> ClientRegistration.withRegistrationId(registrationId)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationUri("https://www.facebook.com/v19.0/dialog/oauth")
                    .tokenUri("https://graph.facebook.com/v19.0/oauth/access_token")
                    .userInfoUri("https://graph.facebook.com/me?fields=id,name,email,picture")
                    .userNameAttributeName(Constants.USER_NAME_ATTR_ID)
                    .clientName("FacebookClient")
                    .redirectUri(Constants.DEFAULT_REDIRECT_URI)
                    .scope(Constants.EMAIL_KEY, Constants.PUBLIC_PROFILE_KEY)),

    GITHUB("github", "GitHub",
            registrationId -> ClientRegistration.withRegistrationId(registrationId)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationUri("https://github.com/login/oauth/authorize")
                    .tokenUri("https://github.com/login/oauth/access_token")
                    .userInfoUri("https://api.github.com/user")
                    .userNameAttributeName(Constants.USER_NAME_ATTR_ID)
                    .clientName("GitHubClient")
                    .redirectUri(Constants.DEFAULT_REDIRECT_URI)
                    .scope(Constants.READ_USER_KEY, Constants.USER_EMAIL_KEY));

    private static final String SPACE_SEPARATOR = " ";

    private static final String NAME_KEY = "name";

    private static final String EMAIL_KEY = "email";

    private static final String GIVEN_NAME_KEY = "given_name";

    private static final String FAMILY_NAME_KEY = "family_name";

    private static final String LOGIN_KEY = "login";

    private final String providerId;

    private final String clientName;

    private final Function<String, ClientRegistration.Builder> builderFunction;

    SocialProvider(String providerId, String clientName, Function<String, ClientRegistration.Builder> builderFunction) {
        this.providerId = providerId;
        this.clientName = clientName;
        this.builderFunction = builderFunction;

    }

    public ClientRegistration.Builder createBuilder(String registrationId) {
        return builderFunction.apply(registrationId);
    }

    public ExtractedAttributes extractAttributes(Map<String, Object> attributes) {
        String name = null;
        String email = null;
        String phone = null;
        String firstname = null;
        String lastname = null;

        switch (this) {
            case GOOGLE -> {
                name = (String) attributes.get(NAME_KEY);
                email = (String) attributes.get(EMAIL_KEY);
                firstname = (String) attributes.get(GIVEN_NAME_KEY);
                lastname = (String) attributes.get(FAMILY_NAME_KEY);
            }

            case FACEBOOK -> {
                name = (String) attributes.get(NAME_KEY);
                email = (String) attributes.get(EMAIL_KEY);

                // Facebook does not always split names
                if (name != null) {
                    String[] parts = name.split(SPACE_SEPARATOR, 2);
                    firstname = parts[0];
                    lastname = parts.length > 1 ? parts[1] : null;
                }
            }

            case GITHUB -> {
                name = (String) attributes.get(NAME_KEY);
                email = (String) attributes.get(EMAIL_KEY);

                // GitHub may not provide name → fallback to login
                if (name == null) {
                    name = (String) attributes.get(LOGIN_KEY);
                }

                if (name != null) {
                    String[] parts = name.split(SPACE_SEPARATOR, 2);
                    firstname = parts[0];
                    lastname = parts.length > 1 ? parts[1] : null;
                }
            }

            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
        if (email == null) {
            email = name;
        }

        return new ExtractedAttributes(name, email, phone, firstname, lastname);
    }

    public record ExtractedAttributes(String username, String email, String phone, String firstname, String lastname) {

    }

}
