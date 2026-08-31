create schema if not exists catalog;
create table if not exists catalog.sm_sequencer
(
    seq_name  varchar(255) not null primary key,
    seq_count bigint
);
create table if not exists catalog.category
(
    category_id       bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    category_image    varchar(100),
    category_status   boolean,
    code              varchar(100) not null,
    depth             integer,
    featured          boolean,
    lineage           varchar(255),
    sort_order        integer,
    visible           boolean,
    store_merchant_id varchar(50)  not null,
    parent_id         bigint
        constraint fk2y94svpmqttx80mshyny85wqr references catalog.category,
    constraint UKbskw32wfw0rrbllmc0xlwo01d unique (store_merchant_id, code)
);
create index if not exists idxiekj8rpfx83k5nww1flbu7tpb on catalog.category (lineage);
create table if not exists catalog.category_description
(
    description_id     bigint       not null primary key,
    date_created       timestamp(6),
    date_modified      timestamp(6),
    updt_id            varchar(60),
    description        text,
    name               varchar(120) not null,
    title              varchar(100),
    category_highlight varchar(255),
    meta_description   varchar(255),
    meta_keywords      varchar(255),
    meta_title         varchar(120),
    sef_url            varchar(120),
    language_code      varchar(6)   not null,
    category_id        bigint       not null
        constraint fkcf1yvvfw0o7fvhxpryuetekcb references catalog.category,
    constraint UKc5ylgobini29a15xcwcwhiksu unique (category_id, language_code)
);
create table if not exists catalog.manufacturer
(
    manufacturer_id    bigint       not null primary key,
    date_created       timestamp(6),
    date_modified      timestamp(6),
    updt_id            varchar(60),
    code               varchar(100) not null,
    manufacturer_image varchar(255),
    sort_order         integer,
    store_merchant_id  varchar(50)  not null,
    constraint UKkdwu1lqah54ppnfwpvksoyve1 unique (store_merchant_id, code)
);
create table if not exists catalog.manufacturer_description
(
    description_id    bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    description       text,
    name              varchar(120) not null,
    title             varchar(100),
    date_last_click   timestamp(6),
    manufacturers_url varchar(255),
    url_clicked       integer,
    language_code     varchar(6)   not null,
    manufacturer_id   bigint       not null
        constraint fk2cpxn0kaionj660yaqdln4sfi references catalog.manufacturer,
    constraint UKebgisqk3yxc370rlqxn8o621f unique (manufacturer_id, language_code)
);
create table if not exists catalog.product_type
(
    product_type_id      bigint not null primary key,
    prd_type_add_to_cart boolean,
    date_created         timestamp(6),
    date_modified        timestamp(6),
    updt_id              varchar(60),
    prd_type_code        varchar(255),
    prd_type_visible     boolean,
    store_merchant_id    varchar(50)
);
create table if not exists catalog.product_type_description
(
    description_id  bigint       not null primary key,
    date_created    timestamp(6),
    date_modified   timestamp(6),
    updt_id         varchar(60),
    description     text,
    name            varchar(120) not null,
    title           varchar(100),
    language_code   varchar(6)   not null,
    product_type_id bigint       not null
        constraint fk5yingh0egjkus0xfkl1hhmwy references catalog.product_type,
    constraint UKedftn4kxppmgot0f38hvk83sm unique (product_type_id, language_code)
);

