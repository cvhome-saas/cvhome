# Domain events & the transactional outbox

Where a service-to-service **call** would create temporal coupling (the caller fails if the callee is down),
cvhome uses **domain events published through a transactional outbox** instead. Currently wired in
`control-plane-service` and `payment-service`.

Library: `io.namastack:namastack-outbox` (`namastack-outbox = 1.7.1` in the version catalog) —
`-starter-jpa` in payment, `-starter-jdbc` in control-plane, `-api` in the `-commons`/`-events` modules that
only need the annotations.

## The DDD layer: aggregate roots register events

Entities extend `SalesManagerEntity`, which extends Spring Data's **`AbstractAggregateRoot`**:

```java
public abstract class SalesManagerEntity<K extends Serializable & Comparable<K>, E extends SalesManagerEntity<K, E>>
        extends AbstractAggregateRoot<E> implements Serializable, Comparable<E> { ... }
```

That gives every entity `registerEvent(...)`. **State transitions are methods on the aggregate**, and the
aggregate records what happened rather than calling anyone:

`store-pod/payment/payment-core/.../entity/payment/Transaction.java`:

```java
public Transaction success(String transactionNo) {
    this.status = PaymentStatus.PAID;
    this.transactionNo = transactionNo;
    this.registerEvent(PaymentPaidEvent.from(this.internalRef, this.requestRef, this.storeMerchantId.getId()));
    return this;
}

public Transaction failed() {
    this.status = PaymentStatus.FAILED;
    this.registerEvent(PaymentFailedEvent.from(...));
    return this;
}
```

This is the core DDD idea in the codebase: **no setter-driven status mutation from a service class.** Call
`transaction.success(...)`, and the invariant (status change + event) is enforced in one place. Spring Data
drains registered events when the repository saves the aggregate — in the *same transaction* as the state
change, which is what makes the outbox atomic.

## The event contract

Events are records implementing `com.asrevo.cvhome.commons.event.Event` (from `store-commons:commons`) and
annotated `@OutboxEvent`:

```java
@OutboxEvent(key = "#this.internalRef()")
public record PaymentPaidEvent(String internalRef, String requestRef, String storeId, Map<String, String> data)
        implements Event {

    public static PaymentPaidEvent from(String internalRef, String requestRef, String storeId) { ... }

    @Override public String eventType() { return PaymentPaidEvent.class.getSimpleName(); }
    @Override public Map<String, String> data() { return Map.of("internalRef", internalRef, ...); }
}
```

- `@OutboxEvent(key = ...)` — a SpEL expression giving the **partition/ordering key**. Events sharing a key are
  processed in order, so all events for one payment reference stay sequenced.
- `Event.eventType()` / `data()` — the repo's own envelope contract, plus a default `getDestinations()`.
- Static `from(...)` factories keep construction at the call site terse.

## The handler side

Consumers annotate methods with `@OutboxHandler`; the poller dispatches by event type.

`payment-service` — reacts to its own payment events by telling checkout the order's payment status changed:

```java
@Component
public class PaymentOutboxHandler {
    private final ExternalOrderService externalOrderService;

    @OutboxHandler
    public void handlePaymentPaidEvent(PaymentPaidEvent event) {
        externalOrderService.updatePaymentStatus(
            new StoreMerchantId(event.storeId()), event.requestRef(), PaymentStatus.PAID);
    }
    // ... handlePaymentFailedEvent, handlePaymentCanceledEvent
}
```

Note what this buys: the **HTTP call to checkout happens in the handler, after the transaction committed**. If
checkout is down, the payment is still recorded and the outbox retries — no lost payment, no distributed
transaction.

`control-plane-service` — same shape, with an extra `EventImpl<T>` interface (from `store-commons:commons`) as a
naming convention:

```java
@Service
public class ManagerStoreCreatedEventImpl implements EventImpl<StoreCreatedEvent> {
    private final StoreProvisioningService storeProvisioningService;

    @OutboxHandler
    public void process(StoreCreatedEvent event) {
        storeProvisioningService.provisioning(event.orgId(), event.store(), event.podId(), event.request());
    }
}
```

Store provisioning is slow and failure-prone, so it is deliberately decoupled from the HTTP request that created
the store.

