CREATE TABLE IF NOT EXISTS product
(
    id          varchar(50) not null,
    store_id    varchar(50),
    name        varchar(50),
    description varchar(50),
    price       int,
    currency    varchar(4),
    published   boolean,
    deleted     boolean DEFAULT FALSE,
    constraint product_pk primary key (id)
);

CREATE TABLE IF NOT EXISTS product_image
(
    id         varchar(50)  not null,
    link       varchar(200) not null,
    product_id varchar(50),
    sequence   varchar(5),
    constraint product_image_pk primary key (id)
);
CREATE TABLE IF NOT EXISTS product_variant
(
    id       varchar(50) not null,
    price    int,
    currency varchar(4),
    amount   int,
    constraint product_variant_pk primary key (id)
);

CREATE TABLE IF NOT EXISTS product_variant_feature
(
    id                 varchar(50) not null,
    key                varchar(50),
    value              varchar(50),
    product_variant_id varchar(50),
    constraint product_variant_feature_pk primary key (id)
);

CREATE TABLE IF NOT EXISTS product_variant_ref
(
    product_id         varchar(50) not null,
    product_variant_id varchar(50) not null,
    constraint product_variant_ref_product_id_fk foreign key (product_id) references product,
    constraint product_variant_ref_product_variant_id_fk foreign key (product_variant_id) references product_variant
);
