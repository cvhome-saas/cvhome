# Database schemas and persistence

Every service owns a **Postgres schema**, and every service ships its own DDL as a SQL file. There is no shared
database and no cross-service foreign key. Two different persistence stacks are in use — know which one you're
in before writing a repository or an entity.

## Two persistence stacks

| | **Spring Data JDBC** | **Spring Data JPA / Hibernate** |
|---|---|---|
| Used by | `tenancy-service` | the `store-pod` services (`payment`, `catalog`, `checkout`, `merchant`, `content`) |
| Entity annotations | `org.springframework.data.relational.core.mapping.{Table, Column}` | `jakarta.persistence.{Entity, Table, Column, Id, …}` |
| Base class | `BaseEntity<E, ID>` (`store-commons:commons`) | `SalesManagerEntity<K, E>` (`store-pod/commons/store-commons`) |
| Outbox starter | `namastack-outbox-starter-jdbc` | `namastack-outbox-starter-jpa` |
| Outbox config key | `namastack.outbox.jdbc.*` | `namastack.outbox.jpa.*` |
| Lazy loading / dirty checking | none — explicit, no proxies | full Hibernate semantics |

Both base classes extend Spring Data's `AbstractAggregateRoot`, so `registerEvent(...)` works identically on
either side (`events-outbox.md`).

```java
// tenancy — Spring Data JDBC
@Table(schema = "tenancy", name = "manager_store")
public class ManagerStoreEntity extends BaseEntity<ManagerStoreEntity, StoreMerchantId> { ... }

// payment — JPA
@Entity
@Table(name = "TRANSACTION", uniqueConstraints = { @UniqueConstraint(...) })
public class Transaction extends SalesManagerEntity<Long, Transaction> { ... }
```

Note the difference in how the schema is chosen: JDBC entities name their schema **explicitly per `@Table`**,
while JPA entities omit it and rely on `spring.jpa.properties.hibernate.default_schema`.

## Schema-per-service

From `common-config.yml` (applies to every service):

```yaml
spring:
  sql:
    init:
      mode: always                       # run the DDL on every startup
  datasource:
    driver-class-name: org.postgresql.Driver
    hikari:
      schema: ${spring.application.name}  # default search path
  jpa:
    hibernate:
      ddl-auto: update
```

Then per service:

```yaml
# payment-service — JPA, explicit DDL location
spring:
  sql:
    init:
      schema-locations: classpath:init-sql/schema.sql
      data-locations: classpath:init-sql/data-common.sql
  jpa:
    properties:
      hibernate:
        default_schema: ${spring.application.name}      # → "payment"
```

`tenancy-service` puts its DDL at the Spring Boot default location (`classpath:schema.sql`), so it needs
no `schema-locations` entry at all.

> `ddl-auto: update` **and** a hand-written `schema.sql` both run. The SQL file is the source of truth
> (everything is `CREATE TABLE IF NOT EXISTS`); Hibernate's `update` is a safety net that adds columns for
> JPA entities. Don't rely on it — add new tables and columns to `schema.sql`.

### Where the DDL lives

| Service | DDL file | Data seed |
|---|---|---|
| `tenancy-service` | `src/main/resources/schema.sql` | — |
| `payment-service` | `src/main/resources/init-sql/schema.sql` | `init-sql/data-common.sql`, `init-sql/data-test-stores.sql`, `init-sql/stores/` |

Pod services follow the `init-sql/` convention: `schema.sql` + `data-common.sql` (reference data loaded always)
+ `data-test-stores.sql` (seeded demo stores, tied to the `test-stores` profile — see `configuration.md`).

### Schemas actually created

**`tenancy-service`** is the exception — it owns **three** schemas rather than one, split by bounded
context:

| Schema | Tables |
|---|---|
| `tenancy` | `manager_org`, `manager_store` |
| `org` | `pod` |
| `tenancy_outbox` | the three outbox tables |

That mirrors the module split (`tenancy-commons`, `tenancy-events`, `pod-external-api`) — the code
boundaries are reflected in the database.

**`payment-service`** uses a single `payment` schema: `payment_configuration`, `transaction`, `sm_sequencer`,
plus the outbox tables.

## Conventions visible in the DDL

- **Ids are `varchar(24)`** in tenancy — that's a Mongo `ObjectId` hex string, matching `PodId` /
  `StoreMerchantId` / `ManagerOrgId` (`api-conventions.md`). Pod-side ids are `varchar(50)`
  (`store_merchant_id`) — the same store id, in a wider column.
- **`version int`** on tenancy tables — optimistic locking via Spring Data JDBC.
- **`sm_sequencer`** in pod schemas is the Shopizer-inherited `@TableGenerator` sequence table
  (`SEQ_NAME`/`SEQ_COUNT`), used by JPA entities like `Transaction` instead of a Postgres sequence.
