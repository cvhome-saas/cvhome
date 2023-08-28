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
