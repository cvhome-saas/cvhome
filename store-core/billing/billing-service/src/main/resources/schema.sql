-- Source of truth for the billing service's DDL. `ddl-auto` is only a safety net; every column, index and CHECK
-- constraint below is written by hand and reviewed.
--
-- Every enum column carries a CHECK listing its literals. That is deliberate duplication: it makes the database
-- reject a value the Java enum no longer knows about, which is what catches a rename that only half landed. Adding
-- an enum constant therefore means editing this file in the same change.

create schema if not exists billing;

-- ------------------------------------------------------------------ plan catalog

CREATE TABLE IF NOT EXISTS billing.plan
(
    id                varchar(24)  not null,
    code              varchar(30)  not null,
    display_name      varchar(60)  not null,
    description       varchar(255),
    tier              int          not null,
    active            boolean      not null default true,
    stripe_product_id varchar(60),
    created_date      timestamp    not null,
    updated_date      timestamp    not null,
    version           int,
    constraint plan_pk primary key (id),
    constraint plan_code_uq unique (code),
    constraint plan_stripe_product_uq unique (stripe_product_id),
    constraint plan_tier_ck check (tier >= 0)
);

CREATE TABLE IF NOT EXISTS billing.plan_price
(
    id               varchar(24) not null,
    plan_id          varchar(24) not null,
    currency         varchar(3)  not null,
    unit_amount      bigint      not null,
    billing_interval varchar(10) not null,
    trial_days       int         not null default 0,
    active           boolean     not null default true,
    stripe_price_id  varchar(60),
    created_date     timestamp   not null,
    updated_date     timestamp   not null,
    version          int,
    constraint plan_price_pk primary key (id),
    constraint plan_price_plan_fk foreign key (plan_id) references billing.plan (id),
    constraint plan_price_uq unique (plan_id, currency, billing_interval),
    constraint plan_price_stripe_uq unique (stripe_price_id),
    constraint plan_price_interval_ck check (billing_interval in ('MONTH', 'YEAR')),
    constraint plan_price_amount_ck check (unit_amount >= 0),
    constraint plan_price_trial_ck check (trial_days >= 0)
);
CREATE INDEX IF NOT EXISTS idx_plan_price_plan_active ON billing.plan_price (plan_id, active);

-- limit_value xor flag_value, or neither. Both null means unlimited, which is not the same as a zero limit — a plan
-- that simply does not mention a key must not read as a plan that forbids it.
CREATE TABLE IF NOT EXISTS billing.plan_entitlement
(
    id              varchar(24) not null,
    plan_id         varchar(24) not null,
    entitlement_key varchar(40) not null,
    limit_value     int,
    flag_value      boolean,
    version         int,
    constraint plan_entitlement_pk primary key (id),
    constraint plan_entitlement_plan_key_uq unique (plan_id, entitlement_key),
    constraint plan_entitlement_plan_fk foreign key (plan_id) references billing.plan (id),
    constraint plan_entitlement_key_ck check (entitlement_key in
                                              ('MAX_PRODUCTS', 'MAX_ORDERS_MONTH', 'MAX_ACCOUNTS', 'MAX_STORAGE_MB',
                                               'CUSTOM_DOMAIN', 'ANALYTICS', 'PRIORITY_SUPPORT')),
    constraint plan_entitlement_shape_ck check (
        (limit_value is not null and flag_value is null)
            or (limit_value is null and flag_value is not null)
            or (limit_value is null and flag_value is null))
);

-- ------------------------------------------------------------------ subscription

-- id is the store id: one subscription per store, and the store is what is being paid for.
CREATE TABLE IF NOT EXISTS billing.store_subscription
(
    id                     varchar(24) not null,
    org_id                 varchar(24) not null,
    status                 varchar(20) not null,
    plan_id                varchar(24),
    plan_price_id          varchar(24),
    stripe_customer_id     varchar(60),
    stripe_subscription_id varchar(60),
    stripe_schedule_id     varchar(60),
    current_period_start   timestamp,
    current_period_end     timestamp,
    trial_end              timestamp,
    cancel_at_period_end   boolean     not null default false,
    canceled_at            timestamp,
    suspended_at           timestamp,
    pending_plan_price_id  varchar(24),
    pending_effective_at   timestamp,
    grace_until            timestamp,
    created_date           timestamp   not null,
    updated_date           timestamp   not null,
    version                int,
    constraint store_subscription_pk primary key (id),
    constraint store_subscription_plan_fk foreign key (plan_id) references billing.plan (id),
    constraint store_subscription_price_fk foreign key (plan_price_id) references billing.plan_price (id),
    constraint store_subscription_pending_price_fk
        foreign key (pending_plan_price_id) references billing.plan_price (id),
    constraint store_subscription_stripe_uq unique (stripe_subscription_id),
    constraint store_subscription_status_ck check (status in
                                                   ('PENDING', 'TRIALING', 'ACTIVE', 'PAST_DUE', 'SUSPENDED',
                                                    'CANCELED')),
    -- anything past PENDING must name the plan it is on
    constraint store_subscription_plan_required_ck check (status = 'PENDING' or plan_id is not null)
);
CREATE INDEX IF NOT EXISTS idx_store_subscription_org ON billing.store_subscription (org_id);
CREATE INDEX IF NOT EXISTS idx_store_subscription_customer ON billing.store_subscription (stripe_customer_id);
CREATE INDEX IF NOT EXISTS idx_store_subscription_status_period
    ON billing.store_subscription (status, current_period_end);
