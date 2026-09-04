-- The SSO server's schema, shared by both deployments.
--
-- Source of truth for store-core/uaa and store-pod/cua alike: each shell's init-sql/schema.sql is generated from
-- this file at build time, with @@SCHEMA@@ replaced by that deployment's schema name. Two hand-maintained copies
-- is how the two servers drifted apart in the first place, and a column added to one and not the other fails only
-- on the deployment nobody was testing.
--
-- Not production yet: drop and recreate. No ALTERs, no migrations — a developer resets with
-- `drop schema <name> cascade` and restarts.

create schema if not exists @@SCHEMA@@;
-- The realms this deployment serves. uaa has exactly one row and always will; cua has one per store, written
-- when the store is provisioned.
--
-- It exists so that "is this a store we serve?" has an answer that is not "did anyone happen to create a row in
-- some other table". cua used to synthesise an OAuth2 client for any client_id presented, which meant an unknown
-- store still got a working client and was only stopped later, by the user lookup finding nobody.
create table if not exists @@SCHEMA@@.realms
(
    id           varchar(64) primary key,
    display_name varchar(190),
    enabled      boolean     not null default true,
    created_at   timestamptz not null default current_timestamp
);

create table if not exists @@SCHEMA@@.oauth2_registered_client
(
    id                            varchar(100) primary key,
    client_id                     varchar(100)  not null unique,
    client_id_issued_at           timestamp     not null,
    client_secret                 varchar(200),
    client_secret_expires_at      timestamp,
    client_name                   varchar(200)  not null,
    client_authentication_methods varchar(1000) not null,
    authorization_grant_types     varchar(1000) not null,
    redirect_uris                 varchar(1000),
    post_logout_redirect_uris     varchar(1000),
    scopes                        varchar(1000) not null,
    client_settings               varchar(2000) not null,
    token_settings                varchar(2000) not null
);

-- What the server knows about a registered client beyond what Spring's table holds: whether it may authenticate at all, a
-- description, and when it last obtained a token. One row per registration, created with it.
create table if not exists @@SCHEMA@@.client_extension
(
    realm_id varchar(64) not null default 'platform',
    registered_client_id varchar(100) primary key references @@SCHEMA@@.oauth2_registered_client (id) on delete cascade,
    enabled              boolean     not null default true,
    description          varchar(500),
    disabled_at          timestamptz,
    disabled_by          varchar(200),
    last_token_issued_at timestamptz,
    created_at           timestamptz not null default now(),
    updated_at           timestamptz not null default now()
);

-- A rotated-out secret that still authenticates for a grace window, so an integration can pick up the new one
-- without an outage. Hashed like the live secret; revoked early by an operator or retired by expiry.
create table if not exists @@SCHEMA@@.client_secret_history
(
    realm_id varchar(64) not null default 'platform',
    id                   uuid primary key,
    registered_client_id varchar(100) not null references @@SCHEMA@@.oauth2_registered_client (id) on delete cascade,
    secret_hash          varchar(200) not null,
    created_at           timestamptz  not null,
    expires_at           timestamptz  not null,
    revoked_at           timestamptz
);

create index if not exists idx_client_secret_history_client on @@SCHEMA@@.client_secret_history (registered_client_id);

-- oauth2_authorization
-- Spring Authorization Server's own table, in its Postgres shape: token values and metadata are text (the shipped
-- schema's blob), instants are timestamptz, and the device-flow columns are present because the JDBC service
-- inserts every column whether or not the grant is enabled.
create table if not exists @@SCHEMA@@.oauth2_authorization
(
    id                            varchar(100) primary key,
    registered_client_id          varchar(100) not null,
    principal_name                varchar(200) not null,
    authorization_grant_type      varchar(100) not null,
    authorized_scopes             varchar(1000),
    attributes                    text,
    state                         varchar(500),
    authorization_code_value      text,
    authorization_code_issued_at  timestamptz,
    authorization_code_expires_at timestamptz,
    authorization_code_metadata   text,
    access_token_value            text,
    access_token_issued_at        timestamptz,
    access_token_expires_at       timestamptz,
    access_token_metadata         text,
    access_token_type             varchar(100),
    access_token_scopes           varchar(1000),
    oidc_id_token_value           text,
    oidc_id_token_issued_at       timestamptz,
    oidc_id_token_expires_at      timestamptz,
    oidc_id_token_metadata        text,
    refresh_token_value           text,
    refresh_token_issued_at       timestamptz,
    refresh_token_expires_at      timestamptz,
    refresh_token_metadata        text,
    user_code_value               text,
    user_code_issued_at           timestamptz,
    user_code_expires_at          timestamptz,
    user_code_metadata            text,
    device_code_value             text,
    device_code_issued_at         timestamptz,
    device_code_expires_at        timestamptz,
    device_code_metadata          text,
    foreign key (registered_client_id) references @@SCHEMA@@.oauth2_registered_client (id)
);

