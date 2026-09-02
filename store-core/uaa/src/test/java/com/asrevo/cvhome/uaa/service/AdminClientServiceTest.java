package com.asrevo.cvhome.uaa.service;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.dto.ClientDetails;
import com.asrevo.cvhome.uaa.errors.ClientNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The path decides which client an update writes; the body's own id is ignored.
 */
class AdminClientServiceTest {

    private static final String A = "A";

    private static final String MISSING = "missing";

    private static final String IGNORED = "ignored";

    private static final String RENAMED = "Renamed";

    private static final String KEPT_SECRET = "{bcrypt}kept";

    private static final String GENERATED_SECRET = "{bcrypt}generated";

    private static final String SCOPE = "store_core";

    private final RegisteredClientRepository clients = mock(RegisteredClientRepository.class);

    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    private final AdminClientService service = new AdminClientService(clients, encoder, mock(JdbcTemplate.class),
            mock(AuditService.class));

    private static RegisteredClient existing(String id) {
        return RegisteredClient.withId(id).clientId(String.format("client-%s", id)).clientName(id)
                .clientSecret(KEPT_SECRET)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope(SCOPE)
                .build();
    }

    private static ClientDetails details(String id, String name) {
        return new ClientDetails(id, "client-a", name, Set.of(ClientAuthMethod.CLIENT_SECRET_BASIC),
                Set.of(OAuthGrantType.CLIENT_CREDENTIALS), Set.of(), Set.of(), Set.of(SCOPE), null, null);
    }

    @Test
    void updateWritesThePathClientWhateverTheBodyNames() throws ClientNotFoundException {
        when(clients.findById(A)).thenReturn(existing(A));

        ClientDetails result = service.update(A, details("B", RENAMED));

        ArgumentCaptor<RegisteredClient> saved = ArgumentCaptor.forClass(RegisteredClient.class);
        verify(clients).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(A);
        assertThat(saved.getValue().getClientSecret()).isEqualTo(KEPT_SECRET);
        assertThat(result.id()).isEqualTo(A);
        assertThat(result.clientName()).isEqualTo(RENAMED);
    }

    @Test
    void updateOfAMissingClientIsNotFound() {
        when(clients.findById(MISSING)).thenReturn(null);

        assertThatThrownBy(() -> service.update(MISSING, details(MISSING, "x")))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void createGeneratesAnIdAndASecret() {
        when(encoder.encode(anyString())).thenReturn(GENERATED_SECRET);

        ClientDetails result = service.create(details(IGNORED, "New"));

        ArgumentCaptor<RegisteredClient> saved = ArgumentCaptor.forClass(RegisteredClient.class);
        verify(clients).save(saved.capture());
        assertThat(saved.getValue().getId()).isNotEqualTo(IGNORED);
        assertThat(saved.getValue().getClientSecret()).isEqualTo(GENERATED_SECRET);
        assertThat(result.id()).isEqualTo(saved.getValue().getId());
    }

}
