create schema if not exists manager;
CREATE TABLE IF NOT EXISTS manager.manager_store
(
    id               varchar(24) not null,
    name             varchar(50) not null,
    created_date     timestamp   not null,
    country          varchar(20) not null,
    email            varchar(50) not null,
    phone            varchar(50) not null,
    owner_id         varchar(50) not null,
    synced_in_router boolean default false,
    synced_in_store  boolean default false,
    constraint manager_store_pk primary key (id)
);
CREATE TABLE IF NOT EXISTS manager.reference_alis
(
    id           varchar(24) not null,
    domain       varchar(40) unique,
    reference_id varchar(24),
    constraint reference_alis_pk primary key (id),
    constraint reference_alis_reference_id_fk foreign key (reference_id) references manager.manager_store
);
