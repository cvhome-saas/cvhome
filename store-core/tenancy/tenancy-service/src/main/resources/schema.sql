create schema if not exists tenancy;
CREATE TABLE IF NOT EXISTS tenancy.manager_org
(
    id           varchar(24) not null,
    created_date timestamp   not null,
    email        varchar(50) not null,
    version      int,
    constraint manager_org_pk primary key (id)
);
CREATE TABLE IF NOT EXISTS tenancy.manager_store
(
    id                 varchar(24) not null,
    name               varchar(50) not null,
    created_date       timestamp   not null,
    org_id             varchar(24) not null,
    pod_id             varchar(24) not null,
    provisioning_state varchar(30) not null,
    version            int,
    constraint manager_store_pk primary key (id),
    constraint manager_store_manager_fk foreign key (org_id) references tenancy.manager_org (id),
    -- Store names are the tenant's public handle and were only ever checked with a read-then-write
    -- `checkNameExists` call, which two concurrent creates both pass. The constraint is what actually decides.
    constraint manager_store_name_uq unique (name),
    -- schema.sql is the source of truth for DDL, so a new ProvisioningState constant means editing this line.
    constraint manager_store_provisioning_ck check (provisioning_state in
                                                    ('NOT_STARTED_PROVISIONING', 'IN_PROGRESS_PROVISIONING',
                                                     'SUCCESSFULLY_PROVISIONING', 'FAILED_PROVISIONING'))
);

-- Every store list is filtered by org (see InternalStoreServiceImpl.findAll), and the console's main screen is
-- that query.
CREATE INDEX IF NOT EXISTS manager_store_org_idx ON tenancy.manager_store (org_id);
-- The reaper for stores stuck mid-provisioning scans on this.
CREATE INDEX IF NOT EXISTS manager_store_provisioning_idx ON tenancy.manager_store (provisioning_state);
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
