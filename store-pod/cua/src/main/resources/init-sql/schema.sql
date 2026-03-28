create schema if not exists cua;

create table if not exists cua.users
(
    id            uuid PRIMARY KEY,
    email         VARCHAR(254) NOT NULL UNIQUE,
    username      VARCHAR(190) NOT NULL UNIQUE,
    first_name    VARCHAR(50),
    last_name     VARCHAR(50),
    password_hash VARCHAR(100),
    metadata      jsonb                 DEFAULT '{}'::jsonb,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email),
    constraint UKr43af9ap4edm43mmtq01oddj6 unique (username)
);

CREATE INDEX IF NOT EXISTS idx_users_metadata ON cua.users USING gin (metadata);

CREATE TABLE IF NOT EXISTS cua.social_login_configs
(
    id                uuid PRIMARY KEY,
    client_id         VARCHAR(190) NOT NULL,
    provider_id       VARCHAR(50)  NOT NULL,
    app_id            VARCHAR(254) NOT NULL,
    app_secret        VARCHAR(254) NOT NULL,
    scopes            VARCHAR(254),
    CONSTRAINT UK_client_provider UNIQUE (client_id, provider_id)
);

create table if not exists cua.signing_keys
(
    id         uuid                        not null,
    active     boolean                     not null,
    created_at timestamp(6) with time zone not null,
    jwk_json   oid                         not null,
    kid        varchar(190)                not null,
    primary key (id),
    constraint UKtobs6m52hleh04iy0qgpb2yfv unique (kid)
);