Handlers in `control-plane-service` live under `subscription/processors/{event,command}/` and
`manager/processors/event/`: `OrgCreatedEventImpl`, `ManagerStoreCreatedEventImpl`,
`InvoicePaymentSucceededEventImpl`, `InvoicePaymentFailedEventImpl`, `CustomerSubscriptionDeletedEventImpl`,
`DeActivateNonRenewedSubscriptionCommandImpl`.

## Events vs. commands

`subscription-events` holds both:

- **Events** (`SubscriptionEvent`, `SubscriptionDeActivateEvent`, `InvoicePaymentSucceededEvent`) — "this
  happened", emitted by aggregates or Stripe webhooks.
- **Commands** (`SubscriptionCommand`, `DeActivateNonRenewedSubscriptionCommand`) — "do this", enqueued by
  schedulers. `DeActivateNonRenewedSubscriptionsJob` writes a command to the outbox so the actual work is
  retried durably instead of running inline in the job thread.

## Where events live in the module layout — put them in a separate `-events` module

**An event is a published contract, not an implementation detail.** Any service that listens to an event must
know its structure and properties — the record's fields, the `eventType()`, the `@OutboxEvent` key. So the event
types need to be depended on *independently* of the code that produces them.

That is why control-plane has dedicated **`-events` modules** — `manager-events`, `subscription-events` — each
depending only on its `-commons` sibling plus `namastack-outbox-api`:

```groovy
// store-core/control-plane/subscription-events/build.gradle
api project(':store-core:control-plane:subscription-commons')
api libs.namastack.outbox.api
```

A consumer takes a dependency on `subscription-events` alone and gets exactly the event contracts — no
repositories, no services, no database driver, no transitive pull of the producer's internals. It is the same
principle as `-external-api` for synchronous calls (`service-to-service.md`): **the contract ships in its own
tiny module so consumers can depend on the contract without depending on the producer.**

**Prefer a `-events` module for any event another service might consume.** The cost is one small Gradle module;
the cost of getting it wrong is a consumer forced to depend on a whole `-core`, which reintroduces exactly the
coupling the outbox was meant to remove.

The counter-example in the repo: **payment** keeps its events inside `payment-commons`
(`model/payment/event/payment/*`, `model/payment/event/webhook/*`) with `api libs.namastack.outbox.api`. That
works today only because payment's events are consumed by payment itself. `payment-commons` is already a leaf
module, so it isn't harmful — but the moment another pod needs to react to `PaymentPaidEvent`, splitting a
`payment-events` module out is the right move rather than making that pod depend on all of `payment-commons`.

## Configuration

`payment-service/application.yml` (JPA variant):

```yaml
namastack:
  outbox:
    multicaster: { enabled: true }
    polling:
      fixed: { interval: 2s }
      batch-size: 10
    jpa:
      schema-initialization: { enabled: false }
      schema-name: ${spring.application.name}      # → "payment"
```

`control-plane-service/application.yml` is identical except `jdbc:` instead of `jpa:` and
`schema-name: control`. **The `jdbc.` vs `jpa.` prefix must match the starter** —
`namastack-outbox-starter-jdbc` for control-plane (Spring Data JDBC), `-starter-jpa` for payment (Hibernate).

Both explicitly set `schema-initialization.enabled: false`, overriding the library default of `true`: the
outbox DDL is checked into each service's own `schema.sql` alongside its business tables rather than being
created at startup. See `database-schemas.md` for the three tables and their columns.

### Settings the repo relies on but doesn't spell out

Everything else runs on library defaults. The ones worth knowing:

| Property | Default | Why it matters here |
|---|---|---|
| `retry.policy` | `exponential` | with `retry.exponential.initial-delay: 2s`, `max-delay: 1m`, `multiplier: 2.0` |
| `retry.max-retries` | `3` | after 3 failures a record goes `FAILED` and stops retrying — it will not self-heal |
| `processing.stop-on-first-failure` | `true` | a failing record blocks later records **for the same key**, preserving order |
| `processing.delete-completed-records` | `false` | **completed records are kept forever** — `outbox_record` grows unbounded |
| `instance.heartbeat-interval` | `5s` | replicas heartbeat into `outbox_instance` |
| `instance.stale-instance-timeout` | `30s` | after this, another instance may take over its partitions |
| `instance.rebalance-interval` | `10s` | how often partitions are recalculated across live instances |
| `multicaster.publish-after-save` | `true` | events are published to listeners after the aggregate is saved |

