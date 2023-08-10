CREATE TABLE IF NOT EXISTS public.domain_certificate_order
(
    id                       SERIAL PRIMARY KEY,
    location                 varchar(150),
    domain                   varchar(70),
    certificate_order_status varchar(50),
    challenges               varchar(1500)
);
