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
        check (theme in ('BASIS','MODERN','JEWELERY','BEAUTY','FURNITURE','SPORTS','ELECTRONICS','FOOD','GLASSES',
                         'COSMETICS','WATCHES','BABY','TOOLS')),
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
