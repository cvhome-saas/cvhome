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
    id                 varchar(24)   not null,
    name               varchar(50)   not null,
    created_date       timestamp     not null,
    org_id             varchar(24)   not null,
    pod_id             varchar(24)   not null,
    provisioning_state varchar(30)   not null,
    version            int,
    constraint manager_store_pk primary key (id),
    constraint manager_store_manager_fk foreign key (org_id) references manager.manager_org (id)
);
create schema if not exists subscription;
CREATE TABLE IF NOT EXISTS subscription.subscription
(
    id                  varchar(24) not null,
    created_date        timestamp   not null,
    last_renewed_date   timestamp   not null,
    end_date            timestamp   not null,
    de_activated_date   timestamp   null,
    subscription_plan   varchar(20) not null,
    recurring_plan      varchar(20) not null,
    subscription_status varchar(20) not null,
    version             int,
    constraint subscription_pk primary key (id)
);
CREATE TABLE IF NOT EXISTS subscription.subscription_price_plan
(
    id                varchar(50) not null,
    product_id        varchar(50),
    currency          varchar(6),
    price             bigint,
    subscription_plan varchar(20) not null,
    recurring_plan    varchar(20) not null,
    version           int,
    constraint subscription_price_plan_pk primary key (id),
    constraint price_product_uq unique (id, product_id),
    constraint subscription_recurring_uq unique (subscription_plan, recurring_plan)

);

create schema if not exists org;
CREATE TABLE IF NOT EXISTS org.pod
(
    id            varchar(24) not null,
    name          varchar(50) not null,
    endpoint      varchar(255) not null,
    endpoint_type varchar(20) not null,
    org_id        varchar(24),
    domain        varchar(255),
    version       int,
    constraint pod_pk primary key (id)
    );
