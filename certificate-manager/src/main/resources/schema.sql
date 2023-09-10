CREATE TABLE IF NOT EXISTS public.domain_certificate_order
(
    id                        SERIAL PRIMARY KEY,
    location                  varchar(150),
    domain                    varchar(70),
    challenge_validation_type varchar(50) not null,
    certificate_order_status  varchar(50) default 'INITIATED',
    challenges                varchar(1500),
    created_date              timestamp,
    requested_date            timestamp,
    validated_date            timestamp,
    generated_date            timestamp,
    CONSTRAINT location_unique unique (location)
);