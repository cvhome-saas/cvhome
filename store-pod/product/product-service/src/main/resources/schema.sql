CREATE TABLE IF NOT EXISTS category
(
    id         varchar(24)  not null,
    store_id   varchar(24),
    name       varchar(50),
    image_link varchar(200) not null,
    parent_id  varchar(24),
    sequence   int,
    level      int,
    constraint category_pk primary key (id),
    constraint category_parent_id_fk foreign key (parent_id) references category

);
CREATE TABLE IF NOT EXISTS product
(
    id          varchar(24)  not null,
    store_id    varchar(24),
    category_id varchar(24),
    name        varchar(50),
    description varchar(50),
    price       int,
    currency    varchar(4),
    published   boolean DEFAULT FALSE,
    deleted     boolean DEFAULT FALSE,
    image_link  varchar(200) not null,
    constraint product_pk primary key (id),
    constraint product_category_id_fk foreign key (category_id) references category
);

CREATE TABLE IF NOT EXISTS product_image
(
    id         varchar(24)  not null,
    image_link varchar(200) not null,
    product_id varchar(24),
    store_id   varchar(24),
    sequence   varchar(5),
    constraint product_image_pk primary key (id)
);
CREATE TABLE IF NOT EXISTS product_variant
(
    id         varchar(24) not null,
    product_id varchar(24) not null,
    store_id   varchar(24),
    price      int,
    currency   varchar(4),
    amount     int,
    features   varchar(1500),
    constraint product_variant_pk primary key (id),
    constraint product_variant_product_id_fk foreign key (product_id) references product
);