-- The store's option vocabulary (Color, Size, ...): defined once per store, translated once, reused by any
-- product that assigns it. Value ids are store-wide, which is what makes id-based option faceting possible.
create table if not exists catalog.product_option
(
    product_option_id bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    code              varchar(100) not null,
    sort_order        integer,
    store_merchant_id varchar(50)  not null,
    constraint uk_product_option_code unique (store_merchant_id, code)
);
create table if not exists catalog.product_option_description
(
    description_id    bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    description       text,
    name              varchar(120) not null,
    title             varchar(100),
    language_code     varchar(6)   not null,
    product_option_id bigint       not null
        constraint fk_prd_opt_desc_option references catalog.product_option,
    constraint uk_product_option_desc unique (product_option_id, language_code)
);
create table if not exists catalog.product_option_value
(
    product_option_value_id bigint       not null primary key,
    date_created            timestamp(6),
    date_modified           timestamp(6),
    updt_id                 varchar(60),
    code                    varchar(100) not null,
    sort_order              integer,
    product_option_id       bigint       not null
        constraint fk_prd_opt_value_option references catalog.product_option,
    constraint uk_product_option_value_code unique (product_option_id, code)
);
create table if not exists catalog.product_option_value_description
(
    description_id          bigint       not null primary key,
    date_created            timestamp(6),
    date_modified           timestamp(6),
    updt_id                 varchar(60),
    description             text,
    name                    varchar(120) not null,
    title                   varchar(100),
    language_code           varchar(6)   not null,
    product_option_value_id bigint       not null
        constraint fk_prd_opt_value_desc_value references catalog.product_option_value,
    constraint uk_product_option_value_desc unique (product_option_value_id, language_code)
);

create table if not exists catalog.product
(
    product_id        bigint      not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    available         boolean,
    cond              smallint
        constraint product_cond_check check (
            (cond >= 0)
                AND (cond <= 1)
            ),
    date_available    timestamp(6),
    preorder          boolean,
    product_height    numeric(38, 2),
    product_free      boolean,
    product_length    numeric(38, 2),
    quantity_ordered  integer,
    review_avg        numeric(38, 2),
    review_count      integer,
    product_ship      boolean,
    product_virtual   boolean,
    product_weight    numeric(38, 2),
    product_width     numeric(38, 2),
    ref_sku           varchar(255),
    rental_duration   integer,
    rental_period     integer,
    rental_status     smallint
        constraint product_rental_status_check check (
            (rental_status >= 0)
                AND (rental_status <= 1)
            ),
    sku               varchar(255),
    sort_order        integer,
    manufacturer_id   bigint
        constraint fk89igr5j06uw5ps04djxgom0l1 references catalog.manufacturer,
    store_merchant_id varchar(50) not null,
    product_type_id   bigint
        constraint fklabq3c2e90ybbxk58rc48byqo references catalog.product_type,
    constraint UK8y3h56fhn50m59svlocxwqnn0 unique (store_merchant_id, sku)
);
create table if not exists catalog.product_category
(
    product_id  bigint not null
        constraint fk2k3smhbruedlcrvu6clued06x references catalog.product,
    category_id bigint not null
        constraint fkkud35ls1d40wpjb5htpp14q4e references catalog.category,
    primary key (product_id, category_id)
);
create table if not exists catalog.product_description
(
    description_id    bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    description       text,
    name              varchar(120) not null,
    title             varchar(100),
    meta_description  varchar(255),
    meta_keywords     varchar(255),
    meta_title        varchar(255),
    download_lnk      varchar(255),
    product_highlight varchar(255),
    sef_url           varchar(255),
    language_code     varchar(6)   not null,
    product_id        bigint       not null
        constraint fk9iiotbwtk1n1b6dgga729sg9q references catalog.product,
    constraint UKlw13d26xneb2dsyd1q2rbwqqc unique (product_id, language_code)
);
create index if not exists product_description_sef_url on catalog.product_description (sef_url);
-- A product image is a reference into the content service's media library, not a file catalog owns. media_asset_id
-- is the source of truth; image_url caches the asset's public URL so reading a product needs no cross-service
-- call. That cache is safe because an asset's bytes are never replaced in place — an upload either deduplicates
-- onto the existing asset or mints a new id — and content refuses to delete one a product still references.
create table if not exists catalog.product_image
(
    product_image_id  bigint  not null primary key,
    default_image     boolean,
    image_type        integer,
    media_asset_id    bigint,
    image_url         varchar(500),
    alt_text          varchar(255),
    product_image_url varchar(500),
    sort_order        integer,
    product_id        bigint  not null
        constraint fk6oo0cvcdtb6qmwsga468uuukk references catalog.product
);
alter table catalog.product_image add column if not exists media_asset_id bigint;
alter table catalog.product_image add column if not exists image_url      varchar(500);
alter table catalog.product_image add column if not exists alt_text       varchar(255);
alter table catalog.product_image drop column if exists product_image;
alter table catalog.product_image drop column if exists image_crop;
create index if not exists product_image_media_idx on catalog.product_image (media_asset_id);
create table if not exists catalog.product_group
(
    product_group_id  bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    active            boolean,
    code              varchar(100) not null,
    store_merchant_id varchar(50)  not null,
    parent_product_id bigint
        constraint fk_product_group_parent references catalog.product,
    constraint UK_product_group_code unique (store_merchant_id, code)
);

