create schema if not exists content;
create extension if not exists pg_trgm;

create table if not exists content.sm_sequencer
(
    seq_name  varchar(255) not null primary key,
    seq_count bigint       not null
);

insert into content.sm_sequencer (seq_name, seq_count)
values ('CONTENT_SEQ_NEXT_VAL', 0),
       ('CONTENT_DESCRIPTION_SEQ_NEXT_VAL', 0),
       ('CONTENT_REVISION_SEQ_NEXT_VAL', 0),
       ('CONTENT_STATUS_AUDIT_SEQ_NEXT_VAL', 0),
       ('CONTENT_AUDIT_SEQ_NEXT_VAL', 0),
       ('PAGE_BLOCK_SEQ_NEXT_VAL', 0),
       ('CONTENT_REDIRECT_SEQ_NEXT_VAL', 0),
       ('CONTENT_PREVIEW_TOKEN_SEQ_NEXT_VAL', 0),
       ('POST_CATEGORY_SEQ_NEXT_VAL', 0),
       ('POST_CATEGORY_DESCRIPTION_SEQ_NEXT_VAL', 0),
       ('POST_TAG_SEQ_NEXT_VAL', 0),
       ('FAQ_GROUP_SEQ_NEXT_VAL', 0),
       ('FAQ_GROUP_DESCRIPTION_SEQ_NEXT_VAL', 0),
       ('FAQ_REFERENCE_SEQ_NEXT_VAL', 0),
       ('MENU_ITEM_SEQ_NEXT_VAL', 0),
       ('MEDIA_FOLDER_SEQ_NEXT_VAL', 0),
       ('MEDIA_ASSET_SEQ_NEXT_VAL', 0),
       ('MEDIA_ASSET_DESCRIPTION_SEQ_NEXT_VAL', 0),
       ('MEDIA_VARIANT_SEQ_NEXT_VAL', 0),
       ('MEDIA_TAG_SEQ_NEXT_VAL', 0),
       ('MEDIA_USAGE_SEQ_NEXT_VAL', 0)
on conflict (seq_name) do nothing;

create table if not exists content.content
(
    content_id        bigint                   not null primary key,
    date_created      timestamp with time zone not null,
    date_modified     timestamp with time zone,
    updt_id           varchar(60),
    store_merchant_id varchar(50)              not null,
    code              varchar(100)             not null,
    content_type      varchar(20)              not null,
    status            varchar(20)              not null,
    publish_at        timestamp with time zone,
    unpublish_at      timestamp with time zone,
    deleted_at        timestamp with time zone,
    version           bigint                   not null default 0,
    constraint content_store_code_unique unique (store_merchant_id, code),
    constraint content_identity_store_type_unique unique (content_id, store_merchant_id, content_type),
    constraint content_type_check check (content_type in ('PAGE', 'POST', 'BANNER', 'FAQ', 'MENU', 'POLICY')),
    constraint content_status_check check (
        status in ('DRAFT', 'IN_REVIEW', 'SCHEDULED', 'PUBLISHED', 'UNPUBLISHED', 'ARCHIVED', 'DELETED')
    ),
    constraint content_schedule_order_check check (unpublish_at is null or publish_at is null or unpublish_at > publish_at),
    constraint content_deleted_state_check check (deleted_at is null or status = 'DELETED')
);

create index if not exists content_store_status_idx
    on content.content (store_merchant_id, status);
create index if not exists content_store_type_status_idx
    on content.content (store_merchant_id, content_type, status);
create index if not exists content_publish_due_idx
    on content.content (publish_at) where status = 'SCHEDULED';
create index if not exists content_unpublish_due_idx
    on content.content (unpublish_at) where status = 'PUBLISHED' and unpublish_at is not null;

