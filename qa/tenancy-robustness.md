# QA — tenancy robustness

Phase 9 of `.claude/plans/tenancy-and-pod-registry-split.md`. Six defects the original audit found, plus the
locale interceptor that had to be fixed first because it blocked every write endpoint this phase needed to
test.

## What changed and why you are testing it

**The locale interceptor (prerequisite).** `RequestCacheAwareLocaleInterceptor` in `store-commons/autoconfigure`
called `setLocale` on the resolver and caught only `IllegalArgumentException`. The pod services each declare a
`SessionLocaleResolver`, so it worked there; store-core services declare none and get Spring Boot's default
`AcceptHeaderLocaleResolver`, whose `setLocale` throws `UnsupportedOperationException` by contract. Since the
interceptor is registered platform-wide, that turned a whole service's web layer into 500s the moment anything
supplied a locale — a `?lang=` parameter, or a request Spring Security cached across a login. It looked
intermittent because it only fires when a locale is actually found. Now tolerated: language for these APIs is
carried explicitly by `ServletLanguageCodeArgumentResolver`, so the framework locale is a convenience, not the
mechanism.

**Provisioning is idempotent.** It is an outbox handler, so it runs again — after a timeout, a restart, any
non-permanent failure. The pod's create is not idempotent on the pod's side, so a retry after a create that
actually landed would create the store twice. It now checks its own recorded state first.

**Refused and unreachable are no longer the same thing.** Both used to mark `FAILED_PROVISIONING` and rethrow.
A pod that never answered decided nothing — marking it failed records a verdict nobody reached — so that case
is now left mid-flight and rethrown for the outbox to retry, while a pod that *answered* with an error is
recorded failed and swallowed so the outbox stops. This required declaring the failure types on
`MerchantStorePodClient.create`; without them both arrive wrapped in the unchecked carrier and cannot be told
apart.

**Stores no longer vanish from the console.** The list merges each store's pod detail, and the per-row call was
wrapped in `catch (Exception e) { return null; }` followed by a filter — so a slow or down pod did not degrade
the screen, it *removed rows from it*, silently and with nothing logged. A merchant seeing a store missing
concludes it was deleted. It now falls back to the row tenancy already holds.

**Signup cannot orphan an organization.** The org row was committed before uaa was asked for anything, so a
duplicate email left an org with no user and no way in. Both are now in one transaction.

**Store names are actually unique.** There has always been a `checkNameExists` call, but it is a read-then-write
that two concurrent creates both pass. A unique constraint now decides, with `CHECK` constraints on
`provisioning_state` and indexes on `org_id` and `provisioning_state` alongside.

**Two dead events deleted.** `OrgCreatedEvent` was `@OutboxEvent` with **zero consumers**, so every signup wrote
an outbox row nothing would ever handle; `StoreProvisionedEvent` was registered three times per provisioning
with no listener. The plan says to add consumers — there is nothing for them to do, and adding `@OutboxEvent`
to the second would have produced *more* unconsumed rows. Deleting is the fix; a no-op consumer is worse than
no event.

## Setup

```bash
docker compose -f docker-compose-lcl.yml up -d
# uaa, billing, pod-registry, tenancy, gateway, and merchant
```

Log in as `org1-admin` / `admin`.

> **Billing's quota gate will stop you after a few creates.** Each test store is provisioned as unpaid, and
> `max-pending-stores` is 3, after which creation answers 422 `BILLING.QUOTA.STORE_EXCEEDED`. Between runs:
>
> ```sql
> delete from billing.store_subscription where id in (select id from tenancy.manager_store where name like 'TEST%');
> delete from pod_registry.pod_store_placement;
> delete from tenancy.manager_store where name like 'TEST%';
> ```

---

## Case 1 — the locale interceptor no longer 500s

`GET` and `POST` to any tenancy endpoint **with `?lang=en`**.

**Expect:** normal responses. Before, both were 500 `COMMON.INTERNAL_ERROR` with
`UnsupportedOperationException: Cannot change HTTP Accept-Language header` in the log.

## Case 2 — a duplicate store name is a typed 409

Create a store, then create another with the same name.

**Expect:** 200, then **409** `CONTROL_PLANE.STORE.NAME_TAKEN` with the name in `detail` and `params` — not
the generic `COMMON.DATA_INTEGRITY_VIOLATION`, and not a 500.

## Case 3 — concurrent creates with the same name

