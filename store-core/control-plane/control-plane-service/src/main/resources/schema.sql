create schema if not exists manager;
CREATE TABLE IF NOT EXISTS manager.manager_org
(
    id           varchar(24) not null,
    created_date timestamp   not null,
    email        varchar(50) not null,
    version      int,
    constraint manager_org_pk primary key (id)
);
CREATE TABLE IF NOT EXISTS manager.manager_store
(
    id                 varchar(24) not null,
    name               varchar(50) not null,
    created_date       timestamp   not null,
    org_id             varchar(24) not null,
    pod_id             varchar(24) not null,
    provisioning_state varchar(30) not null,
    version            int,
    constraint manager_store_pk primary key (id),
    constraint manager_store_manager_fk foreign key (org_id) references manager.manager_org (id)
);
create schema if not exists org;
CREATE TABLE IF NOT EXISTS org.pod
(
    id            varchar(24)  not null,
    name          varchar(50)  not null,
    endpoint      varchar(255) not null,
    endpoint_type varchar(20)  not null,
    org_id        varchar(24),
    version       int,
    constraint pod_pk primary key (id),
    constraint pod_name_uq unique (name)
);

create schema if not exists control;

CREATE TABLE IF NOT EXISTS control.outbox_record
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

CREATE TABLE IF NOT EXISTS control.outbox_instance
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

CREATE TABLE IF NOT EXISTS control.outbox_partition
(
    partition_number INTEGER PRIMARY KEY,
    instance_id      VARCHAR(255),
    version          BIGINT                   NOT NULL DEFAULT 0,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_record_record_key_created
    ON control.outbox_record (record_key, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_record_partition_status_retry
    ON control.outbox_record (partition_no, status, next_retry_at);

CREATE INDEX IF NOT EXISTS idx_outbox_record_status_retry
    ON control.outbox_record (status, next_retry_at);

CREATE INDEX IF NOT EXISTS idx_outbox_record_status
    ON control.outbox_record (status);

CREATE INDEX IF NOT EXISTS idx_outbox_record_record_key_completed_created
    ON control.outbox_record (record_key, completed_at, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_instance_status_heartbeat
    ON control.outbox_instance (status, last_heartbeat);

CREATE INDEX IF NOT EXISTS idx_outbox_instance_last_heartbeat
    ON control.outbox_instance (last_heartbeat);

CREATE INDEX IF NOT EXISTS idx_outbox_instance_status
    ON control.outbox_instance (status);

CREATE INDEX IF NOT EXISTS idx_outbox_partition_instance_id
    ON control.outbox_partition (instance_id);
