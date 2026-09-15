create schema if not exists merchant;
create table if not exists merchant.merchant_store
(
    store_merchant_id                 varchar(50)  not null primary key,
    date_created                      timestamp(6),
    date_modified                     timestamp(6),
    updt_id                           varchar(60),
    continueshoppingurl               varchar(150),
    currency_format_national          boolean,
    domain_name                       varchar(80),
    in_business_since                 date,
    invoice_template                  varchar(25),
    lineage                           varchar(255),
    org                               varchar(255) not null,
    theme                             varchar(25)  not null
        check (theme in ('DEFAULT','BASIC','BEAUTY','COSMETICS','FASHION','FURNITURE','GLASSES','GROCERY','HUNGER',
                         'JEWELLERY','PINK','SPORTS','BASIS','MODERN','JEWELERY','ELECTRONICS','FOOD','WATCHES',
                         'BABY','TOOLS')),
    color_theme                       varchar(25)  not null
        check (color_theme in ('DEFAULT','LIGHT','DARK','NATURE','OCEAN','MIDNIGHT','FOREST_WHISPER','DESERT_MIRAGE',
                               'MIDNIGHT_DUSK','ROSE','LAVENDER','AURORA_LIGHTS','CYBERPUNK','AUTUMN_HARVEST',
                               'CYBER_NEON','SUNSET','FOREST','DESERT','SKY','EARTH','FIRE','ICE','BLOSSOM','GOLDEN',
                               'GRAPE','PEACH','MINT','SAND','RAINBOW','NEON','PASTEL')),
    seizeunitcode                     varchar(5),
    store_email                       varchar(60)  not null,
    store_template                    varchar(25),
    store_address                     varchar(255),
    store_city                        varchar(100),
    store_name                        varchar(100) not null,
    store_phone                       varchar(50),
    store_postal_code                 varchar(15),
    store_state_prov                  varchar(100),
    use_cache                         boolean,
    require_login_for_order_placement boolean,
    weightunitcode                    varchar(5),
    country_id                        varchar(6)   not null,
    currency_id                       varchar(6)   not null,
    language_code                     varchar(6)   not null,
    zone_id                           varchar(100)
);
create table if not exists merchant.merchant_language
(
    store_merchant_id varchar(50) not null
        constraint FK14ylsv0o3x2vdww6ts9yx6nyi references merchant.merchant_store (store_merchant_id),
    language_code     varchar(6)  not null,
    constraint merchant_language_pk
        unique (store_merchant_id, language_code)
);

-- Store appearance moved to the content service: the logo, banner, slider images and social links now live in
-- content.site_settings and content banners, where they can be picked from the media library and, unlike here,
-- removed again. Merchant keeps store configuration — languages, currency, domains, address and contact.
alter table merchant.merchant_store drop column if exists store_logo;
alter table merchant.merchant_store drop column if exists store_banner;
drop table if exists merchant.merchant_slider_images;
drop table if exists merchant.social_links;

create table if not exists merchant.store_domains
(
    store_merchant_id varchar(50) not null,
    domain            varchar(100) not null unique,
    domain_type       varchar(15) not null check (domain_type in ('SUB_DOMAIN','CUSTOM_DOMAIN')),
    constraint FKpw0mfwlhf9uay27vw3sbal8ao foreign key (store_merchant_id) references merchant.merchant_store
);

-- The theme list grew: landing-ui ships a package per implemented `Theme` value, and the seven values it
-- already knew about (DEFAULT, FASHION, BASIC, GROCERY, PINK, HUNGER, JEWELLERY) were unwritable here
-- because the check above predates them. `create table if not exists` leaves an existing database on the
-- constraint it was created with, so replace it by name — a no-op once it already matches. The name is the
-- one Postgres gives the inline check above, so a fresh database and an old one end up on the same rule.
alter table merchant.merchant_store drop constraint if exists merchant_store_theme_check;
alter table merchant.merchant_store
    add constraint merchant_store_theme_check
        check (theme in ('DEFAULT','BASIC','BEAUTY','COSMETICS','FASHION','FURNITURE','GLASSES','GROCERY','HUNGER',
                         'JEWELLERY','PINK','SPORTS','BASIS','MODERN','JEWELERY','ELECTRONICS','FOOD','WATCHES',
                         'BABY','TOOLS'));

-- ---------------------------------------------------------------------------------------------------------------
-- Outbox: the cache events (store-commons:cache, com.asrevo.cvhome.cache.event) this service's aggregates register are
-- written here in the transaction that changed them and drained by CacheEventOutboxHandler a moment later.
--
-- The library's own schema initialisation is off (namastack.outbox.jpa.schema-initialization.enabled: false), the
-- same as catalog-service, so the tables are declared here. Index names are schema-scoped, so these do not collide
-- with the other services'.
-- ---------------------------------------------------------------------------------------------------------------

create table if not exists merchant.outbox_record
(
    id             varchar(255)             not null,
    status         varchar(20)              not null,
    record_key     varchar(255)             not null,
    record_type    varchar(255)             not null,
    payload        text                     not null,
    context        text,
    created_at     timestamp with time zone not null,
    completed_at   timestamp with time zone,
    failure_count  int                      not null,
    failure_reason varchar(1000),
    next_retry_at  timestamp with time zone not null,
    partition_no   integer                  not null,
    handler_id     varchar(1000)            not null,
    primary key (id)
);

create table if not exists merchant.outbox_instance
(
    instance_id    varchar(255) primary key,
    hostname       varchar(255)             not null,
    port           integer                  not null,
    status         varchar(50)              not null,
    started_at     timestamp with time zone not null,
    last_heartbeat timestamp with time zone not null,
    created_at     timestamp with time zone not null,
    updated_at     timestamp with time zone not null
);

create table if not exists merchant.outbox_partition
(
    partition_number integer primary key,
    instance_id      varchar(255),
    version          bigint                   not null default 0,
    updated_at       timestamp with time zone not null
);

create index if not exists idx_outbox_record_record_key_created
    on merchant.outbox_record (record_key, created_at);
create index if not exists idx_outbox_record_partition_status_retry
    on merchant.outbox_record (partition_no, status, next_retry_at);
create index if not exists idx_outbox_record_status_retry
    on merchant.outbox_record (status, next_retry_at);
create index if not exists idx_outbox_record_status
    on merchant.outbox_record (status);
create index if not exists idx_outbox_record_record_key_completed_created
    on merchant.outbox_record (record_key, completed_at, created_at);
create index if not exists idx_outbox_instance_status_heartbeat
    on merchant.outbox_instance (status, last_heartbeat);
create index if not exists idx_outbox_instance_last_heartbeat
    on merchant.outbox_instance (last_heartbeat);
create index if not exists idx_outbox_instance_status
    on merchant.outbox_instance (status);
create index if not exists idx_outbox_partition_instance_id
    on merchant.outbox_partition (instance_id);