create index if not exists idx_oauth2_authorization_principal on @@SCHEMA@@.oauth2_authorization (principal_name);
create index if not exists idx_oauth2_authorization_client on @@SCHEMA@@.oauth2_authorization (registered_client_id);

-- oauth2_authorization_consent
create table if not exists @@SCHEMA@@.oauth2_authorization_consent
(
    registered_client_id varchar(100)  not null,
    principal_name       varchar(200)  not null,
    authorities          varchar(1000) not null,
    primary key (registered_client_id, principal_name),
    foreign key (registered_client_id) references @@SCHEMA@@.oauth2_registered_client (id)
);


-- Users table
create table if not exists @@SCHEMA@@.users
(
    id                     uuid PRIMARY KEY,
    realm_id               varchar(64)  NOT NULL DEFAULT 'platform',
    email                  VARCHAR(254) NOT NULL,
    username               VARCHAR(190) NOT NULL,
    first_name             VARCHAR(50),
    last_name              VARCHAR(50),
    password_hash          VARCHAR(100),
    metadata               jsonb                 DEFAULT '{}'::jsonb,
    enabled                BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified         BOOLEAN      NOT NULL DEFAULT FALSE,
    activated_at           timestamptz,
    failed_login_attempts  INT          NOT NULL DEFAULT 0,
    lockout_count          INT          NOT NULL DEFAULT 0,
    locked_until           timestamptz,
    locked_permanently     BOOLEAN      NOT NULL DEFAULT FALSE,
    password_changed_at    timestamptz,
    last_sign_in_at        timestamptz,
    last_sign_in_client_id VARCHAR(100),
    last_sign_in_ip        VARCHAR(45),
    last_sign_in_via       VARCHAR(60),
    created_at             timestamptz  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             timestamptz  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Scoped to the realm, not global: one email address is a different person in each store cua serves.
    constraint uk_users_realm_email unique (realm_id, email),
    constraint uk_users_realm_username unique (realm_id, username)
);

CREATE INDEX IF NOT EXISTS idx_users_metadata ON @@SCHEMA@@.users USING gin (metadata);
CREATE INDEX IF NOT EXISTS idx_users_email_lower ON @@SCHEMA@@.users (lower(email));
CREATE INDEX IF NOT EXISTS idx_users_username_lower ON @@SCHEMA@@.users (lower(username));

-- Roles table
create table if not exists @@SCHEMA@@.roles
(
    id               uuid PRIMARY KEY,
    realm_id         varchar(64) NOT NULL DEFAULT 'platform',
    name             VARCHAR(80) NOT NULL,
    description      VARCHAR(255),
    scope            VARCHAR(20) NOT NULL DEFAULT 'REALM',
    system_role      BOOLEAN     NOT NULL DEFAULT FALSE,
    inherits_from_id uuid REFERENCES @@SCHEMA@@.roles (id),
    created_at       timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       timestamptz,
    constraint uk_roles_realm_name unique (realm_id, name),
    constraint roles_scope_ck check (scope in ('REALM', 'ORGANIZATION', 'CLIENT'))
);

-- The permissions a role grants; the effective set of a user is the union over their roles and what those inherit.
create table if not exists @@SCHEMA@@.role_permissions
(
    role_id    uuid        NOT NULL REFERENCES @@SCHEMA@@.roles (id) ON DELETE CASCADE,
    permission VARCHAR(80) NOT NULL,
    PRIMARY KEY (role_id, permission)
);

