create schema if not exists inventory;
create table if not exists inventory.sm_sequencer
(
    seq_name  varchar(255) not null primary key,
    seq_count bigint
);

-- Moved from the catalog schema as-is, so migrate-from-catalog.sql can copy rows column for column. The former
-- foreign keys to catalog.product / catalog.product_variant are gone; sku is the cross-service key.
-- The service maps only: product_avail_id, audit, store_merchant_id, sku, product_id, quantity, available,
-- quantity_ord_min, quantity_ord_max. Everything else is dormant until variants return.
create table if not exists inventory.product_availability
(
    product_avail_id  bigint  not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    available         boolean,
    height            numeric(38, 2),
    length            numeric(38, 2),
    weight            numeric(38, 2),
    width             numeric(38, 2),
    owner             varchar(255),
    date_available    date,
    free_shipping     boolean,
    quantity          integer not null,
    quantity_ord_max  integer,
    quantity_ord_min  integer,
    status            boolean,
    region            varchar(255),
    region_variant    varchar(255),
    sku               varchar(255),
    store_merchant_id varchar(50),
    product_id        bigint,
    product_variant   bigint,
    constraint UK3cq0pcvlrorbgahh1r1o6fao5 unique (
                                                   store_merchant_id, product_id, product_variant,
                                                   region_variant
        )
);
create index if not exists prd_avail_store_prd_idx on inventory.product_availability (product_id, store_merchant_id);
create index if not exists prd_avail_store_sku_idx on inventory.product_availability (store_merchant_id, sku);

-- store_merchant_id is new here: the catalog schema inherited tenancy through the availability FK, a standalone
-- price row carries it directly (backfilled by migrate-from-catalog.sql). The service maps: product_price_id,
-- store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount and the three
-- special_* columns.
create table if not exists inventory.product_price
(
    product_price_id               bigint       not null primary key,
    product_price_code             varchar(255) not null,
    default_price                  boolean,
    product_identifier_id          bigint,
    store_merchant_id              varchar(50),
    product_price_amount           numeric(38, 2),
    product_price_special_amount   numeric(38, 2),
    product_price_special_end_date date,
    product_price_special_st_date  date,
    product_price_type             varchar(20)
        constraint product_price_product_price_type_check check (
            (product_price_type):: text = ANY (
                (
                    ARRAY [ 'ONE_TIME' :: character varying,
                        'MONTHLY' :: character varying]
                    ):: text[]
                )
            ),
    product_avail_id               bigint       not null
        constraint fk_prd_price_avail references inventory.product_availability
);

create table if not exists inventory.product_reservation
(
    id                bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    ref               varchar(60)  not null,
    expire_at         timestamp(6) not null,
    status            varchar(20)  not null,
    store_merchant_id varchar(50)  not null,
    constraint UNQ_PRODUCT_RESERVATION unique (store_merchant_id, ref)
);

create table if not exists inventory.product_reservation_line
(
    id                     bigint       not null primary key,
    date_created           timestamp(6),
    date_modified          timestamp(6),
    updt_id                varchar(60),
    product_reservation_id bigint       not null,
    sku                    varchar(255) not null,
    quantity               integer      not null,
    product_avail_id       bigint,
    constraint fk_prd_res_line_res foreign key (product_reservation_id) references inventory.product_reservation (id),
    constraint fk_prd_res_line_avail foreign key (product_avail_id) references inventory.product_availability (product_avail_id)
);

create index if not exists idx_prd_res_expire_at on inventory.product_reservation (expire_at);
create index if not exists idx_prd_res_line_res_id on inventory.product_reservation_line (product_reservation_id);