CREATE INDEX IF NOT EXISTS idx_store_subscription_status_grace
    ON billing.store_subscription (status, grace_until);
CREATE INDEX IF NOT EXISTS idx_store_subscription_status_trial
    ON billing.store_subscription (status, trial_end);
CREATE INDEX IF NOT EXISTS idx_store_subscription_pending
    ON billing.store_subscription (pending_effective_at) where pending_plan_price_id is not null;

-- The org's one trial. The primary key is the enforcement: two concurrent first-store creations both try to insert,
-- exactly one wins, and the loser's store starts unpaid. No read-then-write race to lose.
CREATE TABLE IF NOT EXISTS billing.org_trial_grant
(
    org_id     varchar(24) not null,
    store_id   varchar(24) not null,
    granted_at timestamp   not null,
    trial_end  timestamp   not null,
    version    int,
    constraint org_trial_grant_pk primary key (org_id),
    constraint org_trial_grant_store_uq unique (store_id)
);

-- ------------------------------------------------------------------ invoices

CREATE TABLE IF NOT EXISTS billing.subscription_invoice
(
    id                     varchar(60)  not null,
    store_id               varchar(24)  not null,
    org_id                 varchar(24)  not null,
    stripe_subscription_id varchar(60),
    invoice_number         varchar(40),
    status                 varchar(20)  not null,
    currency               varchar(3)   not null,
    amount_due             bigint       not null,
    amount_paid            bigint       not null,
    period_start           timestamp,
    period_end             timestamp,
    hosted_invoice_url     varchar(500),
    invoice_pdf_url        varchar(500),
    issued_at              timestamp    not null,
    paid_at                timestamp,
    created_date           timestamp    not null,
    version                int,
    constraint subscription_invoice_pk primary key (id),
    constraint subscription_invoice_status_ck check (status in ('DRAFT', 'OPEN', 'PAID', 'UNCOLLECTIBLE', 'VOID'))
);
CREATE INDEX IF NOT EXISTS idx_subscription_invoice_store
    ON billing.subscription_invoice (store_id, issued_at desc);

-- ------------------------------------------------------------------ audit

-- Append-only. Never updated, never deleted by the application: "who moved this store onto the cheaper plan, and
-- when" is a question that gets asked months later, usually during a billing dispute.
CREATE TABLE IF NOT EXISTS billing.subscription_audit
(
    id              bigserial   not null,
    store_id        varchar(24) not null,
    org_id          varchar(24) not null,
    event_type      varchar(40) not null,
    from_status     varchar(20),
    to_status       varchar(20),
    from_plan_id    varchar(24),
    to_plan_id      varchar(24),
    source          varchar(20) not null,
    actor           varchar(120),
    stripe_event_id varchar(80),
    detail          text,
    occurred_at     timestamp   not null,
    constraint subscription_audit_pk primary key (id),
    constraint subscription_audit_event_ck check (event_type in
                                                  ('CREATED', 'TRIAL_STARTED', 'ACTIVATED', 'RENEWED',
                                                   'PAYMENT_FAILED', 'PAST_DUE', 'SUSPENDED', 'RESUMED',
                                                   'PLAN_UPGRADED', 'PLAN_DOWNGRADE_SCHEDULED',
                                                   'PLAN_DOWNGRADE_APPLIED', 'CANCEL_SCHEDULED', 'CANCEL_REVOKED',
                                                   'CANCELED', 'INVOICE_RECORDED', 'QUOTA_REFUSED')),
    constraint subscription_audit_from_status_ck check (from_status is null or from_status in
                                                        ('PENDING', 'TRIALING', 'ACTIVE', 'PAST_DUE', 'SUSPENDED',
                                                         'CANCELED')),
    constraint subscription_audit_to_status_ck check (to_status is null or to_status in
                                                      ('PENDING', 'TRIALING', 'ACTIVE', 'PAST_DUE', 'SUSPENDED',
                                                       'CANCELED')),
    constraint subscription_audit_source_ck check (source in ('API', 'WEBHOOK', 'JOB', 'SYSTEM'))
);
CREATE INDEX IF NOT EXISTS idx_subscription_audit_store ON billing.subscription_audit (store_id, occurred_at desc);
CREATE INDEX IF NOT EXISTS idx_subscription_audit_event ON billing.subscription_audit (stripe_event_id);

-- ------------------------------------------------------------------ idempotency