Fire five simultaneous creates with one name.

**Expect:** exactly **one 200 and four 409s**, all `CONTROL_PLANE.STORE.NAME_TAKEN`, and exactly one row in
`manager_store`.

## Case 4 — a store whose pod cannot be reached still appears in the list

`GET /store-manager/private/store` with stores whose pod detail cannot be fetched.

**Expect:** `totalElements` equals the number of rows returned. Under the old code the count and the rows
disagreed — that gap *was* the bug.

## Case 5 — capacity is counted through the real outbox path

Create a store, wait a few seconds.

**Expect:** `pod_registry.pod.capacity_stores` rises and a `pod_store_placement` row appears, without anyone
calling `placement-recorded` by hand. **This also closes phase 8's Case 8**, which could not be run while the
locale interceptor blocked store creation.

## Case 6 — a replayed provisioning event does not create the store twice

**Status: covered by unit test only** (`StoreProvisioningServiceTest.replayDoesNotDuplicate`). Forcing a real
outbox redelivery needs either a mid-flight kill or direct manipulation of `outbox_record`; the guard itself is
a state check with no timing component, so the unit test exercises the same branch.

## Case 7 — signup cannot orphan an organization

Sign up with an email that already exists in uaa.

**Expect:** an error, and **no new row** in `tenancy.manager_org`.

**Status: NOT RUN.** The transaction is the whole change and it is one annotation, but the case was not
exercised against the stack.

---

## Results

Run 2026-08-12, branch `fix/tenancy-robustness`, against all six services.

| Case | Result | Evidence |
|---|---|---|
| 1 — locale interceptor | **PASS** | `GET`/`POST` with `?lang=en` both 200 where both were 500 |
| 2 — duplicate name | **PASS** | 409 `CONTROL_PLANE.STORE.NAME_TAKEN`, `"A store named PHASE9-E already exists."` |
| 3 — concurrent creates | **PASS** | 5 at once → **1× 200, 4× 409**, all NAME_TAKEN, exactly one row |
| 4 — list degrades | **PASS** | `totalElements: 3`, `returnedRows: 3`, including the store whose pod call failed |
| 5 — capacity via outbox | **PASS** | `capacity_stores = 1` and a placement row, unaided — closes phase 8 case 8 |
| 6 — replay idempotency | **UNIT ONLY** | see above |
| 7 — signup orphan | **NOT RUN** | see above |

Automated: `StoreProvisioningServiceTest` — 5 tests covering replay, first run, unreachable-not-failed,
refusal-is-terminal, and a vanished store; plus the existing `StoreTenantScopingTest` 7. Full
`build -x test -x check`, module builds and checkstyle clean.

### A subtle finding worth keeping

The typed 409 did not work on the first three attempts, and the reason is not obvious. **Attaching a Spring
`DataAccessException` as the cause of a domain exception makes Spring resolve the handler from the cause.**
`ExceptionHandlerExceptionResolver` walks the cause chain, `DataIntegrityErrorHandler` claims
`DataIntegrityViolationException`, and so the advice answered with the generic
`COMMON.DATA_INTEGRITY_VIOLATION` and discarded `STORE.NAME_TAKEN` — while the catch had run correctly.
`DuplicateStoreNameException.of` now takes no cause and the constraint violation is logged at the call site.
Anyone chaining a persistence exception onto a domain one will hit this.

Related, and already known from billing: **the violation must not be caught inside the transaction that
caused it.** Postgres aborts the transaction at the constraint, so catching within `@Transactional` only trades
the error for `UnexpectedRollbackException` at commit — still a 500, minus the cause. The translation lives in
the non-transactional caller.

## Still open

1. **Store creation still fails at the pod.** Provisioning ends `FAILED_PROVISIONING` because merchant answers
   `Failed to read request` to the untyped `Map<Object,Object>` payload. Pre-existing and phase 11's job —
   this phase only made the failure *correctly classified* (refused, recorded, not retried forever) instead of
   indistinguishable from a timeout. Worth knowing that a store created locally today is not usable.
2. **Cases 6 and 7**, above.
3. **No reaper** for stores stuck `IN_PROGRESS_PROVISIONING` — that is phase 10, and the index it will scan
   was added here.
4. **`ManagerStoreEntity.createStore` still reads `request.get("name").toString()`** and NPEs on a missing
   name. Phase 11 replaces the map with a typed request.
