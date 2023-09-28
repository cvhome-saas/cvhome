CREATE TABLE IF NOT EXISTS owner
(
    id       varchar(24) not null,
    identity varchar(50) not null,
    email    varchar(50) not null,
    constraint owner_pk primary key (id)
);

CREATE TABLE IF NOT EXISTS domain
(
    id             varchar(24) not null,
    domain         varchar(50) not null,
    domain_type    varchar(24) not null,
    reference      varchar(50) not null,
    reference_type varchar(24) not null,
    owner_id       varchar(24) not null,
    constraint domain_pk primary key (id),
    constraint unique_domain unique (domain),
    constraint domain_owner_id_fk foreign key (owner_id) references owner (id)
);
