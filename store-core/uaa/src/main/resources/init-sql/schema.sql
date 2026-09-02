create schema if not exists uaa;
create table if not exists uaa.oauth2_registered_client
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

-- oauth2_authorization
-- Spring Authorization Server's own table, in its Postgres shape: token values and metadata are text (the shipped
-- schema's blob), instants are timestamptz, and the device-flow columns are present because the JDBC service
-- inserts every column whether or not the grant is enabled.
create table if not exists uaa.oauth2_authorization
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
    foreign key (registered_client_id) references uaa.oauth2_registered_client (id)
);

create index if not exists idx_oauth2_authorization_principal on uaa.oauth2_authorization (principal_name);
create index if not exists idx_oauth2_authorization_client on uaa.oauth2_authorization (registered_client_id);

-- oauth2_authorization_consent
create table if not exists uaa.oauth2_authorization_consent
(
    registered_client_id varchar(100)  not null,
    principal_name       varchar(200)  not null,
    authorities          varchar(1000) not null,
    primary key (registered_client_id, principal_name),
    foreign key (registered_client_id) references uaa.oauth2_registered_client (id)
);


-- Users table
create table if not exists uaa.users
(
    id                     uuid PRIMARY KEY,
    email                  VARCHAR(254) NOT NULL UNIQUE,
    username               VARCHAR(190) NOT NULL UNIQUE,
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
    constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email),
    constraint UKr43af9ap4edm43mmtq01oddj6 unique (username)
);

CREATE INDEX IF NOT EXISTS idx_users_metadata ON uaa.users USING gin (metadata);
CREATE INDEX IF NOT EXISTS idx_users_email_lower ON uaa.users (lower(email));
CREATE INDEX IF NOT EXISTS idx_users_username_lower ON uaa.users (lower(username));

-- Roles table
create table if not exists uaa.roles
(
    id               uuid PRIMARY KEY,
    name             VARCHAR(80) NOT NULL,
    description      VARCHAR(255),
    scope            VARCHAR(20) NOT NULL DEFAULT 'REALM',
    system_role      BOOLEAN     NOT NULL DEFAULT FALSE,
    inherits_from_id uuid REFERENCES uaa.roles (id),
    created_at       timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       timestamptz,
    constraint UKofx66keruapi6vyqpv6f2or37 unique (name),
    constraint roles_scope_ck check (scope in ('REALM', 'ORGANIZATION', 'CLIENT'))
);

-- The permissions a role grants; the effective set of a user is the union over their roles and what those inherit.
create table if not exists uaa.role_permissions
(
    role_id    uuid        NOT NULL REFERENCES uaa.roles (id) ON DELETE CASCADE,
    permission VARCHAR(80) NOT NULL,
    PRIMARY KEY (role_id, permission)
);

-- User-Roles join table
create table if not exists uaa.user_roles
(
    user_id uuid NOT NULL,
    role_id uuid NOT NULL,
    PRIMARY KEY (user_id, role_id),
    constraint FKh8ciramu9cc9q3qcqiv4ue8a6 foreign key (role_id) references uaa.roles,
    constraint FKhfh9dx7w3ubf1co1vdev94g3f foreign key (user_id) references uaa.users
);

-- The previous hashes a user may not reuse; trimmed to settings.password_history_count on every change.
create table if not exists uaa.password_history
(
    id            uuid PRIMARY KEY,
    user_id       uuid         NOT NULL REFERENCES uaa.users (id) ON DELETE CASCADE,
    password_hash VARCHAR(100) NOT NULL,
    created_at    timestamptz  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_password_history_user ON uaa.password_history (user_id, created_at DESC);

-- Realm-wide policy: exactly one row.
create table if not exists uaa.settings
(
    id                                smallint PRIMARY KEY DEFAULT 1 CHECK (id = 1),
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
create table if not exists uaa.audit_events
(
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
CREATE INDEX IF NOT EXISTS idx_audit_occurred ON uaa.audit_events (occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_type_time ON uaa.audit_events (event_type, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_actor ON uaa.audit_events (actor_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_target ON uaa.audit_events (target_type, target_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_client ON uaa.audit_events (client_id, occurred_at DESC);

create table if not exists uaa.signing_keys
(
    id         uuid                        not null,
    active     boolean                     not null,
    created_at timestamp(6) with time zone not null,
    jwk_json   oid                         not null,
    kid        varchar(190)                not null,
    primary key (id),
    constraint UKtobs6m52hleh04iy0qgpb2yfv unique (kid)
);