create table if not exists catalog.product_group_description
(
    description_id   bigint       not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    description      text,
    name             varchar(120) not null,
    title            varchar(120),
    meta_description varchar(255),
    meta_keywords    varchar(255),
    meta_title       varchar(120),
    sef_url          varchar(120),
    language_code    varchar(6)   not null,
    product_group_id bigint       not null
        constraint fk_product_group_desc references catalog.product_group,
    constraint UK_product_group_desc unique (product_group_id, language_code)
);

create table if not exists catalog.product_group_product
(
    product_group_id bigint not null
        constraint fk_pgp_group references catalog.product_group,
    product_id       bigint not null
        constraint fk_pgp_product references catalog.product,
    primary key (product_group_id, product_id)
);

-- ---------------------------------------------------------------------------------------------------------------
-- Product search
--
-- Full-text search over the catalogue, per store and per language. The searchable document is materialised into
-- catalog.product_search_index rather than derived at query time: the document spans product, product_description
-- and manufacturer_description, and a query has to reach it through one index scan, not three joins.
--
-- The table is not mapped for writing by JPA. It is refreshed by catalog.refresh_product_search_index, which the
-- outbox handler calls when a ProductSearchIndexStaleEvent is processed. Keeping the document shape here — and not
-- in Java — is what guarantees the index side and the query side normalise text the same way; a mismatch there
-- does not fail loudly, it just silently stops matching.
-- ---------------------------------------------------------------------------------------------------------------

create extension if not exists unaccent with schema public;
create extension if not exists pg_trgm with schema public;
create extension if not exists btree_gin with schema public;

-- The snowball configuration for a store language. Postgres ships stemmers for all five storefront locales.
-- Immutable so it folds into a constant when the planner sees a literal language.
create or replace function catalog.search_config(p_language varchar) returns regconfig
    language sql immutable parallel safe as
$$
select case lower(left(coalesce(p_language, 'en'), 2))
           when 'ar' then 'arabic'::regconfig
           when 'en' then 'english'::regconfig
           when 'fr' then 'french'::regconfig
           when 'es' then 'spanish'::regconfig
           when 'ru' then 'russian'::regconfig
           else 'simple'::regconfig
           end
$$;

-- unaccent() is STABLE, because it resolves its dictionary by name at call time. Pinning the dictionary makes the
-- wrapper immutable, which is what lets it be used in index expressions and folded by the planner.
create or replace function catalog.search_unaccent(p_text text) returns text
    language sql immutable parallel safe as
$$
select public.unaccent('public.unaccent'::regdictionary, coalesce(p_text, ''))
$$;

-- Fold away the differences a shopper does not type.
--
-- The snowball arabic stemmer handles suffixes but not orthography, so on its own "احذيه" never finds "أَحْذِيَة".
-- This strips the tashkeel block (U+064B..U+0652) and tatweel (U+0640), then folds the alef forms
-- (U+0623 U+0625 U+0622 U+0671 -> U+0627), teh marbuta (U+0629 -> U+0647) and alef maksura (U+0649 -> U+064A).
-- search_unaccent covers the Latin scripts in the same pass.
--
-- This function is the ONLY normalisation implementation. The query side calls it too, in SQL — Java must never
-- re-implement it.
create or replace function catalog.search_normalize(p_text text) returns text
    language sql immutable parallel safe as
$$
select catalog.search_unaccent(
               translate(
                       regexp_replace(lower(coalesce(p_text, '')), '[ً-ْـ]', '', 'g'),
                       'أإآٱىة',
                       'اااايه'))
$$;

