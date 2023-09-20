CREATE TABLE IF NOT EXISTS orders
(
    id                        varchar(24) not null,
    location                  varchar(150),
    domain                    varchar(70),
    challenge_validation_type varchar(50) not null,
    certificate_order_status  varchar(50) default 'INITIATED',
    challenges                varchar(1500),
    created_date              timestamp,
    requested_date            timestamp,
    validated_date            timestamp,
    generated_date            timestamp,
    constraint orders_pk primary key (id)
);

CREATE TABLE IF NOT EXISTS certificate
(
    id            varchar(24)  not null,
    not_after     timestamp    not null,
    not_before    timestamp    not null,
    serial_number varchar(300) not null,
    version       int          not null,
    sig_alg_name  varchar(300) not null,
    sig_alg_oid   varchar(300) not null,
    orders_id     varchar(24)  not null,
    constraint certificate_pk primary key (id),
    constraint certificate_orders_id_fk foreign key (orders_id) references orders
);

CREATE TABLE IF NOT EXISTS domain
(
    id                    varchar(24) not null,
    domain                varchar(50) not null,
    status                varchar(20) not null default 'INITIATED',
    auto_renew            bool        not null default false,
    auto_order bool not null default false,
    active_certificate_id varchar(24) null,
    constraint domain_pk primary key (id),
    constraint domain_active_certificate_id_fk foreign key (active_certificate_id) references certificate
);

CREATE TABLE IF NOT EXISTS domain_certificate_ref
(
    domain_id      varchar(24) not null,
    certificate_id varchar(24) not null,
    constraint domain_certificate_ref_domain_id_fk foreign key (domain_id) references domain,
    constraint domain_certificate_ref_certificate_id_fk foreign key (certificate_id) references certificate
);

CREATE TABLE IF NOT EXISTS domain_orders_ref
(
    domain_id varchar(24) not null,
    orders_id varchar(24) not null,
    constraint domain_orders_ref_domain_id_fk foreign key (domain_id) references domain,
    constraint domain_orders_ref_orders_id_fk foreign key (orders_id) references orders
);


