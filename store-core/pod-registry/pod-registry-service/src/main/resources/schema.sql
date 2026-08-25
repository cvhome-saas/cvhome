-- Pod registry DDL. This file is the source of truth; ddl-auto is only a safety net.
--
-- The schema is pod_registry with an UNDERSCORE. spring.application.name is `pod-registry`, which common-config.yml
-- feeds into spring.datasource.hikari.schema — a hyphenated name that is not this schema. Every @Table pins
-- schema = "pod_registry" explicitly rather than relying on the connection default.
--
-- Columns for phases 6 and 8 (lifecycle, visibility, capacity, health) are created now, as billing's schema did, so
-- that adding placement and health is code rather than a migration. Every enum column carries a CHECK: a new enum
-- constant means editing this file.
create schema if not exists pod_registry;

CREATE TABLE IF NOT EXISTS pod_registry.pod
(
    id                  varchar(24)  not null,
    name                varchar(50)  not null,
    endpoint            varchar(255) not null,
    endpoint_type       varchar(20)  not null,
    org_id              varchar(24),
    visibility          varchar(10)  not null default 'PUBLIC',
    lifecycle_state     varchar(20)  not null default 'ACTIVE',
    region              varchar(30),
    capacity_max_stores int,
    capacity_stores     int          not null default 0,
    last_health_status  varchar(10),
    last_health_at      timestamp,
    version             int,
    constraint pod_pk primary key (id),
    constraint pod_name_uq unique (name),
    constraint pod_endpoint_type_ck check (endpoint_type in ('EXTERNAL', 'INTERNAL')),
    constraint pod_visibility_ck check (visibility in ('PUBLIC', 'PRIVATE')),
    constraint pod_lifecycle_ck check (lifecycle_state in
                                       ('PROVISIONING', 'ACTIVE', 'DRAINING', 'DECOMMISSIONED')),
    constraint pod_health_ck check (last_health_status is null or last_health_status in ('GREEN', 'AMBER', 'RED')),
    constraint pod_capacity_ck check (capacity_max_stores is null or capacity_max_stores >= 0),
    -- A pod that names an owner must be private. Without this a pod could be owned and public at once, and
    -- placement would offer one organization's dedicated infrastructure to everybody.
    constraint pod_private_owner_ck check (org_id is null or visibility = 'PRIVATE')
);

-- Placement reads this on every store creation.
CREATE INDEX IF NOT EXISTS pod_placement_idx ON pod_registry.pod (visibility, lifecycle_state)
    WHERE org_id is null;
CREATE INDEX IF NOT EXISTS pod_org_idx ON pod_registry.pod (org_id);

-- Which store sits on which pod. Phase 8 maintains pod.capacity_stores from tenancy's store events, and
-- `capacity_stores = capacity_stores + 1` is not idempotent — a redelivered StoreCreatedEvent would double-count.
-- This table is what makes the handler a genuine no-op on redelivery: the primary key rejects the second insert.
CREATE TABLE IF NOT EXISTS pod_registry.pod_store_placement
(
    store_id  varchar(24) not null,
    pod_id    varchar(24) not null,
    placed_at timestamp   not null,
    constraint pod_store_placement_pk primary key (store_id),
    constraint pod_store_placement_pod_fk foreign key (pod_id) references pod_registry.pod (id)
);
CREATE INDEX IF NOT EXISTS pod_store_placement_pod_idx ON pod_registry.pod_store_placement (pod_id);

-- Rolling health probe results, pruned by a retention job. Kept separate from pod.last_health_* so the current
-- state stays a single cheap read while the history is still available for diagnosis.
CREATE TABLE IF NOT EXISTS pod_registry.pod_health_check
(
    id          bigserial,
    pod_id      varchar(24) not null,
    status      varchar(10) not null,
    latency_ms  int,
    detail      varchar(500),
    checked_at  timestamp   not null,
    constraint pod_health_check_pk primary key (id),
    constraint pod_health_check_pod_fk foreign key (pod_id) references pod_registry.pod (id),
    constraint pod_health_check_status_ck check (status in ('GREEN', 'AMBER', 'RED'))
);
CREATE INDEX IF NOT EXISTS pod_health_check_pod_idx ON pod_registry.pod_health_check (pod_id, checked_at desc);

-- Append-only. A pod moving to DRAINING or being deleted changes where every future store lands, so who did it and
-- when is worth keeping. Same shape as billing.subscription_audit.
CREATE TABLE IF NOT EXISTS pod_registry.pod_audit
(
    id             bigserial,
    pod_id         varchar(24) not null,
    from_lifecycle varchar(20),
    to_lifecycle   varchar(20),
    source         varchar(10) not null,
    actor          varchar(100),
    detail         varchar(500),
    recorded_at    timestamp   not null,
    constraint pod_audit_pk primary key (id),
    constraint pod_audit_source_ck check (source in ('API', 'JOB', 'SYSTEM'))
);
CREATE INDEX IF NOT EXISTS pod_audit_pod_idx ON pod_registry.pod_audit (pod_id, recorded_at desc);
