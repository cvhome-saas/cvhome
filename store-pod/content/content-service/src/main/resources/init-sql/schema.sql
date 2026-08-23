-- Content platform DDL. This file is the source of truth; hibernate ddl-auto is only a safety net.
--
-- It is written as a migration: the two legacy tables (content, content_description) are created if missing and
-- then extended with "add column if not exists … default …", so a database created by the previous content
-- service is upgraded in place, and a fresh database ends up identical.

create schema if not exists content;

create table if not exists content.sm_sequencer
(
    seq_name  varchar(255) not null primary key,
    seq_count bigint
);

create sequence if not exists content.content_seq increment by 50;

-- ---------------------------------------------------------------------------------------------------------------
-- content (legacy, extended)
-- ---------------------------------------------------------------------------------------------------------------
create table if not exists content.content
(
    content_id        bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    code              varchar(100) not null,
    content_position  varchar(10),
    content_type      varchar(10),
    link_to_menu      boolean,
    product_group     varchar(255),
    sort_order        integer,
    visible           boolean,
    store_merchant_id varchar(50)  not null,
    constraint content_store_code_unique unique (store_merchant_id, code)
);
create index if not exists content_code_idx on content.content (code);

-- widen the type check (legacy databases carry the 3-value one under this name)
alter table content.content drop constraint if exists content_content_type_check;
alter table content.content add constraint content_content_type_check
    check (content_type in ('BOX', 'PAGE', 'SECTION', 'POST', 'BANNER', 'FAQ', 'POLICY'));
alter table content.content drop constraint if exists content_content_position_check;
alter table content.content add constraint content_content_position_check
    check (content_position is null or content_position in ('LEFT', 'RIGHT'));

alter table content.content add column if not exists status         varchar(12) not null default 'DRAFT';
alter table content.content add column if not exists publish_at     timestamp(6);
alter table content.content add column if not exists unpublish_at   timestamp(6);
alter table content.content add column if not exists version        integer     not null default 1;
alter table content.content add column if not exists created_by     varchar(120);
alter table content.content add column if not exists updated_by     varchar(120);
alter table content.content add column if not exists parent_id      bigint;
alter table content.content add column if not exists template       varchar(20);
alter table content.content add column if not exists noindex        boolean     not null default false;
alter table content.content add column if not exists canonical_url  varchar(500);
alter table content.content add column if not exists og_media_id    bigint;
alter table content.content add column if not exists show_in_footer boolean     not null default false;
alter table content.content add column if not exists placement      varchar(20);
alter table content.content add column if not exists starts_at      timestamp(6);
alter table content.content add column if not exists ends_at        timestamp(6);
alter table content.content add column if not exists policy_type    varchar(20);
alter table content.content add column if not exists meta           jsonb;

alter table content.content drop constraint if exists content_status_check;
alter table content.content add constraint content_status_check
    check (status in ('DRAFT', 'REVIEW', 'SCHEDULED', 'PUBLISHED', 'ARCHIVED'));
alter table content.content drop constraint if exists content_template_check;
alter table content.content add constraint content_template_check
    check (template is null or template in ('STANDARD', 'LANDING', 'CONTACT', 'FAQ_PAGE'));
alter table content.content drop constraint if exists content_placement_check;
alter table content.content add constraint content_placement_check
    check (placement is null or placement in ('HERO', 'CAROUSEL', 'COLLECTION', 'STRIP'));
alter table content.content drop constraint if exists content_policy_type_check;
alter table content.content add constraint content_policy_type_check
    check (policy_type is null or policy_type in ('TERMS', 'PRIVACY', 'RETURNS', 'SHIPPING', 'COOKIES', 'CUSTOM'));

create index if not exists content_store_type_status_idx on content.content (store_merchant_id, content_type, status);
create index if not exists content_status_publish_at_idx on content.content (status, publish_at);
create index if not exists content_store_placement_idx on content.content (store_merchant_id, placement, status);

-- migration of legacy rows: what was visible is published; BOX rows mirror visibility
update content.content
   set status = 'PUBLISHED'
 where visible = true and status = 'DRAFT' and created_by is null;

-- ---------------------------------------------------------------------------------------------------------------
-- content_description (legacy, extended)
-- ---------------------------------------------------------------------------------------------------------------
create table if not exists content.content_description
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
        constraint content_description_content_fk references content.content,
    constraint content_description_language_unique unique (content_id, language_code)
);

alter table content.content_description add column if not exists state     varchar(12) not null default 'TRANSLATED';
alter table content.content_description add column if not exists excerpt   varchar(300);
alter table content.content_description add column if not exists alt_text  varchar(255);
alter table content.content_description add column if not exists cta_label varchar(60);
alter table content.content_description add column if not exists subtitle  varchar(300);
alter table content.content_description drop constraint if exists content_description_state_check;
alter table content.content_description add constraint content_description_state_check
    check (state in ('MISSING', 'DRAFT', 'TRANSLATED', 'STALE'));
create index if not exists content_description_content_idx on content.content_description (content_id);

-- ---------------------------------------------------------------------------------------------------------------
-- revisions, status audit, redirects
-- ---------------------------------------------------------------------------------------------------------------
create table if not exists content.content_revision
(
    id                bigint      not null primary key,
    store_merchant_id varchar(50) not null,
    content_id        bigint      not null,
    version           integer     not null,
    snapshot          jsonb       not null,
    author            varchar(120),
    created_at        timestamp(6) not null,
    constraint content_revision_unique unique (content_id, version)
);
create index if not exists content_revision_content_idx on content.content_revision (content_id, version desc);

create table if not exists content.content_status_audit
(
    id                bigint      not null primary key,
    store_merchant_id varchar(50) not null,
    content_id        bigint      not null,
    from_status       varchar(12),
    to_status         varchar(12) not null,
    actor             varchar(120),
    reason            varchar(255),
    occurred_at       timestamp(6) not null
);
create index if not exists content_status_audit_content_idx on content.content_status_audit (content_id, occurred_at desc);

create table if not exists content.redirect
(
    id                bigint       not null primary key,
    store_merchant_id varchar(50)  not null,
    from_path         varchar(255) not null,
    to_path           varchar(255) not null,
    created_at        timestamp(6) not null,
    constraint redirect_store_from_unique unique (store_merchant_id, from_path)
);
