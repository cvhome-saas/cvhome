CREATE TABLE IF NOT EXISTS product
(
  id          varchar(50) not null,
  store_id    varchar(50),
  name        varchar(50),
  description varchar(50),
  price       int,
  currency    varchar(4),
  published   boolean,
  constraint product_pk primary key (id)
);
CREATE TABLE IF NOT EXISTS product_variant
(
  id         varchar(50) not null,
  price      int,
  currency   varchar(4),
  amount     int,
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