-- The autocomplete query: the shopper is still typing, so the last word — and every word, since we do not know
-- which one they are on — matches as a prefix. Lexing the query through to_tsvector first means the prefixes are
-- attached to stemmed lexemes ("running" -> "run:*") and stopwords drop out, which a naive split on spaces would
-- get wrong in both directions.
create or replace function catalog.search_prefix_tsquery(p_text text, p_language varchar) returns tsquery
    language sql immutable parallel safe as
$$
select to_tsquery(catalog.search_config(p_language),
                  array_to_string(
                          (select array_agg(lexeme || ':*')
                           from unnest(tsvector_to_array(
                                   to_tsvector(catalog.search_config(p_language),
                                               catalog.search_normalize(p_text)))) as lexeme),
                          ' & '))
$$;

create table if not exists catalog.product_search_index
(
    product_id        bigint       not null,
    language_code     varchar(6)   not null,
    store_merchant_id varchar(50)  not null,
    name_normalized   text,
    search_document   tsvector,
    indexed_at        timestamp(6) not null default now(),
    primary key (product_id, language_code)
);

-- store_merchant_id sits in the index, not just the table: without it a search for a common word scans every
-- matching row across every tenant in the pod and only then filters, so cost would grow with the number of
-- stores rather than with the store's own catalogue. btree_gin is what lets the scalar columns share the GIN.
create index if not exists product_search_doc_idx on catalog.product_search_index
    using gin (store_merchant_id, language_code, search_document);

-- Only read on the fallback path, when the tsquery found nothing and we are looking for a near miss.
create index if not exists product_search_trgm_idx on catalog.product_search_index
    using gin (store_merchant_id, name_normalized public.gin_trgm_ops);

-- Missing until now, and both sit directly on the facet path.
create index if not exists product_store_idx on catalog.product (store_merchant_id);
create index if not exists product_category_category_idx on catalog.product_category (category_id);

-- The searchable form of every product, as a query. Kept as a view so the document is defined exactly once and
-- both the per-product refresh and the whole-store rebuild are the same expression with a different filter.
--
-- Weighted so a hit on the name outranks a hit in the body:
--   A name · B meta_title, title, keywords, sku, ref_sku, brand · C highlight · D description
-- Category names are deliberately absent: a category match reaches the shopper as its own suggestion, and
-- including them here would mean every category rename invalidated the products underneath it.
create or replace view catalog.product_search_source as
select pd.product_id,
       pd.language_code,
       p.store_merchant_id,
       catalog.search_normalize(pd.name) as name_normalized,
       setweight(to_tsvector(catalog.search_config(pd.language_code),
                             catalog.search_normalize(pd.name)), 'A') ||
       setweight(to_tsvector(catalog.search_config(pd.language_code),
                             catalog.search_normalize(
                                     concat_ws(' ', pd.meta_title, pd.title, pd.meta_keywords,
                                               p.sku, p.ref_sku, md.name))), 'B') ||
       setweight(to_tsvector(catalog.search_config(pd.language_code),
                             catalog.search_normalize(pd.product_highlight)), 'C') ||
       setweight(to_tsvector(catalog.search_config(pd.language_code),
           -- the description is merchant-authored HTML; tags are not words
                             catalog.search_normalize(
                                     regexp_replace(coalesce(pd.description, ''), '<[^>]*>', ' ', 'g'))), 'D')
           as search_document
from catalog.product_description pd
         join catalog.product p on p.product_id = pd.product_id
         left join catalog.manufacturer_description md
                   on md.manufacturer_id = p.manufacturer_id and md.language_code = pd.language_code;

-- Rebuild every language of one product.
--
-- One statement, and plain SQL rather than plpgsql, because Spring's script runner splits init scripts on the
-- semicolon and does not understand dollar quoting — a procedural body with statements in it would be cut in
-- half at startup. It also has to be an upsert rather than a delete-then-insert: a data-modifying CTE that
-- deleted and reinserted the same key would race itself. So the delete only prunes languages the product no
-- longer has, and the insert takes care of the rest.
create or replace function catalog.refresh_product_search_index(p_product_id bigint) returns integer
    language sql as
