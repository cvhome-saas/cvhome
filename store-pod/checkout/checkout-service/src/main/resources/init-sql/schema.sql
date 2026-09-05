create schema if not exists checkout;
create table if not exists checkout.sm_sequencer
(
    seq_name  varchar(255) not null primary key,
    seq_count bigint
);

-- A shopper as this store knows them. Unique per (store, cua account): the same cua account in two stores is two rows.
create table if not exists checkout.customer_account
(
    customer_id             bigint       not null primary key,
    date_created            timestamp(6),
    date_modified           timestamp(6),
    updt_id                 varchar(60),
    store_merchant_id       varchar(50)  not null,
    cua_external_id         varchar(96)  not null,
    email                   varchar(96)  not null,
    first_name              varchar(64),
    last_name               varchar(64),
    billing_first_name      varchar(64),
    billing_last_name       varchar(64),
    billing_company         varchar(100),
    billing_street_address  varchar(256),
    billing_city            varchar(100),
    billing_state           varchar(100),
    billing_postcode        varchar(20),
    billing_telephone       varchar(32),
    billing_country_code    varchar(6),
    billing_zone_code       varchar(100),
    delivery_first_name     varchar(64),
    delivery_last_name      varchar(64),
    delivery_company        varchar(100),
    delivery_street_address varchar(256),
    delivery_city           varchar(100),
    delivery_state          varchar(100),
    delivery_postcode       varchar(20),
    delivery_telephone      varchar(32),
    delivery_country_code   varchar(6),
    delivery_zone_code      varchar(100),
    constraint uk_customer_store_sub unique (store_merchant_id, cua_external_id)
);
create index if not exists customer_store_email_idx on checkout.customer_account (store_merchant_id, email);

-- The cart: sku and quantity per line. Prices are never stored; they are read live from inventory.
create table if not exists checkout.cart
(
    cart_id           bigint      not null primary key,
    version           bigint      not null default 0,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    store_merchant_id varchar(50) not null,
    cart_code         varchar(36) not null constraint uk_cart_code unique,
    status            varchar(10) not null constraint cart_status_check check (status in ('ACTIVE', 'CONVERTED')),
    order_id          bigint,
    cua_external_id   varchar(96),
    language_code     varchar(6)
);
create table if not exists checkout.cart_line
(
    line_id       bigint       not null primary key,
    date_created  timestamp(6),
    date_modified timestamp(6),
    updt_id       varchar(60),
    cart_id       bigint       not null constraint fk_cart_line_cart references checkout.cart,
    sku           varchar(255) not null,
    quantity      integer      not null constraint cart_line_quantity_check check (quantity > 0),
    constraint uk_cart_line_sku unique (cart_id, sku)
);

-- The order aggregate. version is the optimistic lock every transition is applied under; pending_action is the remote
-- step still owed, which the recovery job re-drives; the three status CHECKs list every value of their Java enum.
create table if not exists checkout.sales_order
(
    order_id                  bigint         not null primary key,
    version                   bigint         not null default 0,
    date_created              timestamp(6),
    date_modified             timestamp(6),
    updt_id                   varchar(60),
    store_merchant_id         varchar(50)    not null,
    order_ref                 varchar(36)    not null constraint uk_sales_order_ref unique,
    cart_code                 varchar(36)    not null,
    customer_id               bigint         not null constraint fk_sales_order_customer references checkout.customer_account,
    cua_external_id           varchar(96),
    customer_email            varchar(96)    not null,
    language_code             varchar(6)     not null,
    currency_code             varchar(6)     not null,
    payment_type              varchar(20)    not null constraint sales_order_payment_type_check
        check (payment_type in ('COD', 'MANUAL_TRANSFER', 'PAYPAL', 'STRIPE')),
    order_status              varchar(20)    not null constraint sales_order_order_status_check
        check (order_status in ('CREATED', 'PENDING_PAYMENT', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERING',
                                'DELIVERED', 'COMPLETED', 'CANCELLED', 'RETURNED')),
    payment_status            varchar(30)    not null constraint sales_order_payment_status_check
        check (payment_status in ('PENDING', 'PROCESSING', 'PAID', 'FAILED', 'EXPIRED', 'CANCELLED',
                                  'WAITING_VERIFICATION', 'REJECTED', 'AUTHORIZED', 'REFUNDED')),
    inventory_status          varchar(30)    not null constraint sales_order_inventory_status_check
        check (inventory_status in ('AVAILABLE', 'NOT_REQUESTED', 'RESERVED', 'COMMITTED', 'RELEASED',
                                    'RESERVATION_FAILED')),
    pending_action            varchar(20)    not null constraint sales_order_pending_action_check
        check (pending_action in ('NONE', 'RESERVE', 'INITIATE_PAYMENT', 'COMMIT', 'RELEASE')),
    pending_action_attempts   integer        not null default 0,
    pending_action_updated_at timestamp(6)   not null,
    needs_attention           boolean        not null default false,
    attention_reason          varchar(255),
    reservation_expire_at     timestamp(6),
    payment_transaction_ref   varchar(70),
    redirect_url              varchar(2048),
    success_url               varchar(1024)  not null,
    cancel_url                varchar(1024)  not null,
    expires_at                timestamp(6),
    date_purchased            timestamp(6)   not null,
    subtotal                  numeric(19, 4) not null,
    total                     numeric(19, 4) not null,
    comments                  text,
    billing_first_name        varchar(64),
    billing_last_name         varchar(64),
    billing_company           varchar(100),
    billing_street_address    varchar(256),
    billing_city              varchar(100),
    billing_state             varchar(100),
    billing_postcode          varchar(20),
    billing_telephone         varchar(32),
    billing_country_code      varchar(6),
    billing_zone_code         varchar(100),
    delivery_first_name       varchar(64),
    delivery_last_name        varchar(64),
    delivery_company          varchar(100),
    delivery_street_address   varchar(256),
    delivery_city             varchar(100),
    delivery_state            varchar(100),
    delivery_postcode         varchar(20),
    delivery_telephone        varchar(32),
    delivery_country_code     varchar(6),
    delivery_zone_code        varchar(100)
);
create index if not exists sales_order_store_cart_idx on checkout.sales_order (store_merchant_id, cart_code);
create index if not exists sales_order_store_date_idx on checkout.sales_order (store_merchant_id, date_purchased desc);
create index if not exists sales_order_store_customer_idx on checkout.sales_order (store_merchant_id, customer_id);
create index if not exists sales_order_store_status_idx on checkout.sales_order (store_merchant_id, order_status);
create index if not exists sales_order_pending_idx on checkout.sales_order (pending_action_updated_at)
    where pending_action <> 'NONE';