-- User-Roles join table
create table if not exists @@SCHEMA@@.user_roles
(
    user_id uuid NOT NULL,
    role_id uuid NOT NULL,
    PRIMARY KEY (user_id, role_id),
    constraint FKh8ciramu9cc9q3qcqiv4ue8a6 foreign key (role_id) references @@SCHEMA@@.roles,
    constraint FKhfh9dx7w3ubf1co1vdev94g3f foreign key (user_id) references @@SCHEMA@@.users
);

-- The previous hashes a user may not reuse; trimmed to settings.password_history_count on every change.
create table if not exists @@SCHEMA@@.password_history
(
    realm_id varchar(64) not null default 'platform',
    id            uuid PRIMARY KEY,
    user_id       uuid         NOT NULL REFERENCES @@SCHEMA@@.users (id) ON DELETE CASCADE,
    password_hash VARCHAR(100) NOT NULL,
    created_at    timestamptz  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_password_history_user ON @@SCHEMA@@.password_history (user_id, created_at DESC);

-- A pending account's one-time invitation. Only the token's hash is stored; the token itself is returned once to the
-- administrator and handed to the delivery consumer through the outbox. One live invitation per account.
create table if not exists @@SCHEMA@@.invitations
(
    realm_id varchar(64) not null default 'platform',
    id          uuid PRIMARY KEY,
    user_id     uuid         NOT NULL REFERENCES @@SCHEMA@@.users (id) ON DELETE CASCADE,
    email       VARCHAR(254) NOT NULL,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    status      VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    expires_at  timestamptz  NOT NULL,
    created_at  timestamptz  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(190),
    accepted_at timestamptz,
    constraint invitations_status_ck check (status in ('PENDING', 'ACCEPTED', 'REVOKED', 'EXPIRED'))
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_invitations_one_pending ON @@SCHEMA@@.invitations (user_id) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_invitations_status ON @@SCHEMA@@.invitations (status, created_at DESC);

-- An administrator-issued password-reset link, same handling as an invitation: hash only, used once, short-lived.
create table if not exists @@SCHEMA@@.password_reset_tokens
(
    realm_id varchar(64) not null default 'platform',
    id         uuid PRIMARY KEY,
    user_id    uuid        NOT NULL REFERENCES @@SCHEMA@@.users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(190),
    used_at    timestamptz,
    revoked_at timestamptz
);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user ON @@SCHEMA@@.password_reset_tokens (user_id, created_at DESC);

-- Realm-wide policy: exactly one row.
create table if not exists @@SCHEMA@@.settings
(
    -- One row per realm, keyed by it. A singleton (id = 1) was as far as one deployment's policy went; with a
    -- realm per store there is nowhere for a store's own policy to live.
    realm_id                          varchar(64)  PRIMARY KEY,
    display_name                      VARCHAR(100) NOT NULL DEFAULT 'cvhome ID',
    support_email                     VARCHAR(254),
    default_locale                    VARCHAR(10)  NOT NULL DEFAULT 'en',
    self_registration_enabled         BOOLEAN      NOT NULL DEFAULT FALSE,
    require_email_verification        BOOLEAN      NOT NULL DEFAULT FALSE,
    password_min_length               INT          NOT NULL DEFAULT 12,
    password_require_upper            BOOLEAN      NOT NULL DEFAULT TRUE,
    password_require_lower            BOOLEAN      NOT NULL DEFAULT TRUE,
    password_require_digit            BOOLEAN      NOT NULL DEFAULT TRUE,
    password_require_special          BOOLEAN      NOT NULL DEFAULT FALSE,
    password_history_count            INT          NOT NULL DEFAULT 5,
    password_expiry_days              INT          NOT NULL DEFAULT 0,
    password_hibp_check               BOOLEAN      NOT NULL DEFAULT FALSE,
    lockout_threshold                 INT          NOT NULL DEFAULT 5,
    lockout_duration_seconds          INT          NOT NULL DEFAULT 900,
    lockout_permanent_after           INT          NOT NULL DEFAULT 5,
    session_idle_seconds              INT          NOT NULL DEFAULT 1800,
    session_max_seconds               INT          NOT NULL DEFAULT 43200,
    remember_me_enabled               BOOLEAN      NOT NULL DEFAULT FALSE,
    remember_me_seconds               INT          NOT NULL DEFAULT 2592000,
    single_session_per_user           BOOLEAN      NOT NULL DEFAULT FALSE,
    max_access_token_ttl_seconds      INT          NOT NULL DEFAULT 3600,
    default_access_token_ttl_seconds  INT          NOT NULL DEFAULT 900,
    default_refresh_token_ttl_seconds INT          NOT NULL DEFAULT 43200,
    client_secret_validity_days       INT          NOT NULL DEFAULT 365,
    client_secret_grace_hours         INT          NOT NULL DEFAULT 24,
    key_rotation_days                 INT          NOT NULL DEFAULT 90,
    key_retire_days                   INT          NOT NULL DEFAULT 7,
    audit_retention_days              INT          NOT NULL DEFAULT 365,
    updated_at                        timestamptz,
    updated_by                        VARCHAR(190),
    version                           BIGINT       NOT NULL DEFAULT 0
);

-- Append-only record of every authentication and administrative event. Never updated, trimmed by retention.
create table if not exists @@SCHEMA@@.audit_events
(
    realm_id varchar(64) not null default 'platform',
    id          BIGSERIAL PRIMARY KEY,
    occurred_at timestamptz  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_type  VARCHAR(60)  NOT NULL,
    outcome     VARCHAR(10)  NOT NULL,
    reason_code VARCHAR(60),
    actor_type  VARCHAR(10)  NOT NULL,
    actor_id    VARCHAR(190),
    actor_name  VARCHAR(190),
    target_type VARCHAR(20),
    target_id   VARCHAR(190),
    target_name VARCHAR(190),
    client_id   VARCHAR(100),
    ip          VARCHAR(45),
    user_agent  VARCHAR(512),
    before_json jsonb,
    after_json  jsonb,
    detail      VARCHAR(1000),
    trace_id    VARCHAR(64),
    constraint audit_outcome_ck check (outcome in ('SUCCESS', 'FAILURE')),
    constraint audit_actor_type_ck check (actor_type in ('USER', 'CLIENT', 'SYSTEM', 'ANONYMOUS')),
    constraint audit_target_type_ck check (target_type is null or target_type in
        ('USER', 'ROLE', 'CLIENT', 'IDP', 'SETTINGS', 'KEY', 'SESSION', 'INVITATION', 'TOKEN'))
);
CREATE INDEX IF NOT EXISTS idx_audit_occurred ON @@SCHEMA@@.audit_events (occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_type_time ON @@SCHEMA@@.audit_events (event_type, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_actor ON @@SCHEMA@@.audit_events (actor_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_target ON @@SCHEMA@@.audit_events (target_type, target_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_client ON @@SCHEMA@@.audit_events (client_id, occurred_at DESC);

-- The keys that sign every token. The public half is plain JSON (it is what the JWKS serves); the private half is a
-- secret-crypto envelope (`ENC:…`) and never leaves the row unencrypted. One key is ACTIVE and signs; a rotated-out
-- key is RETIRING — still in the JWKS so in-flight tokens verify — until retire_after, then RETIRED.
create table if not exists @@SCHEMA@@.signing_keys
(
    id               uuid primary key,
    kid              varchar(190) not null unique,
    algorithm        varchar(20)  not null default 'RS256',
    status           varchar(16)  not null check (status in ('ACTIVE', 'RETIRING', 'RETIRED')),
    public_jwk_json  text         not null,
    private_jwk_enc  text         not null,
    created_at       timestamptz  not null,
    activated_at     timestamptz,
    retire_after     timestamptz,
    retired_at       timestamptz
);

create index if not exists idx_signing_keys_status on @@SCHEMA@@.signing_keys (status);

-- External logins brokered through @@SCHEMA@@. The alias is Spring's registration id and the last path segment of the
-- redirect URI registered at the provider; the client id and secret are secret-crypto envelopes. `type` is what the
-- provider speaks; `preset` is which button the console drew it from and which defaults apply.
create table if not exists @@SCHEMA@@.identity_providers
(
    id                   uuid primary key,
    realm_id             varchar(64)  not null default 'platform',
    alias                varchar(50)  not null,
    display_name         varchar(100) not null,
    type                 varchar(16)  not null check (type in ('OIDC', 'OAUTH2')),
    preset               varchar(24)  not null
        check (preset in ('GOOGLE', 'MICROSOFT', 'APPLE', 'GITHUB', 'FACEBOOK', 'GENERIC_OIDC', 'GENERIC_OAUTH2')),
    enabled              boolean      not null default true,
    hide_on_login        boolean      not null default false,
    sort_order           integer      not null default 0,
    client_id_enc        text         not null,
    client_secret_enc    text,
    issuer_uri           varchar(500),
    authorization_uri    varchar(500),
    token_uri            varchar(500),
    user_info_uri        varchar(500),
    jwk_set_uri          varchar(500),
    scopes               varchar(500),
    user_name_attribute  varchar(100),
    client_auth_method   varchar(32)  not null default 'client_secret_basic',
    email_domains        varchar(1000),
    account_linking      varchar(16)  not null default 'CONFIRM' check (account_linking in ('LINK', 'CONFIRM', 'REJECT')),
    jit_provisioning     boolean      not null default false,
    default_roles        varchar(500),
    trust_email_verified boolean      not null default true,
    attribute_mapping    varchar(1000),
    created_at           timestamptz  not null,
    updated_at           timestamptz  not null,
    -- The alias is a Spring registrationId; two stores may both call their provider 'google'.
    constraint uk_idp_realm_alias unique (realm_id, alias)
);

-- One row per (provider, subject): which external identity signs in as which account.
create table if not exists @@SCHEMA@@.user_identities
(
    realm_id varchar(64) not null default 'platform',
    id            uuid primary key,
    user_id       uuid         not null references @@SCHEMA@@.users (id) on delete cascade,
    provider_id   uuid         not null references @@SCHEMA@@.identity_providers (id) on delete cascade,
    subject       varchar(255) not null,
    email         varchar(255),
    linked_at     timestamptz  not null,
    last_login_at timestamptz,
    unique (provider_id, subject)
);

create index if not exists idx_user_identities_user on @@SCHEMA@@.user_identities (user_id);

-- The transactional outbox (namastack), in this deployment's own schema. Every event in sso-events is a row here until its
-- consumer has run; the starter's schema initialisation is off, so these are the only DDL for it.
CREATE TABLE IF NOT EXISTS @@SCHEMA@@.outbox_record
(
    id             VARCHAR(255)             NOT NULL,
    status         VARCHAR(20)              NOT NULL,
    record_key     VARCHAR(255)             NOT NULL,
    record_type    VARCHAR(255)             NOT NULL,
    payload        TEXT                     NOT NULL,
    context        TEXT,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at   TIMESTAMP WITH TIME ZONE,
    failure_count  INT                      NOT NULL,
    failure_reason VARCHAR(1000),
    next_retry_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    partition_no   INTEGER                  NOT NULL,
    handler_id     VARCHAR(1000)            NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS @@SCHEMA@@.outbox_instance
(
    instance_id    VARCHAR(255) PRIMARY KEY,
    hostname       VARCHAR(255)             NOT NULL,
    port           INTEGER                  NOT NULL,
    status         VARCHAR(50)              NOT NULL,
    started_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    last_heartbeat TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS @@SCHEMA@@.outbox_partition
(
    partition_number INTEGER PRIMARY KEY,
    instance_id      VARCHAR(255),
    version          BIGINT                   NOT NULL DEFAULT 0,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_@@SCHEMA@@_outbox_record_record_key_created ON @@SCHEMA@@.outbox_record (record_key, created_at);
CREATE INDEX IF NOT EXISTS idx_@@SCHEMA@@_outbox_record_partition_status_retry ON @@SCHEMA@@.outbox_record (partition_no, status, next_retry_at);
CREATE INDEX IF NOT EXISTS idx_@@SCHEMA@@_outbox_record_status_retry ON @@SCHEMA@@.outbox_record (status, next_retry_at);
CREATE INDEX IF NOT EXISTS idx_@@SCHEMA@@_outbox_record_status ON @@SCHEMA@@.outbox_record (status);
CREATE INDEX IF NOT EXISTS idx_@@SCHEMA@@_outbox_record_record_key_completed_created ON @@SCHEMA@@.outbox_record (record_key, completed_at, created_at);
CREATE INDEX IF NOT EXISTS idx_@@SCHEMA@@_outbox_instance_status_heartbeat ON @@SCHEMA@@.outbox_instance (status, last_heartbeat);
