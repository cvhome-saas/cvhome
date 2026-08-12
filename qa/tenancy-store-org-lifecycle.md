# QA — store and organization lifecycle

Phase 10 of `.claude/plans/tenancy-and-pod-registry-split.md`. Store status, the stuck-provisioning reaper, org
profile and status, members, invitations, and an audit trail over all of it.

## What changed and why you are testing it

Tenancy could create a store and nothing else. There was no way to close one, no way to name an organization or
close it, no members beyond the single administrator signup creates, and no record of who changed what.

**Store status is separate from provisioning state**, deliberately. `ProvisioningState` is the machine's answer —
did the pod create succeed. `StoreStatus` is the operator's: an ACTIVE store that failed provisioning is broken,
a SUSPENDED store that provisioned perfectly is deliberately closed. Folding them together would mean you could
not suspend a store that was still building.

**Delete is soft.** Billing holds a subscription against the store id and pod-registry holds a placement;
removing the row would orphan both and erase the history of a store that existed. `DELETED` is terminal, and the
store list excludes it — which is why the list moved off Query-by-Example, since Example can express the optional
filters but not "and not deleted".

**Suspension blocks entering a store, not reading its record.** `requireOperable` guards the router lookup and
the pod-detail fetch — the calls the console makes to *work in* a store — while the store's own record stays
readable so the console can show why it is closed. Suspending an organization suspends its stores without
writing to any of them: the org owns its status and `requireOperable` reads both, because a fan-out write drifts
the moment one update fails.

**Invitations carry a bearer token and are handled accordingly.** *There is no mail sender anywhere in this
platform* — verified, not assumed — so nothing can email the invitee. Creating an invitation returns a one-time
token and stores only its SHA-256 hash; the console shows a link for an admin to send by whatever means they
already use. That is the same handling a password reset gets, and adding delivery later changes who transports
the link, not how it is stored. Resending **rotates** the token: "resend" usually means the first link went
astray, and a link that went astray should stop working.

**Accept is authenticated but carries no permission token**, which looks like an omission and is not: the
invitee is not yet a member, so no org-scoped check could pass. The token in the link is the authorization,
which is why it is random, hashed at rest and single-use. All four failure modes — unknown, spent, revoked,
expired — return one error, so the endpoint cannot be used to probe which tokens existed.

## Setup

```bash
docker compose -f docker-compose-lcl.yml up -d
# uaa, billing, pod-registry, tenancy, gateway
```

Logins: `super-admin` / `admin` for suspend and resume; `org1-admin` / `admin` for members and invitations.

> The reaper's defaults are deliberately slow (`reap-rate` PT5M, `stuck-after` PT15M) because a pod that is
> merely slow is not stuck. To see it act, restart tenancy with
> `--com.asrevo.cvhome.tenancy.provisioning.reap-rate=PT10S --com.asrevo.cvhome.tenancy.provisioning.stuck-after=PT1S`.

---

## Case 1 — suspend and resume a store

As super-admin: `POST /store-manager/private/store/suspend?store=…&reason=…`, then `/resume`.

**Expect:** 200 each, with `status` moving `ACTIVE → SUSPENDED → ACTIVE` in the response body.

## Case 2 — a suspended store blocks the console but stays readable

With a store SUSPENDED, as an org admin:

| Request | Expect |
|---|---|
| `GET /router/store-pod-by-store-id?store=<suspended>` | **422** `CONTROL_PLANE.STORE.NOT_OPERABLE` |
| `GET /router/store-pod-by-store-id?store=<active>` | 200 |
| `GET /store-manager/store-info?store=<suspended>` | **200** — the record is still readable |

The third row is the point: blocking the record too would leave the console unable to explain the suspension.

## Case 3 — lifecycle rules

- `DELETED` is terminal: resuming a deleted store → **422** `ILLEGAL_TRANSITION`.
- Suspending an already-suspended store → 200, nothing changes, **and an audit row is still written**.
- A soft-deleted store disappears from `GET /store-manager/private/store`.

**Status: unit-tested** (`StoreLifecycleServiceTest`), not exercised on the stack.

## Case 4 — the invitation flow

As org1-admin, against `/org-member`:

1. `POST /invitations?email=Newbie@Example.COM&role=STORE_ADMIN` → 200, a token, email normalised to lowercase.
2. `POST /invitations` again for the same address → **409** `INVITATION.ALREADY_EXISTS`.
3. `GET /invitations` → the token **must not** appear anywhere in the response.
4. `POST /invitations/accept?token=…` → 200, status `ACCEPTED`.
5. Accept again → **422** `INVITATION.NOT_USABLE`.
6. `GET /list` → the accepted user is a member.

## Case 5 — the reaper

Set a store to `IN_PROGRESS_PROVISIONING`, restart tenancy with the fast settings above, wait.

**Expect:** it returns to `NOT_STARTED_PROVISIONING` — eligible for the ordinary provisioning path again — and a
`source = JOB` audit row records it. The reaper resets rather than calling the pod itself, so the idempotent
provisioning path added in phase 9 stays the only place that knows how to talk to a pod.

## Case 6 — everything is audited

`select * from tenancy.tenancy_audit order by recorded_at`

**Expect:** a row per mutation with the previous and new state, the actor, and `API` or `JOB`.

---

## Results

Run 2026-08-12, branch `feat/tenancy-store-org-lifecycle`.

| Case | Result | Evidence |
|---|---|---|
| 1 — suspend / resume | **PASS** | 200 each, `status: "SUSPENDED"` then back |
| 2 — blocks entry, stays readable | **PASS** | 422 `STORE.NOT_OPERABLE` on the suspended store, 200 on the active one, 200 reading the suspended record |
| 3 — lifecycle rules | **UNIT ONLY** | `StoreLifecycleServiceTest`, 6 tests |
| 4 — invitation flow | **PASS** | token 43 chars, email normalised, duplicate 409, **list does not leak the token**, accept 200, second accept 422, member added |
| 5 — reaper | **PASS** | `IN_PROGRESS → NOT_STARTED`, audit row `REPROVISION` / `system` / `JOB` |
| 6 — audit | **PASS** | STORE STATUS ×2, INVITATION CREATE, MEMBER JOIN, STORE REPROVISION — all with actor and source |

Automated: **27 tenancy tests, 0 failures** — `InvitationServiceTest` 9, `StoreTenantScopingTest` 7,
`StoreLifecycleServiceTest` 6, `StoreProvisioningServiceTest` 5. Full `build -x test -x check`, module build and
checkstyle clean.

### Found while testing

**Value objects rendered into columns and messages, three times now.** `String.valueOf(someStoreId)` yields
`ManagerStoreId[id=65f0…]`, which is 40-odd characters — it overflowed `tenancy_audit.entity_id varchar(24)`, so
*every audited change failed on the insert and took the change with it*, surfacing as a 409 on suspend. The same
shape had already produced a leaky error `detail` in phase 3 and an unusable invitation id here. Both are now
unwrapped at their single conversion point (`TenancyAuditEntity.idOf`, `InvitationService.idOf`), but the
pattern is worth watching for: a value object is not a string, and `String.valueOf` will not tell you so.

**Two statistics queries still referenced the `manager.` schema** after the rename — `ManagerStoreRepository`
and `ManagerOrgRepository`. Both would have failed the first time either statistics screen was opened. Fixed
here; the phase 1 completeness grep did not catch them because it looked for `control-plane`, not for the schema
name the rename also changed.

**Super admins cannot read a store.** `PermissionAccessChecker.hasReadAccessOnStore` checks org admin, store
admin, store moderator and store-core scope — but never `isSuperAdmin`, so a super admin gets 403 on
`store-info`. Pre-existing, unrelated to this phase, and the reason case 2 is run as an org admin. It belongs
with the `isOrgAdmin` fix, since both live in the same class.

## Still open

1. **No seller-ui for any of this.** The endpoints exist and are tested; nothing in the Angular console calls
   them yet. Invitations especially need a screen, since the token is only visible once and the console is
   supposed to be what shows the link.
2. **The invitation id is typed `ManagerStoreId`.** It is an ObjectId wrapper and works, but an invitation is
   not a store; it wants its own value object in `commons/domain`. Left alone rather than adding a type in a
   phase that is already large.
3. **Case 3 on the stack**, and org suspend end-to-end — the org path is unit-covered and shares its
   implementation with the store path, but was not exercised through the API.
4. **`tenancy_audit` grows unbounded**, like the other audit tables. Retention is a platform-wide job nobody has
   written yet.
5. **Members are not reconciled with uaa.** Removing a member here does not remove their uaa user, and a user
   deleted in uaa leaves a membership row behind.
