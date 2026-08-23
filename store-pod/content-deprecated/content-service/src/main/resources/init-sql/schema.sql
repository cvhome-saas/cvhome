create schema if not exists content;
create table if not exists content.sm_sequencer
(
    seq_name  varchar(255) not null primary key,
    seq_count bigint
);
create table if not exists content.content
(
    content_id        bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    code              varchar(100) not null,
    content_position  varchar(10)
        constraint content_content_position_check check (
            (content_position)::text = any (array ['LEFT', 'RIGHT']::text[])
        ),
    content_type      varchar(10)
        constraint content_content_type_check check (
            (content_type)::text = any (array ['BOX', 'PAGE', 'SECTION']::text[])
        ),
    link_to_menu      boolean,
    product_group     varchar(255),
    sort_order        integer,
    visible           boolean,
    store_merchant_id varchar(50)  not null,
    constraint content_store_code_unique unique (store_merchant_id, code)
);
create index if not exists content_code_idx on content.content (code);
create table if not exists content.content_description
(
    description_id   bigint       not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id           varchar(60),
    description      text,
    name             varchar(120) not null,
    title            varchar(100),
    meta_description varchar(255),
    meta_keywords    varchar(255),
    meta_title       varchar(255),
    sef_url          varchar(120),
    language_code    varchar(6)   not null,
    content_id       bigint       not null
        constraint content_description_content_fk references content.content,
    constraint content_description_language_unique unique (content_id, language_code)
);
