package com.asrevo.cvhome.sso.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.audit.AuditTargetType;
import com.asrevo.cvhome.sso.client.ClientType;
import com.asrevo.cvhome.sso.client.RedirectUriRules;
import com.asrevo.cvhome.sso.domain.ClientExtension;
import com.asrevo.cvhome.sso.domain.ClientSecretHistory;
import com.asrevo.cvhome.sso.dto.ClientDetails;
import com.asrevo.cvhome.sso.dto.ClientSearch;
import com.asrevo.cvhome.sso.dto.ClientStats;
import com.asrevo.cvhome.sso.dto.ClientStatus;
import com.asrevo.cvhome.sso.dto.ClientSummary;
import com.asrevo.cvhome.sso.dto.CreatedClient;
import com.asrevo.cvhome.sso.dto.RotatedSecret;
import com.asrevo.cvhome.sso.mapper.ClientClientDetailsMapper;
import com.asrevo.cvhome.sso.repo.ClientExtensionRepository;
import com.asrevo.cvhome.sso.repo.ClientSecretHistoryRepository;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.sso.token.TokenRevocationService;
import com.asrevo.cvhome.uaa.errors.ClientIdTakenException;
import com.asrevo.cvhome.uaa.errors.ClientNoPreviousSecretException;
import com.asrevo.cvhome.uaa.errors.ClientNotConfidentialException;
import com.asrevo.cvhome.uaa.errors.ClientNotFoundException;
import com.asrevo.cvhome.uaa.errors.ClientTokenTtlExceedsPolicyException;
import com.asrevo.cvhome.uaa.errors.InvalidRedirectUriException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The client registry, administered.
 *
 * <p>
 * Spring's {@code oauth2_registered_client} row is the registration; {@code client_extension} is what uaa adds to it
 * (enabled, description, last token) and {@code client_secret_history} is the grace window. A secret is answered
 * exactly once — at creation and at rotation — and never read back; the realm's settings decide how long a new one
 * lives and how long the one it replaced keeps working.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminClientService {

    static final Duration EXPIRING_SOON = Duration.ofDays(30);

    private static final String LIST = """
            select c.id, c.client_id, c.client_name, c.client_authentication_methods, c.authorization_grant_types,
                   c.client_secret_expires_at, coalesce(e.enabled, true), e.last_token_issued_at
            from oauth2_registered_client c
            left join client_extension e on e.registered_client_id = c.id
            order by c.client_id""";

    private static final String DELETE = "delete from oauth2_registered_client where id = ?";

    private static final String GRACE_UNTIL = "previous secret valid until %s";

    private final RegisteredClientRepository clients;

    private final PasswordEncoder encoder;

    private final JdbcTemplate jdbc;

    private final AuditService audit;

    private final ClientExtensionRepository extensions;

    private final ClientSecretHistoryRepository history;

    private final SettingsService settings;

    private final RedirectUriRules redirectUriRules;

    private final TokenRevocationService revocation;

    private final Clock clock;

    private final StringKeyGenerator secretGenerator = new Base64StringKeyGenerator(
            Base64.getUrlEncoder().withoutPadding(), 32);

    // ---------------------------------------------------------------- read

    /**
     * The list, filtered and paged in memory: a realm has tens of clients, and the type is derived from two columns
     * rather than stored, so a SQL filter on it would have to re-derive it in the query.
     */
    public Page<ClientSummary> listClients(ClientSearch search, Pageable pageable) {
        List<ClientSummary> all = summaries().stream().filter(row -> matches(row, search)).toList();
        int from = (int) Math.min(pageable.getOffset(), all.size());
        int to = (int) Math.min(from + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(from, to), pageable, all.size());
    }

    public ClientStats stats() {
        List<ClientSummary> all = summaries();
        Instant soon = clock.instant().plus(EXPIRING_SOON);
        return new ClientStats(all.size(), all.stream().filter(ClientSummary::enabled).count(),
                all.stream().filter(row -> !row.enabled()).count(), count(all, ClientType.MACHINE),
                count(all, ClientType.CONFIDENTIAL), count(all, ClientType.PUBLIC),
                all.stream().filter(row -> row.clientSecretExpiresAt() != null && row.clientSecretExpiresAt().isBefore(soon))
                        .count());
    }

    private static long count(List<ClientSummary> all, ClientType type) {
        return all.stream().filter(row -> row.type() == type).count();
    }

    private List<ClientSummary> summaries() {
        return jdbc.query(LIST, (rs, rowNum) -> {
            Set<ClientAuthenticationMethod> methods = split(rs.getString(4)).stream()
                    .map(ClientAuthenticationMethod::new).collect(Collectors.toSet());
            Set<String> grants = split(rs.getString(5));
            ClientType type = ClientType.of(methods, grants.stream().map(AuthorizationGrantType::new)
                    .collect(Collectors.toSet()));
            return new ClientSummary(rs.getString(1), rs.getString(2), rs.getString(3), type, rs.getBoolean(7), grants,
                    toInstant(rs.getTimestamp(6)), toInstant(rs.getTimestamp(8)));
        });
    }

    private static Set<String> split(String csv) {
        return csv == null || csv.isBlank() ? Set.of()
                : Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    private static Instant toInstant(java.sql.Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static boolean matches(ClientSummary row, ClientSearch search) {
        if (search.enabled() != null && row.enabled() != search.enabled()) {
            return false;
        }
        if (search.type() != null && row.type() != search.type()) {
            return false;
        }
        if (search.q() == null || search.q().isBlank()) {
            return true;
        }
        String q = search.q().trim().toLowerCase(Locale.ROOT);
        return row.clientId().toLowerCase(Locale.ROOT).contains(q)
                || row.clientName() != null && row.clientName().toLowerCase(Locale.ROOT).contains(q);
    }

    public ClientDetails findById(String id) throws ClientNotFoundException {
        return details(findOrThrow(id));
    }

    private RegisteredClient findOrThrow(String id) throws ClientNotFoundException {
        RegisteredClient client = clients.findById(id);
        if (client == null) {
            throw ClientNotFoundException.of(id);
        }
        return client;
    }

    /** The registration with uaa's status attached. */
    ClientDetails details(RegisteredClient client) {
        Optional<ClientExtension> extension = extensions.findById(client.getId());
        Instant now = clock.instant();
        Instant graceUntil = history.findByRegisteredClientIdAndRevokedAtIsNull(client.getId()).stream()
                .filter(row -> row.live(now)).map(ClientSecretHistory::getExpiresAt).max(Comparator.naturalOrder())
                .orElse(null);
        ClientStatus status = new ClientStatus(extension.map(ClientExtension::getDescription).orElse(null),
                extension.map(ClientExtension::isEnabled).orElse(true), ClientType.of(client),
                client.getClientIdIssuedAt(), client.getClientSecretExpiresAt(),
                extension.map(ClientExtension::getLastTokenIssuedAt).orElse(null),
                extension.map(ClientExtension::getDisabledAt).orElse(null),
                extension.map(ClientExtension::getDisabledBy).orElse(null), graceUntil);
        return ClientClientDetailsMapper.toClientDetails(client).withStatus(status);
    }

    // ---------------------------------------------------------------- write

    /**
     * Registers a client. The body's {@code id} is ignored (always generated), and the secret is generated here and
     * answered once: a public client gets none.
     */
    @Transactional
    public CreatedClient create(ClientDetails details)
            throws ClientIdTakenException, InvalidRedirectUriException, ClientTokenTtlExceedsPolicyException {
        if (clients.findByClientId(details.clientId()) != null || existsByClientId(details.clientId())) {
            throw ClientIdTakenException.of(details.clientId());
        }
        validate(details);
        Instant now = clock.instant();
        RegisteredClient draft = ClientClientDetailsMapper.toRegisteredClient(details.withId(UUID.randomUUID().toString()));
        ClientType type = ClientType.of(draft);
        String secret = type.holdsSecret() ? secretGenerator.generateKey() : null;
        RegisteredClient.Builder builder = RegisteredClient.from(draft).clientIdIssuedAt(now);
        if (secret != null) {
            builder.clientSecret(encoder.encode(secret)).clientSecretExpiresAt(secretExpiry(now));
        }
        RegisteredClient created = builder.build();
        clients.save(created);
        extensions.save(ClientExtension.create(created.getId(), description(details), now));
        ClientDetails view = details(created);
        audit.record(AuditRecord.of(AuditEventType.CLIENT_CREATED).client(view.clientId())
                .target(AuditTargetType.CLIENT, view.id(), view.clientId()).change(null, view));
        return new CreatedClient(view, secret);
    }

    /** A disabled client is invisible to {@code findByClientId}, so the uniqueness check has to read the table. */
    private boolean existsByClientId(String clientId) {
        Integer n = jdbc.queryForObject("select count(*) from oauth2_registered_client where client_id = ?",
                Integer.class, clientId);
        return n != null && n > 0;
    }

    /**
     * Updates the client at {@code id}. The path decides which registration is written — the body's own {@code id}
     * is ignored, so a request to {@code PUT /clients/A} can no longer rewrite client B by naming it in the payload.
     */
    @Transactional
    public ClientDetails update(String id, ClientDetails details)
            throws ClientNotFoundException, InvalidRedirectUriException, ClientTokenTtlExceedsPolicyException {
        RegisteredClient existing = findOrThrow(id);
        validate(details);
        ClientDetails before = details(existing);
        RegisteredClient updated = ClientClientDetailsMapper.toRegisteredClient(details.withId(id), existing);
        clients.save(updated);
        ClientExtension extension = extensionOf(updated);
        extension.setDescription(description(details));
        extension.setUpdatedAt(clock.instant());
        extensions.save(extension);
        ClientDetails after = details(updated);
        audit.record(AuditRecord.of(AuditEventType.CLIENT_UPDATED).client(after.clientId())
                .target(AuditTargetType.CLIENT, id, after.clientId()).change(before, after));
        return after;
    }

    private void validate(ClientDetails details) throws InvalidRedirectUriException, ClientTokenTtlExceedsPolicyException {
        redirectUriRules.validate(details.redirectUris());
        redirectUriRules.validate(details.postLogoutRedirectUris());
        Duration requested = details.tokenSettings() == null ? null : details.tokenSettings().accessTokenTimeToLive();
        Duration max = Duration.ofSeconds(settings.current().tokens().maxAccessTokenTtlSeconds());
        if (requested != null && requested.compareTo(max) > 0) {
            throw ClientTokenTtlExceedsPolicyException.of(requested, max);
        }
    }

    private static String description(ClientDetails details) {
        return details.status() == null ? null : details.status().description();
    }

    /** Deletes the registration, its tokens first: a token for a client that no longer exists is a row nothing can read. */
    @Transactional
    public boolean delete(String id) {
        RegisteredClient existing = clients.findById(id);
        if (existing == null) {
            return false;
        }
        ClientDetails before = details(existing);
        revocation.revokeAllForClient(id, existing.getClientId());
        int updatedRows = jdbc.update(DELETE, id);
        if (updatedRows > 0) {
            audit.record(AuditRecord.of(AuditEventType.CLIENT_DELETED).client(existing.getClientId())
                    .target(AuditTargetType.CLIENT, id, existing.getClientId()).change(before, null));
        }
        return updatedRows > 0;
    }

    @Transactional
    public ClientDetails enable(String id) throws ClientNotFoundException {
        RegisteredClient client = findOrThrow(id);
        ClientExtension extension = extensionOf(client);
        extension.enable(clock.instant());
        extensions.save(extension);
        audit.record(AuditRecord.of(AuditEventType.CLIENT_ENABLED).client(client.getClientId())
                .target(AuditTargetType.CLIENT, id, client.getClientId()));
        return details(client);
    }

    /** Disabling also revokes every authorization the client holds; a disabled client's tokens must not keep working. */
    @Transactional
    public ClientDetails disable(String id, String by) throws ClientNotFoundException {
        RegisteredClient client = findOrThrow(id);
        ClientExtension extension = extensionOf(client);
        extension.disable(clock.instant(), by);
        extensions.save(extension);
        int revoked = revocation.revokeAllForClient(id, client.getClientId());
        audit.record(AuditRecord.of(AuditEventType.CLIENT_DISABLED).client(client.getClientId())
                .target(AuditTargetType.CLIENT, id, client.getClientId())
                .detail(String.format("%d authorization(s) revoked", revoked)));
        return details(client);
    }

    private ClientExtension extensionOf(RegisteredClient client) {
        return extensions.findById(client.getId())
                .orElseGet(() -> ClientExtension.create(client.getId(), null, clock.instant()));
    }

    /**
     * Rotates a client's secret: a fresh random one, valid for the realm's validity period, while the one it replaces
     * keeps authenticating for the realm's grace window. Answered once.
     */
    @Transactional
    public RotatedSecret rotateSecret(String id) throws ClientNotFoundException, ClientNotConfidentialException {
        RegisteredClient client = findOrThrow(id);
        if (!ClientType.of(client).holdsSecret()) {
            throw ClientNotConfidentialException.of(client.getClientId());
        }
        Instant now = clock.instant();
        RealmSettings.Tokens tokens = settings.current().tokens();
        Instant graceUntil = null;
        if (client.getClientSecret() != null && tokens.clientSecretGraceHours() > 0) {
            graceUntil = now.plus(tokens.clientSecretGraceHours(), ChronoUnit.HOURS);
            retireLive(id, now);
            history.save(ClientSecretHistory.retire(id, client.getClientSecret(), now, graceUntil));
        }
        String secret = secretGenerator.generateKey();
        Instant expiresAt = secretExpiry(now);
        clients.save(RegisteredClient.from(client).clientSecret(encoder.encode(secret)).clientSecretExpiresAt(expiresAt)
                .build());
        audit.record(AuditRecord.of(AuditEventType.CLIENT_SECRET_ROTATED).client(client.getClientId())
                .target(AuditTargetType.CLIENT, id, client.getClientId())
                .detail(graceUntil == null ? "no grace window" : String.format(GRACE_UNTIL, graceUntil)));
        return new RotatedSecret(id, client.getClientId(), secret, expiresAt, graceUntil);
    }

    /** Every secret-holding client at once — incident response, after which every integration must be reconfigured. */
    @Transactional
    public List<RotatedSecret> rotateAll() throws ClientNotFoundException, ClientNotConfidentialException {
        List<RotatedSecret> rotated = new ArrayList<>();
        for (ClientSummary row : summaries()) {
            if (row.type().holdsSecret()) {
                rotated.add(rotateSecret(row.id()));
            }
        }
        return rotated;
    }

    /** Ends the grace window now: the previous secret stops authenticating immediately. */
    @Transactional
    public void revokePreviousSecret(String id) throws ClientNotFoundException, ClientNoPreviousSecretException {
        RegisteredClient client = findOrThrow(id);
        Instant now = clock.instant();
        if (retireLive(id, now) == 0) {
            throw ClientNoPreviousSecretException.of(client.getClientId());
        }
        audit.record(AuditRecord.of(AuditEventType.CLIENT_SECRET_ROTATED).client(client.getClientId())
                .target(AuditTargetType.CLIENT, id, client.getClientId()).detail("previous secret revoked early"));
    }

    private int retireLive(String id, Instant now) {
        int retired = 0;
        for (ClientSecretHistory row : history.findByRegisteredClientIdAndRevokedAtIsNull(id)) {
            if (row.live(now)) {
                row.setRevokedAt(now);
                history.save(row);
                retired++;
            }
        }
        return retired;
    }

    /**
     * Sets a secret the operator chose, with no grace window: the alias the SDK and the older console call. A missing
     * client is a 404 rather than a silent success — the previous {@code if (client != null)} answered 200 without
     * rotating anything.
     */
    @Transactional
    public void resetSecret(String id, String newSecret) throws ClientNotFoundException, ClientNotConfidentialException {
        RegisteredClient client = findOrThrow(id);
        if (!ClientType.of(client).holdsSecret()) {
            throw ClientNotConfidentialException.of(client.getClientId());
        }
        Instant now = clock.instant();
        retireLive(id, now);
        clients.save(RegisteredClient.from(client).clientSecret(encoder.encode(newSecret))
                .clientSecretExpiresAt(secretExpiry(now)).build());
        audit.record(AuditRecord.of(AuditEventType.CLIENT_SECRET_ROTATED).client(client.getClientId())
                .target(AuditTargetType.CLIENT, id, client.getClientId()).detail("secret set by an operator"));
    }

    private Instant secretExpiry(Instant now) {
        int days = settings.current().tokens().clientSecretValidityDays();
        return days > 0 ? now.plus(days, ChronoUnit.DAYS) : null;
    }

    /** The scopes a registration may ask for, for the form. */
    public Map<String, Object> scopeCatalogue() {
        return Map.of("scopes", List.of("openid", "profile", "email", "api.read", "store_core", "store_pod", "super_admin"));
    }

}
