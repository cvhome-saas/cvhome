create schema if not exists manager;
CREATE TABLE IF NOT EXISTS manager.manager_store
(
    id       varchar(24) not null,
    name     varchar(50) not null,
    country  varchar(20) not null,
    email    varchar(50) not null,
    owner_id varchar(50) not null,
    constraint store_pk primary key (id)
);