create schema if not exists orders;
create table if not exists orders.sm_sequencer
(
    seq_name  varchar(255) not null primary key,
    seq_count bigint
);
create table if not exists orders.currency
(
    currency_code      varchar(6) not null primary key,
    currency_name      varchar(255)
        constraint uk_ntgaxtcgi6crpijka4yu7927o unique,
    currency_supported boolean
);
create table if not exists orders.geozone
(
    geozone_id   bigint not null primary key,
    geozone_code varchar(255),
    geozone_name varchar(255)
);
create table if not exists orders.country
(
    country_isocode   varchar(6) not null primary key,
    country_supported boolean,
    geozone_id        bigint
);
create table if not exists orders.language
(
    date_created  timestamp(6),
    date_modified timestamp(6),
    updt_id       varchar(60),
    code          varchar(6) not null primary key,
    sort_order    integer
);
create table if not exists orders.country_description
(
    description_id bigint       not null primary key,
    date_created   timestamp(6),
    date_modified  timestamp(6),
    updt_id        varchar(60),
    description    text,
    name           varchar(120) not null,
    title          varchar(100),
    language_code  varchar(6)   not null,
    country_id     varchar(6)   not null,
    constraint UKdf8ewjt49cy3enpcwoe9ganps unique (country_id, language_code)
);
create table if not exists orders.geozone_description
(
    description_id bigint       not null primary key,
    date_created   timestamp(6),
    date_modified  timestamp(6),
    updt_id        varchar(60),
    description    text,
    name           varchar(120) not null,
    title          varchar(100),
    language_code  varchar(6)   not null,
    geozone_id     bigint,
    constraint UKl86ppufwjqn6fy1sd7vfc7any unique (geozone_id, language_code)
);
create index if not exists code_idx2 on orders.language (code);
create table if not exists orders.zone
(
    zone_code  varchar(100) not null primary key,
    country_id varchar(6)   not null
);
create table if not exists orders.zone_description
(
    description_id bigint       not null primary key,
    date_created   timestamp(6),
    date_modified  timestamp(6),
    updt_id        varchar(60),
    description    text,
    name           varchar(120) not null,
    title          varchar(100),
    language_code  varchar(6)   not null,
    zone_id        varchar(100) not null,
    constraint UKbb9ur48rkg1nngdmka6tjlmum unique (zone_id, language_code)
);
create table if not exists orders.customer
(
    customer_id             bigint      not null primary key,
    customer_anonymous      boolean,
    date_created            timestamp(6),
    date_modified           timestamp(6),
    updt_id                 varchar(60),
    billing_street_address  varchar(256),
    billing_city            varchar(100),
    billing_company         varchar(100),
    billing_first_name      varchar(64) not null,
    billing_last_name       varchar(64) not null,
    latitude                varchar(100),
    longitude               varchar(100),
    billing_postcode        varchar(20),
    billing_state           varchar(100),
    billing_telephone       varchar(32),
    customer_company        varchar(100),
    reset_credentials_req   varchar(256),
    reset_credentials_exp   date,
    review_avg              numeric(38, 2),
    review_count            integer,
    customer_dob            timestamp(6),
    delivery_street_address varchar(256),
    delivery_city           varchar(100),
    delivery_company        varchar(100),
    delivery_first_name     varchar(64),
    delivery_last_name      varchar(64),
    delivery_postcode       varchar(20),
    delivery_state          varchar(100),
    delivery_telephone      varchar(32),
    customer_email_address  varchar(96) not null,
    customer_gender         char
        constraint customer_customer_gender_check check (
            customer_gender = ANY (ARRAY [ 'M' :: bpchar, 'F' :: bpchar])
            ),
    customer_nick           varchar(96),
    customer_password       varchar(60),
    provider                varchar(255),
    billing_country_id      varchar(6)  not null,
    billing_zone_id         varchar(100),
    language_code           varchar(6)  not null,
    DELIVERY_COUNTRY_CODE   varchar(6),
    delivery_zone_id        varchar(100),
    store_merchant_id       varchar(50) not null,
    constraint UKsniymsufa1eqq35pc8kfgyo7p unique (store_merchant_id, customer_nick)
);
create table if not exists orders.customer_group
(
    customer_id bigint  not null
        constraint fkbopjkmu9mriivehbk9yd6rbvw references orders.customer,
    group_id    integer not null
);
create table if not exists orders.customer_option
(
    customer_option_id   bigint      not null primary key,
    customer_opt_active  boolean,
    customer_opt_code    varchar(255),
    customer_option_type varchar(10),
    customer_opt_public  boolean,
    sort_order           integer,
    store_merchant_id    varchar(50) not null,
    constraint UKe0v0628sjv6kcme4jqtkhk1lt unique (store_merchant_id, customer_opt_code)
);
create index if not exists cust_opt_code_idx on orders.customer_option (customer_opt_code);
create table if not exists orders.customer_option_desc
(
    description_id          bigint       not null primary key,
    date_created            timestamp(6),
    date_modified           timestamp(6),
    updt_id                 varchar(60),
    description             text,
    name                    varchar(120) not null,
    title                   varchar(100),
    customer_option_comment varchar(4000),
    language_code           varchar(6)   not null,
    customer_option_id      bigint       not null
        constraint fk201v4egnnqx20q6qky676mks2 references orders.customer_option,
    constraint UKq3l77dsrwyrmm10bct1tbl4dw unique (customer_option_id, language_code)
);
create table if not exists orders.customer_option_value
(
    customer_option_value_id bigint      not null primary key,
    customer_opt_val_code    varchar(255),
    customer_opt_val_image   varchar(255),
    sort_order               integer,
    store_merchant_id        varchar(50) not null,
    constraint UKi3whyloen8xy3nimesr70jah unique (
                                                  store_merchant_id, customer_opt_val_code
        )
);
create table if not exists orders.customer_attribute
(
    customer_attribute_id bigint not null primary key,
    customer_attr_txt_val varchar(255),
    customer_id           bigint not null
        constraint fkkt9jyeddekdvrhcx806k7os0g references orders.customer,
    option_id             bigint not null
        constraint fkm7j9jcewyjmeh5ai9nsuuvi8i references orders.customer_option,
    option_value_id       bigint not null
        constraint fksipgdm09ffity5b9g5c50jx3w references orders.customer_option_value,
    constraint uksoy0ia28b7ehe2rru8c5q5v87 unique (option_id, customer_id)
);
create table if not exists orders.customer_opt_val_description
(
    description_id      bigint       not null primary key,
    date_created        timestamp(6),
    date_modified       timestamp(6),
    updt_id             varchar(60),
    description         text,
    name                varchar(120) not null,
    title               varchar(100),
    language_code       varchar(6)   not null,
    customer_opt_val_id bigint
        constraint fkmej30yqti7y4t4iqsq15t9yc3 references orders.customer_option_value,
    constraint UKf0c1ffdlvc2a6k0dqqvmucjjy unique (customer_opt_val_id, language_code)
);
create table if not exists orders.customer_option_set
(
    customer_optionset_id    bigint not null primary key,
    sort_order               integer,
    customer_option_id       bigint not null
        constraint fkiuve99ti5qr9j3qvth1yujjmh references orders.customer_option,
    customer_option_value_id bigint not null
        constraint fko2ohdbcnl065c2u3icfya9688 references orders.customer_option_value,
    constraint uk1lqrcukylq7x2te82cai3t7y9 unique (
                                                   customer_option_id, customer_option_value_id
        )
);
create index if not exists cust_opt_val_code_idx on orders.customer_option_value (customer_opt_val_code);
create table if not exists orders.customer_review
(
    customer_review_id   bigint not null primary key,
    date_created         timestamp(6),
    date_modified        timestamp(6),
    updt_id              varchar(60),
    review_date          timestamp(6),
    reviews_rating       double precision,
    reviews_read         bigint,
    status               integer,
    customers_id         bigint
        constraint fktnsb170ewuhjtok3p50nuaby2 references orders.customer,
    reviewed_customer_id bigint
        constraint uk_p329f4tc2gt8e9iicefcf1dwu unique
        constraint fkrt9to366jismmfdap0onydy9c references orders.customer,
    constraint ukpe13frashysxlaqa4rcms49j6 unique (
                                                   customers_id, reviewed_customer_id
        )
);
create table if not exists orders.customer_review_description
(
    description_id     bigint       not null primary key,
    date_created       timestamp(6),
    date_modified      timestamp(6),
    updt_id            varchar(60),
    description        text,
    name               varchar(120) not null,
    title              varchar(100),
    language_code      varchar(6)   not null,
    customer_review_id bigint
        constraint fk3nu9inejlfrkcig7ppv3glhrh references orders.customer_review,
    constraint UKmxdv3d04v2swtcv7ss7cx7qc9 unique (customer_review_id, language_code)
);
create table if not exists orders.file_history
(
    file_history_id   bigint       not null primary key,
    accounted_date    timestamp(6),
    date_added        timestamp(6) not null,
    date_deleted      timestamp(6),
    download_count    integer      not null,
    file_id           bigint,
    filesize          integer      not null,
    store_merchant_id varchar(50)  not null,
    constraint UKippv13bhwgk2igdoub27tps73 unique (store_merchant_id, file_id)
);
create table if not exists orders.optin
(
    optin_id          bigint       not null primary key,
    code              varchar(255) not null,
    description       varchar(255),
    end_date          timestamp(6),
    type              varchar(255) not null
        constraint optin_type_check check (
            (type):: text = ANY (
                (
                    ARRAY [ 'NEWSLETTER' :: character varying,
                        'PROMOTIONS' :: character varying]
                    ):: text[]
                )
            ),
    start_date        timestamp(6),
    store_merchant_id varchar(50),
    constraint UKre6g495jxc6apfyo4dyvw7yuy unique (store_merchant_id, code)
);
create table if not exists orders.customer_optin
(
    customer_optin_id bigint       not null primary key,
    email             varchar(255) not null,
    first             varchar(255),
    last              varchar(255),
    optin_date        timestamp(6),
    value             text,
    store_merchant_id varchar(50)  not null,
    optin_id          bigint
        constraint fkr1uwied13q1bdptgap3vdu8xb references orders.optin,
    constraint ukmaibabrcxys6ij7ifc1iqggb unique (email, optin_id)
);
create table if not exists orders.orders
(
    order_id                bigint      not null primary key,
    billing_street_address  varchar(256),
    billing_city            varchar(100),
    billing_company         varchar(100),
    billing_first_name      varchar(64) not null,
    billing_last_name       varchar(64) not null,
    latitude                varchar(100),
    longitude               varchar(100),
    billing_postcode        varchar(20),
    billing_state           varchar(100),
    billing_telephone       varchar(32),
    channel                 varchar(255)
        constraint orders_channel_check check (
            (channel):: text = ANY (
                (
                    ARRAY [ 'ONLINE' :: character varying,
                        'API' :: character varying]
                    ):: text[]
                )
            ),
    confirmed_address       boolean,
    card_type               varchar(255)
        constraint orders_card_type_check check (
            (card_type):: text = ANY (
                (
                    ARRAY [ 'AMEX' :: character varying,
                        'VISA' :: character varying, 'MASTERCARD' :: character varying,
                        'DINERS' :: character varying, 'DISCOVERY' :: character varying]
                    ):: text[]
                )
            ),
    cc_cvv                  varchar(255),
    cc_expires              varchar(255),
    cc_number               varchar(255),
    cc_owner                varchar(255),
    currency_value          numeric(38, 2),
    customer_agreed         boolean,
    customer_email_address  varchar(50) not null,
    customer_id             bigint,
    date_purchased          date,
    delivery_street_address varchar(256),
    delivery_city           varchar(100),
    delivery_company        varchar(100),
    delivery_first_name     varchar(64),
    delivery_last_name      varchar(64),
    delivery_postcode       varchar(20),
    delivery_state          varchar(100),
    delivery_telephone      varchar(32),
    ip_address              varchar(255),
    last_modified           timestamp(6),
    locale                  varchar(255),
    order_date_finished     timestamp(6),
    order_type              varchar(255)
        constraint orders_order_type_check check (
            (order_type):: text = ANY (
                (
                    ARRAY [ 'ORDER' :: character varying,
                        'BOOKING' :: character varying]
                    ):: text[]
                )
            ),
    payment_module_code     varchar(255),
    payment_type            varchar(255)
        constraint orders_payment_type_check check (
            (payment_type):: text = ANY (
                (
                    ARRAY [ 'CREDITCARD' :: character varying,
                        'FREE' :: character varying, 'COD' :: character varying,
                        'MONEYORDER' :: character varying,
                        'PAYPAL' :: character varying, 'INVOICE' :: character varying,
                        'DIRECTBANK' :: character varying,
                        'PAYMENTPLAN' :: character varying,
                        'ACCOUNTCREDIT' :: character varying]
                    ):: text[]
                )
            ),
    shipping_module_code    varchar(255),
    cart_code               varchar(255),
    order_status            varchar(255)
        constraint orders_order_status_check check (
            (order_status):: text = ANY (
                (
                    ARRAY [ 'ORDERED' :: character varying,
                        'PROCESSED' :: character varying,
                        'DELIVERED' :: character varying,
                        'REFUNDED' :: character varying, 'CANCELED' :: character varying]
                    ):: text[]
                )
            ),
    order_total             numeric(38, 2),
    billing_country_id      varchar(6)  not null,
    billing_zone_id         varchar(100),
    currency_id             varchar(6),
    DELIVERY_COUNTRY_CODE   varchar(6),
    delivery_zone_id        varchar(100),
    store_merchant_id       varchar(50)
);
create table if not exists orders.order_account
(
    order_account_id         bigint  not null primary key,
    order_account_bill_day   integer not null,
    order_account_end_date   date,
    order_account_start_date date    not null,
    order_id                 bigint  not null
        constraint fktdb599f1si18ktq25o4w5tsau references orders.orders
);
create table if not exists orders.order_attribute
(
    order_attribute_id bigint       not null primary key,
    identifier         varchar(255) not null,
    value              varchar(255) not null,
    order_id           bigint       not null
        constraint fkpfbrs3waqlbp0yeohck8sx91c references orders.orders
);
create table if not exists orders.order_product
(
    order_product_id bigint         not null primary key,
    onetime_charge   numeric(38, 2) not null,
    product_name     varchar(64)    not null,
    product_quantity integer,
    product_sku      varchar(255),
    order_id         bigint         not null
        constraint fkl5mnj9n0di7k1v90yxnthkc73 references orders.orders
);
create table if not exists orders.order_account_product
(
    order_account_product_id       bigint  not null primary key,
    order_account_product_accnt_dt date,
    order_account_product_end_dt   date,
    order_account_product_eot      timestamp(6),
    order_account_product_l_st_dt  timestamp(6),
    order_account_product_l_trx_st integer not null,
    order_account_product_pm_fr_ty integer not null,
    order_account_product_st_dt    date    not null,
    order_account_product_status   integer not null,
    order_account_id               bigint  not null
        constraint fk238g8uilgxlh5fa6ieub3ods references orders.order_account,
    order_product_id               bigint  not null
        constraint fkl3rvhrb6nq9sdb1nai9cr9klm references orders.order_product
);
create table if not exists orders.order_product_attribute
(
    order_product_attribute_id bigint         not null primary key,
    product_attribute_is_free  boolean        not null,
    product_attribute_name     varchar(255),
    product_attribute_price    numeric(15, 4) not null,
    product_attribute_val_name varchar(255),
    product_attribute_weight   numeric(15, 4),
    product_option_id          bigint         not null,
    product_option_value_id    bigint         not null,
    order_product_id           bigint         not null
        constraint fk9w04pur2suf544spmowxfr3xg references orders.order_product
);
create table if not exists orders.order_product_download
(
    order_product_download_id bigint       not null primary key,
    download_count            integer      not null,
    download_maxdays          integer      not null,
    order_product_filename    varchar(255) not null,
    order_product_id          bigint       not null
        constraint fkmy1bxlfoja5v2pmo9vq76l7ry references orders.order_product
);
create table if not exists orders.order_product_price
(
    order_product_price_id   bigint         not null primary key,
    default_price            boolean        not null,
    product_price            numeric(38, 2) not null,
    product_price_code       varchar(64)    not null,
    product_price_name       varchar(255),
    product_price_special    numeric(38, 2),
    prd_price_special_end_dt timestamp(6),
    prd_price_special_st_dt  timestamp(6),
    order_product_id         bigint         not null
        constraint fkoh8f95nugkcqxflqo1rist0g1 references orders.order_product
);
create table if not exists orders.order_status_history
(
    order_status_history_id bigint       not null primary key,
    comments                text,
    customer_notified       integer,
    date_added              timestamp(6) not null,
    status                  varchar(255)
        constraint order_status_history_status_check check (
            (status):: text = ANY (
                (
                    ARRAY [ 'ORDERED' :: character varying,
                        'PROCESSED' :: character varying,
                        'DELIVERED' :: character varying,
                        'REFUNDED' :: character varying, 'CANCELED' :: character varying]
                    ):: text[]
                )
            ),
    order_id                bigint       not null
        constraint fknmcbg3mmbt8wfva97ra40nmp3 references orders.orders
);
create table if not exists orders.order_total
(
    order_account_id bigint         not null primary key,
    module           varchar(60),
    code             varchar(255)   not null,
    order_total_type varchar(255)
        constraint order_total_order_total_type_check check (
            (order_total_type):: text = ANY (
                (
                    ARRAY [ 'SHIPPING' :: character varying,
                        'HANDLING' :: character varying, 'TAX' :: character varying,
                        'PRODUCT' :: character varying, 'SUBTOTAL' :: character varying,
                        'TOTAL' :: character varying, 'CREDIT' :: character varying,
                        'REFUND' :: character varying]
                    ):: text[]
                )
            ),
    order_value_type varchar(255)
        constraint order_total_order_value_type_check check (
            (order_value_type):: text = ANY (
                (
                    ARRAY [ 'ONE_TIME' :: character varying,
                        'MONTHLY' :: character varying]
                    ):: text[]
                )
            ),
    sort_order       integer        not null,
    text             text,
    title            varchar(255),
    value            numeric(15, 4) not null,
    order_id         bigint         not null
        constraint fksyu55314fmsbvx76nxyvo2ejj references orders.orders
);
create table if not exists orders.shopping_cart
(
    shp_cart_id       bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    customer_id       bigint,
    ip_address        varchar(255),
    order_id          bigint,
    promo_added       timestamp(6),
    promo_code        varchar(255),
    shp_cart_code     varchar(255) not null
        constraint uk_g6b5qebd5yvy3msjrus23vw51 unique,
    store_merchant_id varchar(50)  not null
);
create index if not exists shp_cart_code_idx on orders.shopping_cart (shp_cart_code);
create index if not exists shp_cart_customer_idx on orders.shopping_cart (customer_id);
create table if not exists orders.shopping_cart_item
(
    shp_cart_item_id bigint       not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    quantity         integer,
    sku              varchar(255) not null,
    product_variant  bigint,
    shp_cart_id      bigint       not null
        constraint fk10kmhpldycqc7cvn24tesj8yx references orders.shopping_cart
);
create table if not exists orders.shopping_cart_attr_item
(
    shp_cart_attr_item_id bigint not null primary key,
    date_created          timestamp(6),
    date_modified         timestamp(6),
    updt_id               varchar(60),
    product_attr_id       bigint not null,
    shp_cart_item_id      bigint not null
        constraint fkt3iw5nxx7h55j5vta1tyrvgv3 references orders.shopping_cart_item
);
create table if not exists orders.sm_transaction
(
    transaction_id   bigint not null primary key,
    amount           numeric(38, 2),
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    details          text,
    payment_type     varchar(255)
        constraint sm_transaction_payment_type_check check (
            (payment_type):: text = ANY (
                (
                    ARRAY [ 'CREDITCARD' :: character varying,
                        'FREE' :: character varying, 'COD' :: character varying,
                        'MONEYORDER' :: character varying,
                        'PAYPAL' :: character varying, 'INVOICE' :: character varying,
                        'DIRECTBANK' :: character varying,
                        'PAYMENTPLAN' :: character varying,
                        'ACCOUNTCREDIT' :: character varying]
                    ):: text[]
                )
            ),
    transaction_date timestamp(6),
    transaction_type varchar(255)
        constraint sm_transaction_transaction_type_check check (
            (transaction_type):: text = ANY (
                (
                    ARRAY [ 'INIT' :: character varying,
                        'AUTHORIZE' :: character varying,
                        'CAPTURE' :: character varying, 'AUTHORIZECAPTURE' :: character varying,
                        'REFUND' :: character varying, 'OK' :: character varying]
                    ):: text[]
                )
            ),
    order_id         bigint
        constraint fkdgyct8065xy9kp7entj7lcgsj references orders.orders
);