create table if not exists content.content_description
(
    description_id   bigint                   not null primary key,
    date_created     timestamp with time zone not null,
    date_modified    timestamp with time zone,
    updt_id           varchar(60),
    content_id       bigint                   not null,
    store_merchant_id varchar(50)             not null,
    content_type     varchar(20)              not null,
    language_code    varchar(6)               not null,
    translation_state varchar(20)             not null default 'DRAFT',
    name             varchar(255)             not null,
    title            varchar(255),
    description      text,
    meta_title       varchar(255),
    meta_description varchar(500),
    meta_keywords    varchar(500),
    sef_url          varchar(255),
    canonical_url    varchar(1000),
    og_media_id      bigint,
    no_index         boolean                  not null default false,
    constraint content_description_content_fk
        foreign key (content_id, store_merchant_id, content_type)
            references content.content (content_id, store_merchant_id, content_type) on delete cascade,
    constraint content_description_language_unique unique (content_id, language_code),
    constraint content_description_route_unique
        unique (store_merchant_id, content_type, language_code, sef_url),
    constraint content_description_type_check
        check (content_type in ('PAGE', 'POST', 'BANNER', 'FAQ', 'MENU', 'POLICY')),
    constraint content_description_translation_state_check
        check (translation_state in ('DRAFT', 'CURRENT', 'STALE'))
);

create index if not exists content_description_store_language_idx
    on content.content_description (store_merchant_id, language_code);
create index if not exists content_description_name_trgm_idx
    on content.content_description using gin (name gin_trgm_ops);
create index if not exists content_description_sef_url_trgm_idx
    on content.content_description using gin (sef_url gin_trgm_ops);

create table if not exists content.content_revision
(
    revision_id bigint                   not null primary key,
    content_id  bigint                   not null references content.content on delete cascade,
    version     bigint                   not null,
    snapshot    jsonb                    not null,
    author      varchar(255)             not null,
    created_at  timestamp with time zone not null,
    constraint content_revision_version_unique unique (content_id, version)
);

create table if not exists content.content_status_audit
(
    status_audit_id bigint                   not null primary key,
    content_id      bigint                   not null,
    store_merchant_id varchar(50)            not null,
    from_status     varchar(20),
    to_status       varchar(20)              not null,
    actor           varchar(255)             not null,
    reason          varchar(1000),
    occurred_at     timestamp with time zone not null,
    constraint content_status_audit_from_check check (
        from_status is null or from_status in
        ('DRAFT', 'IN_REVIEW', 'SCHEDULED', 'PUBLISHED', 'UNPUBLISHED', 'ARCHIVED', 'DELETED')
    ),
    constraint content_status_audit_to_check check (
        to_status in ('DRAFT', 'IN_REVIEW', 'SCHEDULED', 'PUBLISHED', 'UNPUBLISHED', 'ARCHIVED', 'DELETED')
    )
);

create index if not exists content_status_audit_store_content_idx
    on content.content_status_audit (store_merchant_id, content_id, occurred_at desc);

create table if not exists content.content_audit
(
    audit_id         bigint                   not null primary key,
    content_id       bigint                   not null,
    store_merchant_id varchar(50)             not null,
    action           varchar(50)              not null,
    actor            varchar(255)             not null,
    ip_address       varchar(64),
    user_agent       varchar(1000),
    before_summary   jsonb,
    after_summary    jsonb,
    occurred_at      timestamp with time zone not null
);

create index if not exists content_audit_store_content_idx
    on content.content_audit (store_merchant_id, content_id, occurred_at desc);

create table if not exists content.content_search_document
(
    content_id        bigint      not null references content.content on delete cascade,
    store_merchant_id varchar(50) not null,
    language_code     varchar(6)  not null,
    searchable_text   text        not null,
    search_vector     tsvector generated always as (to_tsvector('simple', searchable_text)) stored,
    primary key (content_id, language_code)
);

create index if not exists content_search_store_language_idx
    on content.content_search_document (store_merchant_id, language_code);
create index if not exists content_search_vector_idx
    on content.content_search_document using gin (search_vector);
create index if not exists content_search_text_trgm_idx
    on content.content_search_document using gin (searchable_text gin_trgm_ops);

