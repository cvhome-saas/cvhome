CREATE TABLE IF NOT EXISTS pod
(
    id        varchar(24) not null,
    region    varchar(30),
    sub_region varchar(30),
    pod_type      varchar(30),
    namespace varchar(70),
    location  varchar(150),
    location_alis  varchar(150),
    constraint pod_pk primary key (id)
);
