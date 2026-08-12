create schema if not exists tenancy;
CREATE TABLE IF NOT EXISTS tenancy.manager_org
(
    id            varchar(24) not null,
    created_date  timestamp   not null,
    email         varchar(50) not null,
    -- An organization had only an id, an email and a created date: nothing to show a human, and no way to say
    -- "this one is suspended".
    name          varchar(100),
    status        varchar(20) not null default 'ACTIVE',
    owner_user_id varchar(64),
    version       int,
    constraint manager_org_pk primary key (id),
    constraint manager_org_status_ck check (status in ('ACTIVE', 'SUSPENDED', 'CLOSED'))
);
CREATE TABLE IF NOT EXISTS tenancy.manager_store
(
    id                 varchar(24) not null,
    name               varchar(50) not null,
    created_date       timestamp   not null,
    org_id             varchar(24) not null,
    pod_id             varchar(24) not null,
    provisioning_state varchar(30) not null,
    status             varchar(20) not null default 'ACTIVE',
    version            int,
    constraint manager_store_pk primary key (id),
    constraint manager_store_manager_fk foreign key (org_id) references tenancy.manager_org (id),
    -- Store names are the tenant's public handle and were only ever checked with a read-then-write
    -- `checkNameExists` call, which two concurrent creates both pass. The constraint is what actually decides.
    constraint manager_store_name_uq unique (name),
    -- schema.sql is the source of truth for DDL, so a new ProvisioningState constant means editing this line.
    constraint manager_store_provisioning_ck check (provisioning_state in
                                                    ('NOT_STARTED_PROVISIONING', 'IN_PROGRESS_PROVISIONING',
                                                     'SUCCESSFULLY_PROVISIONING', 'FAILED_PROVISIONING')),
    -- Distinct from provisioning_state, which says how far the build got. This says whether the store may be
    -- used, and it is the operator's lever rather than the machine's.
    constraint manager_store_status_ck check (status in ('ACTIVE', 'SUSPENDED', 'ARCHIVED', 'DELETED'))
);

-- Every store list is filtered by org (see InternalStoreServiceImpl.findAll), and the console's main screen is
-- that query.
CREATE INDEX IF NOT EXISTS manager_store_org_idx ON tenancy.manager_store (org_id);
-- The reaper for stores stuck mid-provisioning scans on this.
CREATE INDEX IF NOT EXISTS manager_store_provisioning_idx ON tenancy.manager_store (provisioning_state);

-- Who belongs to an organization, beyond the one administrator signup creates. The user id is uaa's, and is a
-- string rather than a value object because uaa issues UUIDs, not ObjectIds.
CREATE TABLE IF NOT EXISTS tenancy.org_member
(
    org_id   varchar(24) not null,
    user_id  varchar(64) not null,
    role     varchar(30) not null,
    added_at timestamp   not null,
    added_by varchar(64),
    constraint org_member_pk primary key (org_id, user_id),
    constraint org_member_org_fk foreign key (org_id) references tenancy.manager_org (id)
);
CREATE INDEX IF NOT EXISTS org_member_user_idx ON tenancy.org_member (user_id);

-- An invitation to join an organization.
--
-- token_hash, never the token: this is a bearer credential that grants membership, so it is stored the way a
-- password reset would be. The plaintext exists once, in the response to whoever created the invitation, and
-- nowhere else — including the logs.
CREATE TABLE IF NOT EXISTS tenancy.org_invitation
(
    id          varchar(24)  not null,
    org_id      varchar(24)  not null,
    email       varchar(120) not null,
    role        varchar(30)  not null,
    token_hash  varchar(88)  not null,
    status      varchar(20)  not null,
    expires_at  timestamp    not null,
    created_at  timestamp    not null,
    created_by  varchar(64),
    accepted_at timestamp,
    accepted_by varchar(64),
    version     int,
    constraint org_invitation_pk primary key (id),
    constraint org_invitation_org_fk foreign key (org_id) references tenancy.manager_org (id),
    constraint org_invitation_token_uq unique (token_hash),
    constraint org_invitation_status_ck check (status in ('PENDING', 'ACCEPTED', 'REVOKED', 'EXPIRED'))
);
CREATE INDEX IF NOT EXISTS org_invitation_org_idx ON tenancy.org_invitation (org_id, status);
-- Only one live invitation per address per organization; re-inviting reuses or replaces it rather than
-- accumulating tokens that all still work.
CREATE UNIQUE INDEX IF NOT EXISTS org_invitation_pending_uq
    ON tenancy.org_invitation (org_id, lower(email)) WHERE status = 'PENDING';

-- Append-only. Suspending a store or an organization takes someone's business offline, so who did it, when,
-- and what it was before are worth more than the current value alone.
CREATE TABLE IF NOT EXISTS tenancy.tenancy_audit
(
    id          bigserial,
    entity_type varchar(20)  not null,
    entity_id   varchar(24)  not null,
    action      varchar(30)  not null,
    from_state  varchar(30),
    to_state    varchar(30),
    actor       varchar(100),
    source      varchar(10)  not null,
    detail      varchar(500),
    recorded_at timestamp    not null,
    constraint tenancy_audit_pk primary key (id),
    constraint tenancy_audit_entity_ck check (entity_type in ('STORE', 'ORG', 'MEMBER', 'INVITATION')),
    constraint tenancy_audit_source_ck check (source in ('API', 'JOB', 'SYSTEM'))
);
CREATE INDEX IF NOT EXISTS tenancy_audit_entity_idx ON tenancy.tenancy_audit (entity_type, entity_id, recorded_at desc);
create schema if not exists tenancy_outbox;

CREATE TABLE IF NOT EXISTS tenancy_outbox.outbox_record
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

CREATE TABLE IF NOT EXISTS tenancy_outbox.outbox_instance
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

CREATE TABLE IF NOT EXISTS tenancy_outbox.outbox_partition
(
    partition_number INTEGER PRIMARY KEY,
    instance_id      VARCHAR(255),
    version          BIGINT                   NOT NULL DEFAULT 0,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_record_record_key_created
    ON tenancy_outbox.outbox_record (record_key, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_record_partition_status_retry
    ON tenancy_outbox.outbox_record (partition_no, status, next_retry_at);

CREATE INDEX IF NOT EXISTS idx_outbox_record_status_retry
    ON tenancy_outbox.outbox_record (status, next_retry_at);

CREATE INDEX IF NOT EXISTS idx_outbox_record_status
    ON tenancy_outbox.outbox_record (status);

CREATE INDEX IF NOT EXISTS idx_outbox_record_record_key_completed_created
    ON tenancy_outbox.outbox_record (record_key, completed_at, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_instance_status_heartbeat
    ON tenancy_outbox.outbox_instance (status, last_heartbeat);

CREATE INDEX IF NOT EXISTS idx_outbox_instance_last_heartbeat
    ON tenancy_outbox.outbox_instance (last_heartbeat);

CREATE INDEX IF NOT EXISTS idx_outbox_instance_status
    ON tenancy_outbox.outbox_instance (status);

CREATE INDEX IF NOT EXISTS idx_outbox_partition_instance_id
    ON tenancy_outbox.outbox_partition (instance_id);