create table if not exists content.media_folder
(
    folder_id         bigint                   not null primary key,
    store_merchant_id varchar(50)              not null,
    parent_folder_id  bigint references content.media_folder on delete cascade,
    name              varchar(255)             not null,
    date_created      timestamp with time zone not null,
    date_modified     timestamp with time zone,
    updt_id           varchar(60),
    version           bigint                   not null default 0
);

create unique index if not exists media_folder_root_name_unique
    on content.media_folder (store_merchant_id, name) where parent_folder_id is null;
create unique index if not exists media_folder_child_name_unique
    on content.media_folder (store_merchant_id, parent_folder_id, name) where parent_folder_id is not null;

create table if not exists content.media_asset
(
    asset_id           bigint                   not null primary key,
    store_merchant_id  varchar(50)              not null,
    folder_id          bigint references content.media_folder on delete set null,
    original_filename  varchar(500)             not null,
    normalized_filename varchar(500)            not null,
    detected_mime      varchar(100)             not null,
    media_kind         varchar(20)              not null,
    byte_size          bigint                   not null,
    checksum           char(64)                 not null,
    width              integer,
    height             integer,
    page_count         integer,
    storage_key        varchar(1000)            not null,
    processing_status  varchar(20)              not null,
    failure_reason     varchar(1000),
    deleted_at         timestamp with time zone,
    date_created       timestamp with time zone not null,
    date_modified      timestamp with time zone,
    updt_id            varchar(60),
    version            bigint                   not null default 0,
    constraint media_asset_store_checksum_unique unique (store_merchant_id, checksum),
    constraint media_asset_storage_key_unique unique (storage_key),
    constraint media_asset_kind_check check (media_kind in ('IMAGE', 'DOCUMENT')),
    constraint media_asset_processing_status_check
        check (processing_status in ('PROCESSING', 'READY', 'FAILED')),
    constraint media_asset_size_check check (byte_size > 0 and byte_size <= 52428800),
    constraint media_asset_dimensions_check check (
        (media_kind = 'IMAGE' and width is not null and height is not null and width > 0 and height > 0
            and page_count is null)
        or (media_kind = 'DOCUMENT' and width is null and height is null and page_count is not null
            and page_count > 0)
    )
);

create index if not exists media_asset_store_status_idx
    on content.media_asset (store_merchant_id, processing_status, deleted_at);
create index if not exists media_asset_store_folder_idx
    on content.media_asset (store_merchant_id, folder_id);

create table if not exists content.media_asset_description
(
    description_id bigint       not null primary key,
    asset_id       bigint       not null references content.media_asset on delete cascade,
    language_code  varchar(6)   not null,
    alt_text       varchar(500),
    title          varchar(255),
    caption        text,
    constraint media_asset_description_language_unique unique (asset_id, language_code)
);

create table if not exists content.media_variant
(
    variant_id  bigint        not null primary key,
    asset_id    bigint        not null references content.media_asset on delete cascade,
    variant_name varchar(30)  not null,
    format      varchar(10)   not null,
    width       integer       not null,
    height      integer       not null,
    byte_size   bigint        not null,
    storage_key varchar(1000) not null,
    constraint media_variant_asset_name_unique unique (asset_id, variant_name),
    constraint media_variant_storage_key_unique unique (storage_key),
    constraint media_variant_name_check check (variant_name in ('THUMB_320', 'CARD_640', 'HERO_1600', 'FULL_1920')),
    constraint media_variant_format_check check (format in ('JPEG', 'PNG')),
    constraint media_variant_values_check check (width > 0 and height > 0 and byte_size > 0)
);

create table if not exists content.media_tag
(
    tag_id            bigint       not null primary key,
    store_merchant_id varchar(50)  not null,
    name               varchar(100) not null,
    normalized_name    varchar(100) not null,
    constraint media_tag_store_name_unique unique (store_merchant_id, normalized_name)
);

create table if not exists content.media_asset_tag
(
    asset_id bigint not null references content.media_asset on delete cascade,
    tag_id   bigint not null references content.media_tag on delete cascade,
    primary key (asset_id, tag_id)
);

