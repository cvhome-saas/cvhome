create schema if not exists router;
CREATE TABLE IF NOT EXISTS router.pod
(
    id            varchar(24) not null,
    region        varchar(30),
    sub_region    varchar(30),
    pod_type      varchar(30),
    namespace     varchar(70),
    location      varchar(150),
    location_alis varchar(150),
    constraint pod_pk primary key (id)
);
CREATE TABLE IF NOT EXISTS router.reference
(
    id             varchar(24) not null,
    reference      varchar(24),
    reference_type varchar(24),
    enabled        boolean,
    pod_id         varchar(24),
    constraint reference_pk primary key (id),
    constraint reference_pod_id_fk foreign key (pod_id) references router.pod
);
CREATE TABLE IF NOT EXISTS router.reference_alis
(
    id           varchar(24) not null,
    domain       varchar(40),
    reference_id varchar(24),
    constraint reference_alis_pk primary key (id),
    constraint reference_alis_reference_id_fk foreign key (reference_id) references router.reference
);
