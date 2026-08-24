create table if not exists catalog.product_variant_group
(
    product_variant_group_id bigint      not null primary key,
    store_merchant_id        varchar(50) not null
);
create table if not exists catalog.product_var_image
(
    product_var_image_id     bigint not null primary key,
    default_image            boolean,
    product_image            varchar(255),
    product_variant_group_id bigint not null
        constraint fkdw60r3ujcjs86ljprqecoueu references catalog.product_variant_group
);
create table if not exists catalog.product_variation
(
    product_variation_id bigint       not null primary key,
    date_created         timestamp(6),
    date_modified        timestamp(6),
    updt_id              varchar(60),
    code                 varchar(100) not null,
    sort_order           integer,
    variant_default      boolean,
    store_merchant_id    varchar(50)  not null,
    product_option_id    bigint       not null
        constraint fkqsuew0y7i60q5x056w7qymhtq references catalog.product_option,
    option_value_id      bigint       not null
        constraint fkmdirohj7ym0mj2l1dl21yr8ur references catalog.product_option_value,
    constraint UK7q6j9dlg19t0ea6gfasnskeo3 unique (
                                                   store_merchant_id, product_option_id, option_value_id
        )
);
create table if not exists catalog.product_var_image_description
(
    description_id       bigint       not null primary key,
    date_created         timestamp(6),
    date_modified        timestamp(6),
    updt_id              varchar(60),
    description          text,
    name                 varchar(120) not null,
    title                varchar(100),
    alt_tag              varchar(100),
    language_code        varchar(6)   not null,
    product_id           bigint       not null
        constraint fkhn2mq2b7cqn7rg7h4k9py71k6 references catalog.product,
    product_var_image_id bigint       not null
        constraint fkdafb5lwa8xuw5knggb5opunqw references catalog.product_var_image,
    constraint UKnqb3wtfkfird2cjp6eyfhgbmu unique (
                                                   product_var_image_id, language_code
        )
);
create table if not exists catalog.product_variant
(
    product_variant_id         bigint not null primary key,
    date_created               timestamp(6),
    date_modified              timestamp(6),
    updt_id                    varchar(60),
    available                  boolean,
    code                       varchar(255),
    date_available             timestamp(6),
    default_selection          boolean,
    sku                        varchar(255),
    sort_order                 integer,
    product_id                 bigint not null
        constraint fkgrbbs9t374m9gg43l6tq1xwdj references catalog.product,
    product_variant_group_id   bigint
        constraint fk8xtj3bi1s75coa9f84tgof611 references catalog.product_variant_group,
    product_variation_id       bigint
        constraint fkrsayulwa4xtlt8mc3sddl1spt references catalog.product_variation,
    product_variation_value_id bigint
        constraint fkrt8wxodoxdqx5t4a1ey66xnyd references catalog.product_variation,
    constraint ukg69sutihl26shxl3w47s0vch unique (product_id, sku)
);
create table if not exists catalog.product_availability
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
    product_id        bigint  not null
        constraint fk3sgpu0mqt3cncaw0k1x3okkq8 references catalog.product,
    product_variant   bigint
        constraint fkg460g5177h3t14atnsynm8ikc references catalog.product_variant,
    constraint UK3cq0pcvlrorbgahh1r1o6fao5 unique (
                                                   store_merchant_id, product_id, product_variant,
                                                   region_variant
        )
);
create table if not exists catalog.product_price
(
    product_price_id               bigint       not null primary key,
    product_price_code             varchar(255) not null,
    default_price                  boolean,
    product_identifier_id          bigint,
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
        constraint fkth2oyk1000w7vut9aro9a8mhi references catalog.product_availability
);
create table if not exists catalog.product_price_description
(
    description_id   bigint       not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    description      text,
    name             varchar(120) not null,
    title            varchar(100),
    price_appender   varchar(255),
    language_code    varchar(6)   not null,
    product_price_id bigint       not null
        constraint fkghwl7kccj71r4u0qdwegbles6 references catalog.product_price,
    constraint UKc84xdnuwluljfaeor1cax423p unique (product_price_id, language_code)
);
create table if not exists catalog.product_reservation
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
create table if not exists catalog.product_reservation_line
(
    id                     bigint       not null primary key,
    date_created           timestamp(6),
    date_modified          timestamp(6),
    updt_id                varchar(60),
    product_reservation_id bigint       not null,
    sku                    varchar(255) not null,
    quantity               integer      not null,
    product_avail_id       bigint,
    constraint fk_prd_res_line_res foreign key (product_reservation_id) references catalog.product_reservation (id),
    constraint fk_prd_res_line_avail foreign key (product_avail_id) references catalog.product_availability (product_avail_id)
);
