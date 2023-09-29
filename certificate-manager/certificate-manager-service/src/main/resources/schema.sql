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

CREATE TABLE IF NOT EXISTS orders_event
(
    event_id varchar(24) not null,
    event_type varchar(50) not null,
    data     varchar(1500),
    orders_id  varchar(24) not null,
    sequence   varchar(5)  not null,
    constraint orders_event_pk primary key (event_id),
    constraint orders_event_orders_fk foreign key (orders_id) references orders
);