create table if not exists content.media_usage
(
    usage_id        bigint      not null primary key,
    asset_id        bigint      not null references content.media_asset on delete restrict,
    content_id      bigint      not null references content.content on delete cascade,
    usage_type      varchar(30) not null,
    field_reference varchar(255) not null,
    constraint media_usage_logical_unique unique (asset_id, content_id, usage_type, field_reference),
    constraint media_usage_type_check check (
        usage_type in ('DESCRIPTION_OG', 'PAGE_BLOCK', 'POST_HERO', 'BANNER_DESKTOP', 'BANNER_MOBILE')
    )
);

alter table content.content_description
    drop constraint if exists content_description_og_media_fk;
alter table content.content_description
    add constraint content_description_og_media_fk
        foreign key (og_media_id) references content.media_asset on delete set null;

create table if not exists content.content_page
(
    content_id      bigint       not null primary key references content.content on delete cascade,
    template        varchar(100) not null,
    show_in_sitemap boolean      not null default true,
    parent_page_id  bigint references content.content_page on delete set null
);

create table if not exists content.page_block
(
    block_id       bigint      not null primary key,
    page_content_id bigint     not null references content.content_page on delete cascade,
    block_type     varchar(30) not null,
    position       integer     not null,
    payload        jsonb       not null,
    constraint page_block_position_unique unique (page_content_id, position),
    constraint page_block_type_check check (block_type in (
        'RICH_TEXT', 'IMAGE', 'GALLERY', 'VIDEO_LINK', 'PRODUCT_GRID', 'FAQ_REFERENCE',
        'BANNER_REFERENCE', 'PAGE_REFERENCE', 'HTML_EMBED', 'SPACER', 'CTA'
    )),
    constraint page_block_position_check check (position >= 0)
);

alter table content.page_block
    drop constraint if exists page_block_type_check;
alter table content.page_block
    add constraint page_block_type_check check (block_type in (
        'RICH_TEXT', 'IMAGE', 'GALLERY', 'VIDEO_LINK', 'PRODUCT_GRID', 'FAQ_REFERENCE',
        'BANNER_REFERENCE', 'PAGE_REFERENCE', 'HTML_EMBED', 'SPACER', 'CTA'
    ));

create table if not exists content.page_block_description
(
    block_id      bigint     not null references content.page_block on delete cascade,
    language_code varchar(6) not null,
    payload       jsonb      not null,
    primary key (block_id, language_code)
);

create table if not exists content.content_redirect
(
    redirect_id       bigint       not null primary key,
    store_merchant_id varchar(50)  not null,
    language_code     varchar(6)   not null,
    old_path          varchar(500) not null,
    destination_content_id bigint not null references content.content on delete cascade,
    http_status       smallint     not null default 301,
    constraint content_redirect_old_path_unique unique (store_merchant_id, language_code, old_path),
    constraint content_redirect_status_check check (http_status in (301, 308))
);

create table if not exists content.content_preview_token
(
    preview_token_id bigint                   not null primary key,
    token_hash       char(64)                 not null unique,
    content_id       bigint                   not null references content.content on delete cascade,
    expires_at       timestamp with time zone not null,
    revoked_at       timestamp with time zone,
    created_at       timestamp with time zone not null
);

create index if not exists content_preview_token_content_idx
    on content.content_preview_token (content_id, expires_at);

create table if not exists content.content_post
(
    content_id      bigint       not null primary key references content.content on delete cascade,
    hero_media_id   bigint references content.media_asset on delete set null,
    author_snapshot varchar(255) not null,
    reading_minutes integer      not null,
    featured        boolean      not null default false,
    excerpt          text         not null,
    constraint content_post_reading_minutes_check check (reading_minutes > 0)
);

create table if not exists content.content_post_description
(
    description_id bigint not null primary key references content.content_description on delete cascade,
    excerpt        text   not null
);

create table if not exists content.post_category
(
    category_id       bigint       not null primary key,
    store_merchant_id varchar(50)  not null,
    code              varchar(100) not null,
    constraint post_category_store_code_unique unique (store_merchant_id, code)
);