- **Enums are `varchar` with a `CHECK` constraint**, not Postgres enum types:
  ```sql
  status varchar(255) check (status in ('PENDING','PROCESSING','PAID','FAILED','EXPIRED',
                                        'CANCELLED','WAITING_VERIFICATION','REJECTED','PAY_LATER'))
  ```
  **Adding an enum value therefore requires a DDL change**, not just a Java change — the check constraint will
  reject the new value otherwise. Same for `payment_type` (`COD`, `MANUAL_TRANSFER`, `PAYPAL`, `STRIPE`).
- **Tenant scoping is in the key.** `payment_configuration` is keyed
  `primary key (store_merchant_id, payment_type)`, and `transaction` carries
  `unique (request_ref, store_merchant_id)`. Uniqueness is per store, never global.
- **Encrypted columns are plain `varchar(255)`** (`api_key`, `secret_key`, `webhook_secret`) holding the
  `ENC:…` envelope — see `secrets-encryption.md`. Size them for base64 ciphertext, which is larger than the
  plaintext.
- **Foreign keys stay inside a schema/service.** `manager_store.org_id → manager_org.id` exists;
  `manager_store.pod_id → org.pod.id` does **not**, even though both live in the same database — pods are
  conceptually a different bounded context.

## The outbox tables

Both services create the same three tables, in their own schema (`control.*` / `payment.*`), because the
library's auto-creation is **switched off** in this repo:

```yaml
namastack.outbox.jdbc.schema-initialization.enabled: false   # tenancy
namastack.outbox.jpa.schema-initialization.enabled:  false   # payment
```

The library would otherwise create them on startup (`jdbc.schema-initialization.enabled` defaults to `true`).
Here the DDL is checked in alongside the business tables, which keeps schema changes reviewable and lets each
service place the tables in a schema it chooses via `schema-name`.

```sql
CREATE TABLE IF NOT EXISTS <schema>.outbox_record (
    id             VARCHAR(255) NOT NULL PRIMARY KEY,
    status         VARCHAR(20)  NOT NULL,     -- NEW / PENDING / COMPLETED / FAILED
    record_key     VARCHAR(255) NOT NULL,     -- from @OutboxEvent(key = ...) — the ordering key
    record_type    VARCHAR(255) NOT NULL,     -- event class, used to match handlers
    payload        TEXT         NOT NULL,     -- serialized event
    context        TEXT,                      -- propagated trace ids / tenant context
    created_at     TIMESTAMPTZ  NOT NULL,
    completed_at   TIMESTAMPTZ,
    failure_count  INT          NOT NULL,
    failure_reason VARCHAR(1000),
    next_retry_at  TIMESTAMPTZ  NOT NULL,     -- backoff schedule
    partition_no   INTEGER      NOT NULL,     -- which partition owns this record
    handler_id     VARCHAR(1000) NOT NULL
);

CREATE TABLE IF NOT EXISTS <schema>.outbox_instance (
    instance_id VARCHAR(255) PRIMARY KEY,
    hostname VARCHAR(255) NOT NULL, port INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL, last_heartbeat TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS <schema>.outbox_partition (
    partition_number INTEGER PRIMARY KEY,
    instance_id      VARCHAR(255),
    version          BIGINT NOT NULL DEFAULT 0,
    updated_at       TIMESTAMPTZ NOT NULL
);
```

Plus indexes on `(record_key, created_at)`, `(partition_no, status, next_retry_at)`, `(status, next_retry_at)`,
`(status)`, `(record_key, completed_at, created_at)`, and on the instance table `(status, last_heartbeat)`.
Payment additionally indexes `outbox_instance(last_heartbeat)`, `outbox_instance(status)` and
`outbox_partition(instance_id)` — tenancy's file stops earlier, a small inconsistency rather than a
deliberate difference.

**What the three tables mean together:** `outbox_instance` is a heartbeat registry of running replicas,
`outbox_partition` assigns partitions to instances (with a `version` column for optimistic claim), and each
`outbox_record` belongs to a `partition_no`. That's how multiple replicas of the same service divide the work
without processing the same record twice, and how records sharing a `record_key` stay ordered. Operational
detail in `events-outbox.md`.

## Adding a table or column

1. Edit the service's `schema.sql` (`init-sql/schema.sql` for pod services) with `CREATE TABLE IF NOT EXISTS` /
   an idempotent `ALTER`.
2. Put it in the right schema — pod services use one schema named after the app; tenancy picks the
   bounded-context schema.
3. JDBC entity → add `@Table(schema = "...", name = "...")`; JPA entity → omit the schema, it comes from
   `hibernate.default_schema`.
4. Widening an enum? Update the `CHECK` constraint too.
5. Never add a foreign key that crosses a service boundary.
