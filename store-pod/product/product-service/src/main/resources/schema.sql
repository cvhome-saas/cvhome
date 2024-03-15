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
    id           varchar(24)  not null,
    store_id     varchar(24),
    category_id  varchar(24),
    name         varchar(50),
    description  varchar(50),
    price        int,
    currency     varchar(4),
    published    boolean DEFAULT FALSE,
    deleted      boolean DEFAULT FALSE,
    image_link   varchar(200) not null,
    amount       int,
    product_type varchar(10),
    sub_products varchar(1000),
    image_links varchar(1024),
--     features   varchar(1500),
    constraint product_pk primary key (id),
    constraint product_category_id_fk foreign key (category_id) references category
);

CREATE TABLE IF NOT EXISTS image
(
    id         varchar(24)  not null,
    image_link varchar(200) not null,
    name       varchar(50),
    store_id   varchar(24),
    constraint image_pk primary key (id)
);

CREATE TABLE IF NOT EXISTS product_details
(
    id              varchar(24) not null,
    product_id      varchar(24),
    store_id        varchar(24),
    product_details varchar(2048),
    constraint product_details_pk primary key (id),
    constraint product_details_product_id_fk foreign key (product_id) references product
);