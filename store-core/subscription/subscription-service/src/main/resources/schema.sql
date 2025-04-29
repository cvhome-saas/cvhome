create schema if not exists subscription;
CREATE TABLE IF NOT EXISTS subscription.subscription
(
    id                  varchar(24) not null,
    created_date        timestamp   not null,
    last_renewed_date   timestamp   not null,
    end_date            timestamp   not null,
    de_activated_date   timestamp   null,
    subscription_plan   varchar(20) not null,
    recurring_plan      varchar(20) not null,
    subscription_status varchar(20) not null,
    version             int,
    constraint subscription_pk primary key (id)
);
CREATE TABLE IF NOT EXISTS subscription.subscription_price_plan
(
    id                varchar(50) not null,
    product_id        varchar(50),
    currency          varchar(6),
    price             bigint,
    subscription_plan varchar(20) not null,
    recurring_plan    varchar(20) not null,
    version           int,
    constraint subscription_price_plan_pk primary key (id),
    constraint price_product_uq unique (id, product_id),
    constraint subscription_recurring_uq unique (subscription_plan, recurring_plan)

);

