CREATE TABLE IF NOT EXISTS public.x_user
(
    id               serial PRIMARY KEY,
    name             varchar(50),
    email            varchar(50),
    external_auth_id varchar(50),
    constraint external_auth_id_unique unique (external_auth_id)
);


CREATE TABLE IF NOT EXISTS public.domain_reference
(
    id                    SERIAL PRIMARY KEY,
    domain                varchar(70) not null,
    reference             varchar(50) not null,
    domain_type           varchar(50) not null,
    domain_status         varchar(50) not null default 'INITIATED',
    created_date          timestamp,
    external_acm_order_id bigint,
    constraint domain_reference_domain_unique unique (domain)
);
