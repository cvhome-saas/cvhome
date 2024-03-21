create schema if not exists landing;
CREATE TABLE IF NOT EXISTS landing.owner
(
    id    varchar(50) not null,
    email varchar(50) not null,
    constraint owner_pk primary key (id)
);

CREATE TABLE IF NOT EXISTS landing.store
(
    id       varchar(24) not null,
    name     varchar(50) not null,
    owner_id varchar(50) not null,
    constraint store_pk primary key (id),
    constraint store_owner_id_fk foreign key (owner_id) references owner (id)
);

CREATE TABLE IF NOT EXISTS landing.owner_store_ref
(
    owner_id varchar(50) not null,
    store_id varchar(24) not null,
    constraint owner_store_ref_owner_id_fk foreign key (owner_id) references owner,
    constraint owner_store_ref_store_id_fk foreign key (store_id) references store
);
