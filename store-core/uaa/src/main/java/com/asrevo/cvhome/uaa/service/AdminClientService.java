package com.asrevo.cvhome.uaa.service;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.audit.AuditTargetType;
import com.asrevo.cvhome.uaa.dto.ClientDetails;
import com.asrevo.cvhome.uaa.dto.ClientSummary;
import com.asrevo.cvhome.uaa.errors.ClientNotFoundException;
import com.asrevo.cvhome.uaa.mapper.ClientClientDetailsMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminClientService {

    private final RegisteredClientRepository clients;

    private final PasswordEncoder encoder;

    private final JdbcTemplate jdbc;

    private final AuditService audit;

    private final StringKeyGenerator secretGenerator = new Base64StringKeyGenerator(
            Base64.getUrlEncoder().withoutPadding(), 32);

    public Page<ClientSummary> listClients(Pageable pageable) {
        long total = Optional.ofNullable(
                jdbc.queryForObject("select count(*) from oauth2_registered_client", Long.class)
        ).orElse(0L);

        var items = jdbc.query(
                "select id, client_id, client_name from oauth2_registered_client order by id limit ? offset ?",
                (rs, rowNum) -> new ClientSummary(rs.getString(1), rs.getString(2), rs.getString(3)),
                pageable.getPageSize(), pageable.getOffset()
        );

        return new PageImpl<>(items, pageable, total);
    }

    public boolean delete(String id) {
        RegisteredClient existing = clients.findById(id);
        int updatedRows = jdbc.update("delete from oauth2_registered_client where id=?", id);
        if (updatedRows > 0 && existing != null) {
            audit.record(AuditRecord.of(AuditEventType.CLIENT_DELETED).client(existing.getClientId())
                    .target(AuditTargetType.CLIENT, id, existing.getClientId())
                    .change(ClientClientDetailsMapper.toClientDetails(existing), null));
        }
        return updatedRows > 0;
    }

    public ClientDetails findById(String id) throws ClientNotFoundException {
        RegisteredClient client = this.clients.findById(id);
        if (Objects.isNull(client)) {
            throw ClientNotFoundException.of(id);
        }
        return ClientClientDetailsMapper.toClientDetails(client);
    }

    /** Registers a new client. The body's {@code id} is ignored: the registration id is always generated here. */
    public ClientDetails create(ClientDetails details) {
        ClientDetails newClientDetails = withId(UUID.randomUUID().toString(), details);
        RegisteredClient newClient = ClientClientDetailsMapper.toRegisteredClient(newClientDetails);
        if (!StringUtils.hasText(newClient.getClientSecret())) {
            newClient = RegisteredClient.from(newClient)
                    .clientSecret(encoder.encode(secretGenerator.generateKey()))
                    .build();
        }
        clients.save(newClient);
        ClientDetails created = ClientClientDetailsMapper.toClientDetails(newClient);
        audit.record(AuditRecord.of(AuditEventType.CLIENT_CREATED).client(created.clientId())
                .target(AuditTargetType.CLIENT, created.id(), created.clientId()).change(null, created));
        return created;
    }

    /**
     * Updates the client at {@code id}. The path decides which registration is written — the body's own {@code id}
     * is ignored, so a request to {@code PUT /clients/A} can no longer rewrite client B by naming it in the payload.
     */
    public ClientDetails update(String id, ClientDetails details) throws ClientNotFoundException {
        RegisteredClient existingClient = clients.findById(id);
        if (existingClient == null) {
            throw ClientNotFoundException.of(id);
        }
        RegisteredClient updatedClient = ClientClientDetailsMapper.toRegisteredClient(withId(id, details), existingClient);
        clients.save(updatedClient);
        ClientDetails after = ClientClientDetailsMapper.toClientDetails(updatedClient);
        audit.record(AuditRecord.of(AuditEventType.CLIENT_UPDATED).client(after.clientId())
                .target(AuditTargetType.CLIENT, id, after.clientId())
                .change(ClientClientDetailsMapper.toClientDetails(existingClient), after));
        return after;
    }

    private static ClientDetails withId(String id, ClientDetails details) {
        return new ClientDetails(id, details.clientId(), details.clientName(), details.clientAuthenticationMethods(),
                details.authorizationGrantTypes(), details.redirectUris(), details.postLogoutRedirectUris(),
                details.scopes(), details.clientSettings(), details.tokenSettings());
    }

    /**
     * Rotates a client's secret.
     *
     * <p>
     * A missing client is now a 404 rather than a silent success. The previous {@code if (client != null)} answered
     * HTTP 200 without rotating anything, so an operator rotating a secret for a mistyped id was told it had worked.
     * </p>
     */
    public void resetSecret(String id, String newSecret) throws ClientNotFoundException {
        RegisteredClient client = clients.findById(id);
        if (client == null) {
            throw ClientNotFoundException.of(id);
        }
        RegisteredClient updatedClient = RegisteredClient.from(client)
                .clientSecret(encoder.encode(newSecret))
                .build();
        clients.save(updatedClient);
        audit.record(AuditRecord.of(AuditEventType.CLIENT_SECRET_ROTATED).client(client.getClientId())
                .target(AuditTargetType.CLIENT, id, client.getClientId()));
    }

}
