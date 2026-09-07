package com.asrevo.cvhome.uaa.config;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2TokenEndpointConfigurer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.authentication.AuthenticationConverter;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.config.AuthorizationServerHttpCustomizer;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.token.ImpersonationExchangeConverter;
import com.asrevo.cvhome.sso.token.ImpersonationExchangeProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The grant is wired the way the configurer requires: our converter through {@code accessTokenRequestConverter},
 * which the configurer consults ahead of its defaults, and our provider <em>first</em> in the list, built from the
 * token generator the shared objects hold by the time the providers callback runs.
 */
class ImpersonationGrantCustomizerTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void theConverterIsRegisteredAndTheProviderGoesFirst() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class);
        OAuth2AuthorizationServerConfigurer server = mock(OAuth2AuthorizationServerConfigurer.class);
        OAuth2TokenEndpointConfigurer token = mock(OAuth2TokenEndpointConfigurer.class);
        OAuth2TokenGenerator generator = mock(OAuth2TokenGenerator.class);
        when(http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)).thenReturn(server);
        when(http.getSharedObject(OAuth2TokenGenerator.class)).thenReturn(generator);
        when(server.tokenEndpoint(any())).thenAnswer(invocation -> {
            invocation.<Customizer<OAuth2TokenEndpointConfigurer>>getArgument(0).customize(token);
            return server;
        });
        when(token.accessTokenRequestConverter(any())).thenReturn(token);
        when(token.authenticationProviders(any())).thenReturn(token);

        AuthorizationServerHttpCustomizer customizer = new ImpersonationGrantCustomizer().impersonationGrant(
                mock(OAuth2AuthorizationService.class), mock(UserRepository.class), mock(AuditService.class),
                Clock.systemUTC());
        customizer.customize(http);

        ArgumentCaptor<AuthenticationConverter> converter = ArgumentCaptor.forClass(AuthenticationConverter.class);
        verify(token).accessTokenRequestConverter(converter.capture());
        assertThat(converter.getValue()).isInstanceOf(ImpersonationExchangeConverter.class);

        ArgumentCaptor<Consumer<List<AuthenticationProvider>>> providers = ArgumentCaptor.forClass(Consumer.class);
        verify(token).authenticationProviders(providers.capture());
        List<AuthenticationProvider> defaults = new ArrayList<>(List.of(mock(AuthenticationProvider.class)));
        providers.getValue().accept(defaults);
        assertThat(defaults).hasSize(2);
        assertThat(defaults.getFirst()).isInstanceOf(ImpersonationExchangeProvider.class);
    }

}
