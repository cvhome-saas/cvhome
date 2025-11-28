create schema if not exists merchant;
create table if not exists merchant.sm_sequencer
(
    seq_name  varchar(255) not null primary key,
    seq_count bigint
);
create table if not exists merchant.merchant_store
(
    store_merchant_id        varchar(50)  not null primary key,
    date_created             timestamp(6),
    date_modified            timestamp(6),
    updt_id                  varchar(60),
    continueshoppingurl      varchar(150),
    currency_format_national boolean,
    domain_name              varchar(80),
    in_business_since        date,
    invoice_template         varchar(25),
    lineage                  varchar(255),
    org                      varchar(255) not null,
    theme                    varchar(25)  not null,
    color_theme              varchar(25)  not null,
    seizeunitcode            varchar(5),
    store_email              varchar(60)  not null,
    store_logo               varchar(100),
    store_banner             varchar(100),
    store_template           varchar(25),
    store_address            varchar(255),
    store_city               varchar(100),
    store_name               varchar(100) not null,
    store_phone              varchar(50),
    store_postal_code        varchar(15),
    store_state_prov         varchar(100),
    use_cache                boolean,
    weightunitcode           varchar(5),
    country_id               varchar(6)   not null,
    currency_id              varchar(6)   not null,
    language_code            varchar(6)   not null,
    zone_id                  varchar(100)
);
create table if not exists merchant.merchant_language
(
    store_merchant_id varchar(50) not null
        constraint FK14ylsv0o3x2vdww6ts9yx6nyi references merchant.merchant_store (store_merchant_id),
    language_code     varchar(6)  not null,
    constraint merchant_language_pk
        unique (store_merchant_id, language_code)
);
create table if not exists merchant.merchant_slider_images
(
    store_merchant_id varchar(50)  not null
        constraint fk6qg1wx3ow5v07pswgwf8dbguf references merchant.merchant_store,
    priority          int,
    name              varchar(100) not null
);
create table if not exists merchant.social_links
(
    store_merchant_id varchar(50) not null
        constraint fkfh74uew5thxc4fpf90vs8ebsy references merchant.merchant_store,
    provider          varchar(10),
    url               varchar(100)
);

create table if not exists merchant.store_domains
(
    store_merchant_id varchar(50) not null,
    domain            varchar(100),
    domain_type       varchar(15) not null,
    constraint FKpw0mfwlhf9uay27vw3sbal8ao foreign key (store_merchant_id) references merchant.merchant_store
);

create table if not exists merchant.content
(
    content_id        bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    code              varchar(100) not null,
    content_position  varchar(10)
        constraint content_content_position_check check (
            (content_position):: text = ANY (
                (
                    ARRAY [ 'LEFT' :: character varying,
                        'RIGHT' :: character varying]
                    ):: text[]
                )
            ),
    content_type      varchar(10)
        constraint content_content_type_check check (
            (content_type):: text = ANY (
                (
                    ARRAY [ 'BOX' :: character varying, 'PAGE' :: character varying,
                        'SECTION' :: character varying]
                    ):: text[]
                )
            ),
    link_to_menu      boolean,
    product_group     varchar(255),
    sort_order        integer,
    visible           boolean,
    store_merchant_id varchar(50)  not null,
    constraint UKbm9y7874o2jvipd27cixy6hig unique (store_merchant_id, code)
);
create index if not exists code_idx on merchant.content (code);
create table if not exists merchant.content_description
(
    description_id   bigint       not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    description      text,
    name             varchar(120) not null,
    title            varchar(100),
    meta_description varchar(255),
    meta_keywords    varchar(255),
    meta_title       varchar(255),
    sef_url          varchar(120),
    language_code    varchar(6)   not null,
    content_id       bigint       not null
        constraint fklu77slp5yqo5p281lk6vdpppi references merchant.content,
    constraint ukfrkqw8gr1kexiuv0h76s6xvpr unique (content_id, language_code)
);
