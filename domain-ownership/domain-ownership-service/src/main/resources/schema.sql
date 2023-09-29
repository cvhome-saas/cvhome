CREATE TABLE IF NOT EXISTS owner
(
    id    varchar(50) not null,
    email varchar(50) not null,
    constraint owner_pk primary key (id)
);

CREATE TABLE IF NOT EXISTS domain
(
    id             varchar(24) not null,
    domain         varchar(50) not null,
    domain_type    varchar(24) not null,
    reference      varchar(50) not null,
    reference_type varchar(24) not null,
    status     varchar(30) not null default 'INITIATED',
    auto_renew bool        not null default false,
    auto_order bool        not null default false,
    owner_id   varchar(50) not null,
    constraint domain_pk primary key (id),
    constraint unique_domain unique (domain),
    constraint domain_owner_id_fk foreign key (owner_id) references owner (id)
);

CREATE TABLE IF NOT EXISTS owner_domain_ref
(
    owner_id  varchar(50) not null,
    domain_id varchar(24) not null,
    constraint owner_domain_ref_owner_id_fk foreign key (owner_id) references owner,
    constraint owner_domain_ref_domain_id_fk foreign key (domain_id) references domain
);