create table if not exists content.post_category_description
(
    category_description_id bigint       not null primary key,
    category_id              bigint       not null references content.post_category on delete cascade,
    language_code            varchar(6)   not null,
    name                     varchar(255) not null,
    slug                     varchar(255) not null,
    constraint post_category_description_language_unique unique (category_id, language_code)
);

create table if not exists content.post_tag
(
    tag_id            bigint       not null primary key,
    store_merchant_id varchar(50)  not null,
    name              varchar(100) not null,
    normalized_name   varchar(100) not null,
    constraint post_tag_store_name_unique unique (store_merchant_id, normalized_name)
);

create table if not exists content.content_post_category
(
    post_content_id bigint not null references content.content_post on delete cascade,
    category_id    bigint not null references content.post_category on delete restrict,
    primary key (post_content_id, category_id)
);

create table if not exists content.content_post_tag
(
    post_content_id bigint not null references content.content_post on delete cascade,
    tag_id          bigint not null references content.post_tag on delete restrict,
    primary key (post_content_id, tag_id)
);

create table if not exists content.content_banner
(
    content_id       bigint       not null primary key references content.content on delete cascade,
    placement        varchar(30)  not null,
    position         integer      not null,
    target_kind      varchar(30)  not null,
    target_value     varchar(1000),
    background_color varchar(20),
    foreground_color varchar(20),
    logged_in_target varchar(20)  not null default 'ANY',
    constraint content_banner_placement_check check (placement in ('HOME_HERO', 'HOME_SECONDARY', 'CATEGORY', 'CHECKOUT')),
    constraint content_banner_target_kind_check check (target_kind in ('NONE', 'URL', 'CONTENT', 'PRODUCT', 'CATEGORY')),
    constraint content_banner_login_check check (logged_in_target in ('ANY', 'ANONYMOUS', 'AUTHENTICATED')),
    constraint content_banner_position_check check (position >= 0)
);

create table if not exists content.content_banner_description
(
    description_id bigint not null primary key references content.content_description on delete cascade,
    headline       varchar(255),
    subhead        text,
    cta_label      varchar(100)
);

create table if not exists content.banner_artwork
(
    content_id       bigint     not null references content.content_banner on delete cascade,
    language_code    varchar(6) not null,
    desktop_media_id bigint references content.media_asset on delete set null,
    mobile_media_id  bigint references content.media_asset on delete set null,
    alt_text         varchar(500),
    primary key (content_id, language_code)
);

create table if not exists content.banner_country
(
    content_id  bigint    not null references content.content_banner on delete cascade,
    country_code char(2)  not null,
    primary key (content_id, country_code)
);

create table if not exists content.faq_group
(
    group_id          bigint       not null primary key,
    store_merchant_id varchar(50)  not null,
    code              varchar(100) not null,
    position          integer      not null,
    constraint faq_group_store_code_unique unique (store_merchant_id, code),
    constraint faq_group_position_check check (position >= 0)
);

create table if not exists content.faq_group_description
(
    group_description_id bigint       not null primary key,
    group_id             bigint       not null references content.faq_group on delete cascade,
    language_code        varchar(6)   not null,
    name                 varchar(255) not null,
    constraint faq_group_description_language_unique unique (group_id, language_code)
);

create table if not exists content.content_faq
(
    content_id bigint  not null primary key references content.content on delete cascade,
    group_id   bigint  not null references content.faq_group on delete restrict,
    position   integer not null,
    constraint content_faq_group_position_unique unique (group_id, position),
    constraint content_faq_position_check check (position >= 0)
);

create table if not exists content.faq_reference
(
    reference_id   bigint        not null primary key,
    faq_content_id bigint        not null references content.content_faq on delete cascade,
    reference_kind varchar(20)   not null,
    reference_value varchar(255) not null,
    constraint faq_reference_logical_unique unique (faq_content_id, reference_kind, reference_value),
    constraint faq_reference_kind_check check (reference_kind in ('PRODUCT', 'PAGE'))
);

