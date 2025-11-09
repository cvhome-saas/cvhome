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