$$
with wanted as (select * from catalog.product_search_source where product_id = p_product_id),
     pruned as (
         delete from catalog.product_search_index i
             where i.product_id = p_product_id
                 and not exists (select 1 from wanted w where w.language_code = i.language_code)
             returning 1),
     upserted as (
         insert into catalog.product_search_index
             (product_id, language_code, store_merchant_id, name_normalized, search_document, indexed_at)
             select product_id, language_code, store_merchant_id, name_normalized, search_document, now()
             from wanted
             on conflict (product_id, language_code)
                 do update set store_merchant_id = excluded.store_merchant_id,
                               name_normalized   = excluded.name_normalized,
                               search_document   = excluded.search_document,
                               indexed_at        = excluded.indexed_at
             returning 1)
select count(*)::int from upserted
$$;

create or replace function catalog.purge_product_search_index(p_product_id bigint) returns integer
    language sql as
$$
with removed as (delete from catalog.product_search_index where product_id = p_product_id returning 1)
select count(*)::int from removed
$$;

-- Whole-store rebuild: behind the private rebuild endpoint, and what to run after the document's shape changes
-- above. Set-based rather than a loop over products, so a large catalogue is one pass.
create or replace function catalog.rebuild_product_search_index(p_store varchar) returns integer
    language sql as
$$
with wanted as (select * from catalog.product_search_source where store_merchant_id = p_store),
     pruned as (
         delete from catalog.product_search_index i
             where i.store_merchant_id = p_store
                 and not exists (select 1
                                 from wanted w
                                 where w.product_id = i.product_id and w.language_code = i.language_code)
             returning 1),
     upserted as (
         insert into catalog.product_search_index
             (product_id, language_code, store_merchant_id, name_normalized, search_document, indexed_at)
             select product_id, language_code, store_merchant_id, name_normalized, search_document, now()
             from wanted
             on conflict (product_id, language_code)
                 do update set store_merchant_id = excluded.store_merchant_id,
                               name_normalized   = excluded.name_normalized,
                               search_document   = excluded.search_document,
                               indexed_at        = excluded.indexed_at
             returning 1)
select count(*)::int from upserted
$$;

-- ---------------------------------------------------------------------------------------------------------------
-- Outbox
--
-- The search index is refreshed from domain events, not from a database trigger: the event row commits in the same
-- transaction as the product change, and the poller picks it up a moment later. That buys retries, ordered
-- per-product processing and a batched brand rename, at the cost of the index being eventually consistent — a
-- merchant who saves a product does not find it by search for a second or two.
--
-- The library's own schema initialisation is off (namastack.outbox.jpa.schema-initialization.enabled: false), the
-- same as payment-service, so the tables are declared here. Index names are schema-scoped, so these do not collide
-- with payment's.
-- ---------------------------------------------------------------------------------------------------------------

create table if not exists catalog.outbox_record
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

create table if not exists catalog.outbox_instance
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

create table if not exists catalog.outbox_partition
(
    partition_number integer primary key,
    instance_id      varchar(255),
    version          bigint                   not null default 0,
    updated_at       timestamp with time zone not null
);

create index if not exists idx_outbox_record_record_key_created
    on catalog.outbox_record (record_key, created_at);
create index if not exists idx_outbox_record_partition_status_retry
    on catalog.outbox_record (partition_no, status, next_retry_at);
create index if not exists idx_outbox_record_status_retry
    on catalog.outbox_record (status, next_retry_at);
create index if not exists idx_outbox_record_status
    on catalog.outbox_record (status);
create index if not exists idx_outbox_record_record_key_completed_created
    on catalog.outbox_record (record_key, completed_at, created_at);
create index if not exists idx_outbox_instance_status_heartbeat
    on catalog.outbox_instance (status, last_heartbeat);
create index if not exists idx_outbox_instance_last_heartbeat
    on catalog.outbox_instance (last_heartbeat);
create index if not exists idx_outbox_instance_status
    on catalog.outbox_instance (status);
create index if not exists idx_outbox_partition_instance_id
    on catalog.outbox_partition (instance_id);