Two operational consequences to keep in mind:

- **`outbox_record` is append-only in this configuration.** Nothing prunes completed rows. A long-running
  service needs either a retention job or `processing.delete-completed-records: true` — worth checking before
  a table gets large.
- **A permanently failing handler stops at `max-retries: 3` and stays `FAILED`.** Because
  `stop-on-first-failure` is on, records queued behind it *for the same `record_key`* wait. When debugging
  "the event never arrived", query `outbox_record` for `status = 'FAILED'` and read `failure_reason`.

## Delivery semantics: at-least-once — handlers must be idempotent

The outbox gives **at-least-once** delivery, not exactly-once. A handler can run more than once for the same
event: a retry after a partial failure, or a crash between doing the work and marking the record `COMPLETED`.

**Write every `@OutboxHandler` so that running it twice is harmless.** The existing handlers satisfy this by
being idempotent state assignments rather than increments —
`externalOrderService.updatePaymentStatus(store, ref, PaymentStatus.PAID)` sets the same value however many
times it runs. Provisioning is likewise keyed on the store id. If you need to do something that is *not*
naturally idempotent (charging a card, sending an email, incrementing a counter), guard it with a dedup key or
a status check inside the handler.

Handler methods may take a second parameter for context:

```java
@OutboxHandler
public void handle(PaymentPaidEvent event, OutboxRecordMetadata metadata) { ... }
```

`OutboxRecordMetadata` exposes `key`, `handlerId`, `failureCount`, `attempt`, `isRetry`, and `context`
(propagated trace ids / tenant info) — `isRetry` and `attempt` are the hooks for making a risky side effect
safe on replay.

### Ordering

Records sharing a `record_key` are processed in order; different keys proceed in parallel across partitions.
That is exactly why `@OutboxEvent(key = ...)` matters:

```java
@OutboxEvent(key = "#this.internalRef()")          // all events for one payment stay sequenced
@OutboxEvent(key = "#this.store().id().toString()") // all events for one store stay sequenced
```

**Choose the key as the entity whose event order must be preserved.** Too coarse a key (a constant, or the
store id where the payment ref would do) serializes unrelated work and becomes a throughput bottleneck; too
fine a key loses the ordering you wanted.

### Status lifecycle

`outbox_record.status` moves `NEW` → `PENDING` → `COMPLETED`, or → `FAILED` once retries are exhausted.

## Stripe webhooks → outbox

`control-plane`'s `StripeWebhookController` and payment's `PublicPaymentWebhookApi` convert provider callbacks
into outbox events (`InvoicePaymentSucceededEvent`, `InvoicePaymentFailedEvent`,
`CustomerSubscriptionDeletedEvent`, `WebhookEvent` handled by `WebhookOutboxHandler`). The webhook endpoint
persists and returns 200 fast; the real work happens in the handler. Payment providers retry aggressively on
slow or failed responses, so this is deliberate.

## When to use which

| Situation | Use |
|---|---|
| Caller needs the result now (read a product, reserve stock) | Synchronous `-external-api` client — see `service-to-service.md` |
| Something happened and others should react; caller must not block or fail | Domain event + `@OutboxHandler` |
| Inbound webhook from an external provider | Persist to outbox, ack immediately, handle async |
| Scheduled/batch work that must survive restarts | Outbox **command** |

## Adding a new domain event

1. Define the record in a dedicated `-events` module (see above — that is the default; only fall back to
   `-commons` if the event is genuinely private to the service): implement `Event`, annotate
   `@OutboxEvent(key = "#this.someId()")`, add a static factory.
2. `registerEvent(...)` it from an **aggregate method** that performs the state change — not from a service.
3. Add an `@OutboxHandler` method in the consuming service.
4. Confirm the consuming service has the right starter (`-starter-jpa` or `-starter-jdbc`), a matching
   `namastack.outbox.{jpa,jdbc}` block in its `application.yml`, and the three outbox tables in its
   `schema.sql` (`database-schemas.md`).
5. Make the handler idempotent — delivery is at-least-once.

> Library reference: <https://www.namastack.io/outbox/reference/>