create table if not exists content.content_menu
(
    content_id        bigint       not null primary key references content.content on delete cascade,
    store_merchant_id varchar(50)  not null,
    handle            varchar(100) not null,
    constraint content_menu_store_handle_unique unique (store_merchant_id, handle)
);

create table if not exists content.menu_item
(
    menu_item_id  bigint        not null primary key,
    menu_content_id bigint      not null references content.content_menu on delete cascade,
    parent_item_id bigint references content.menu_item on delete cascade,
    position      integer       not null,
    target_kind   varchar(20)   not null,
    target_value  varchar(1000),
    open_new_tab  boolean       not null default false,
    visible       boolean       not null default true,
    login_required boolean      not null default false,
    constraint menu_item_position_check check (position >= 0),
    constraint menu_item_target_kind_check check (target_kind in ('URL', 'CONTENT', 'PRODUCT', 'CATEGORY'))
);

create unique index if not exists menu_item_root_position_unique
    on content.menu_item (menu_content_id, position) where parent_item_id is null;
create unique index if not exists menu_item_child_position_unique
    on content.menu_item (menu_content_id, parent_item_id, position) where parent_item_id is not null;

create table if not exists content.menu_item_description
(
    menu_item_id  bigint       not null references content.menu_item on delete cascade,
    language_code varchar(6)   not null,
    label         varchar(255) not null,
    primary key (menu_item_id, language_code)
);

create table if not exists content.content_policy
(
    content_id          bigint       not null primary key references content.content on delete cascade,
    store_merchant_id   varchar(50)  not null,
    policy_type         varchar(30)  not null,
    policy_version      varchar(50)  not null,
    effective_date      date         not null,
    acceptance_required boolean      not null default false,
    jurisdiction        varchar(100),
    active              boolean      not null default false,
    constraint content_policy_store_type_version_unique
        unique (store_merchant_id, policy_type, policy_version),
    constraint content_policy_type_check
        check (policy_type in ('PRIVACY', 'TERMS', 'REFUND', 'SHIPPING', 'COOKIE'))
);

create unique index if not exists content_policy_one_active_type_unique
    on content.content_policy (store_merchant_id, policy_type) where active;

create table if not exists content.policy_display_location
(
    policy_content_id bigint      not null references content.content_policy on delete cascade,
    display_location  varchar(30) not null,
    primary key (policy_content_id, display_location),
    constraint policy_display_location_check check (display_location in ('FOOTER', 'CHECKOUT', 'REGISTRATION'))
);

create table if not exists content.outbox_record
(
    id             varchar(255)             not null primary key,
    status         varchar(20)              not null,
    record_key     varchar(255)             not null,
    record_type    varchar(255)             not null,
    payload        text                     not null,
    context        text,
    created_at     timestamp with time zone not null,
    completed_at   timestamp with time zone,
    failure_count  integer                  not null,
    failure_reason varchar(1000),
    next_retry_at  timestamp with time zone not null,
    partition_no   integer                  not null,
    handler_id     varchar(1000)            not null
);

create table if not exists content.outbox_instance
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

create table if not exists content.outbox_partition
(
    partition_number integer primary key,
    instance_id      varchar(255),
    version          bigint                   not null default 0,
    updated_at       timestamp with time zone not null
);

create index if not exists content_outbox_record_key_created_idx
    on content.outbox_record (record_key, created_at);
create index if not exists content_outbox_partition_status_retry_idx
    on content.outbox_record (partition_no, status, next_retry_at);
create index if not exists content_outbox_status_retry_idx
    on content.outbox_record (status, next_retry_at);
create index if not exists content_outbox_record_key_completed_idx
    on content.outbox_record (record_key, completed_at, created_at);
create index if not exists content_outbox_instance_status_heartbeat_idx
    on content.outbox_instance (status, last_heartbeat);
create index if not exists content_outbox_partition_instance_idx
    on content.outbox_partition (instance_id);
