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
create table if not exists catalog.product_image
(
    product_image_id  bigint not null primary key,
    default_image     boolean,
    image_crop        boolean,
    image_type        integer,
    product_image     varchar(255),
    product_image_url varchar(255),
    sort_order        integer,
    product_id        bigint not null
        constraint fk6oo0cvcdtb6qmwsga468uuukk references catalog.product
);
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
