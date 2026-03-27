create schema if not exists cua;
-- Users table
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

-- Roles table
create table if not exists cua.roles
(
    id   uuid PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    constraint UKofx66keruapi6vyqpv6f2or37 unique (name)
);

-- User-Roles join table
create table if not exists cua.user_roles
(
    user_id uuid NOT NULL,
    role_id uuid NOT NULL,
    PRIMARY KEY (user_id, role_id),
    constraint FKh8ciramu9cc9q3qcqiv4ue8a6 foreign key (role_id) references cua.roles,
    constraint FKhfh9dx7w3ubf1co1vdev94g3f foreign key (user_id) references cua.users
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