create index if not exists sales_order_expiry_idx on checkout.sales_order (expires_at) where expires_at is not null;
create index if not exists sales_order_attention_idx on checkout.sales_order (store_merchant_id) where needs_attention;

create table if not exists checkout.sales_order_line
(
    line_id      bigint         not null primary key,
    order_id     bigint         not null constraint fk_sales_order_line_order references checkout.sales_order,
    sku          varchar(255)   not null,
    product_id   bigint,
    product_name varchar(255)   not null,
    unit_price   numeric(19, 4) not null,
    quantity     integer        not null constraint sales_order_line_quantity_check check (quantity > 0),
    line_total   numeric(19, 4) not null,
    image_url    varchar(1024),
    sort_order   integer        not null
);
create index if not exists sales_order_line_order_idx on checkout.sales_order_line (order_id);

create table if not exists checkout.sales_order_line_option
(
    option_id   bigint       not null primary key,
    line_id     bigint       not null constraint fk_sales_order_line_option_line references checkout.sales_order_line,
    option_name varchar(120) not null,
    value_name  varchar(120) not null,
    sort_order  integer
);
create index if not exists sales_order_line_option_line_idx on checkout.sales_order_line_option (line_id);

create table if not exists checkout.sales_order_total
(
    total_id   bigint         not null primary key,
    order_id   bigint         not null constraint fk_sales_order_total_order references checkout.sales_order,
    code       varchar(20)    not null constraint sales_order_total_code_check
        check (code in ('SUBTOTAL', 'SHIPPING', 'TAX', 'TOTAL')),
    module     varchar(60)    not null,
    title      varchar(255),
    value      numeric(19, 4) not null,
    sort_order integer        not null
);
create index if not exists sales_order_total_order_idx on checkout.sales_order_total (order_id);

-- The user-visible status trail.
create table if not exists checkout.sales_order_history
(
    history_id bigint       not null primary key,
    order_id   bigint       not null constraint fk_sales_order_history_order references checkout.sales_order,
    status     varchar(20)  not null constraint sales_order_history_status_check
        check (status in ('CREATED', 'PENDING_PAYMENT', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERING',
                          'DELIVERED', 'COMPLETED', 'CANCELLED', 'RETURNED')),
    comments   text,
    actor      varchar(100),
    date_added timestamp(6) not null
);
create index if not exists sales_order_history_order_idx on checkout.sales_order_history (order_id, date_added);

-- The ledger: one row per transition and per inbound signal, including the ones that changed nothing.
-- (order, source, source_ref) is unique where source_ref is set — that is what makes a redelivered signal a DUPLICATE.
create table if not exists checkout.sales_order_event
(
    event_id               bigint       not null primary key,
    order_id               bigint       not null constraint fk_sales_order_event_order references checkout.sales_order,
    event_type             varchar(40)  not null,
    source                 varchar(20)  not null constraint sales_order_event_source_check
        check (source in ('PLACEMENT', 'PAYMENT', 'INVENTORY', 'CONSOLE', 'JOB', 'SYSTEM')),
    source_ref             varchar(120),
    outcome                varchar(10)  not null constraint sales_order_event_outcome_check
        check (outcome in ('APPLIED', 'DUPLICATE', 'IGNORED')),
    order_status_after     varchar(20),
    payment_status_after   varchar(30),
    inventory_status_after varchar(30),
    pending_action_after   varchar(20),
    payload                text,
    reason                 varchar(255),
    occurred_at            timestamp(6) not null
);
create unique index if not exists sales_order_event_dedup_idx on checkout.sales_order_event (order_id, source, source_ref)
    where source_ref is not null;
create index if not exists sales_order_event_order_idx on checkout.sales_order_event (order_id, occurred_at);