-- Inbound. The primary key is the race guard: a concurrent redelivery of the same event loses the insert, rolls back,
-- and its retry finds the row already there.
CREATE TABLE IF NOT EXISTS billing.processed_stripe_event
(
    event_id     varchar(80) not null,
    event_type   varchar(80) not null,
    api_version  varchar(20),
    outcome      varchar(20) not null,
    received_at  timestamp   not null,
    processed_at timestamp,
    version      int,
    constraint processed_stripe_event_pk primary key (event_id),
    constraint processed_stripe_event_outcome_ck check (outcome in ('SCHEDULED', 'IGNORED', 'FAILED'))
);
CREATE INDEX IF NOT EXISTS idx_processed_stripe_event_received ON billing.processed_stripe_event (received_at);

-- Outbound. Records the intent of a mutating Stripe call, not its outcome — a crash between the insert and the call
-- leaves completed_at null, and that is exactly what makes replaying under the same key safe.
CREATE TABLE IF NOT EXISTS billing.stripe_request
(
    -- 255 because that is Stripe's own limit on an idempotency key, and ours are built from an operation name plus
    -- two 24-character ids plus a time bucket, which already passes 80.
    idempotency_key  varchar(255) not null,
    store_id         varchar(24),
    operation        varchar(40) not null,
    -- Wider than the other Stripe id columns on purpose: this one holds whatever object the operation produced, and
    -- checkout session ids run far longer than the cus_/sub_/price_ ids stored elsewhere.
    stripe_object_id varchar(255),
    created_at       timestamp   not null,
    completed_at     timestamp,
    version          int,
    constraint stripe_request_pk primary key (idempotency_key),
    constraint stripe_request_operation_ck check (operation in
                                                  ('CUSTOMER_CREATE', 'CHECKOUT_SESSION_CREATE',
                                                   'SUBSCRIPTION_UPDATE', 'SUBSCRIPTION_CANCEL',
                                                   'SUBSCRIPTION_RESUME', 'SCHEDULE_CREATE', 'SCHEDULE_RELEASE',
                                                   'PRODUCT_CREATE', 'PRICE_CREATE'))
);
CREATE INDEX IF NOT EXISTS idx_stripe_request_store ON billing.stripe_request (store_id, created_at desc);

-- ------------------------------------------------------------------ outbox

-- Owned by the namastack outbox starter; schema-initialization is disabled so the DDL lives here with everything
-- else. Its own schema, so the outbox tables of the services sharing this database stay distinguishable.
create schema if not exists billing_outbox;

CREATE TABLE IF NOT EXISTS billing_outbox.outbox_record
(
    id             VARCHAR(255)             NOT NULL,
    status         VARCHAR(20)              NOT NULL,
    record_key     VARCHAR(255)             NOT NULL,
    record_type    VARCHAR(255)             NOT NULL,
    payload        TEXT                     NOT NULL,
    context        TEXT,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at   TIMESTAMP WITH TIME ZONE,
    failure_count  INT                      NOT NULL,
    failure_reason VARCHAR(1000),
    next_retry_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    partition_no   INTEGER                  NOT NULL,
    handler_id     VARCHAR(1000)            NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS billing_outbox.outbox_instance
(
    instance_id    VARCHAR(255) PRIMARY KEY,
    hostname       VARCHAR(255)             NOT NULL,
    port           INTEGER                  NOT NULL,
    status         VARCHAR(50)              NOT NULL,
    started_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    last_heartbeat TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS billing_outbox.outbox_partition
(
    partition_number INTEGER PRIMARY KEY,
    instance_id      VARCHAR(255),
    version          BIGINT                   NOT NULL DEFAULT 0,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_billing_outbox_record_record_key_created
    ON billing_outbox.outbox_record (record_key, created_at);

CREATE INDEX IF NOT EXISTS idx_billing_outbox_record_partition_status_retry
    ON billing_outbox.outbox_record (partition_no, status, next_retry_at);

CREATE INDEX IF NOT EXISTS idx_billing_outbox_record_status_retry
    ON billing_outbox.outbox_record (status, next_retry_at);

CREATE INDEX IF NOT EXISTS idx_billing_outbox_record_status
    ON billing_outbox.outbox_record (status);

CREATE INDEX IF NOT EXISTS idx_billing_outbox_record_record_key_completed_created
    ON billing_outbox.outbox_record (record_key, completed_at, created_at);

CREATE INDEX IF NOT EXISTS idx_billing_outbox_instance_status_heartbeat
    ON billing_outbox.outbox_instance (status, last_heartbeat);

CREATE INDEX IF NOT EXISTS idx_billing_outbox_instance_last_heartbeat
    ON billing_outbox.outbox_instance (last_heartbeat);

CREATE INDEX IF NOT EXISTS idx_billing_outbox_instance_status
    ON billing_outbox.outbox_instance (status);

CREATE INDEX IF NOT EXISTS idx_billing_outbox_partition_instance_id
    ON billing_outbox.outbox_partition (instance_id);
