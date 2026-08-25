# User impersonation — backend requirements

Written during Module 11 (org & pod management) of the seller-ui → console-ui migration. It
graduates out of `store-core/console-ui/lessons.md` because it is a change to two services and an
audit obligation, not a screen — the precedent being
`store-core/console-template/Content Management Service - Backend Requirements.md`.

`lessons.md` links here under **"Platform — no impersonation"**. The console ships the row action
**rendered and disabled**, on `/platform/users` and on an organization's Users tab.

---

## 1. What is being asked for

A support engineer or platform administrator acting **as** a named merchant, inside the console,
so they can see exactly what that merchant is reporting.

This is the single most-requested capability that a platform console does not have, and the reason
is not that nobody built the screen. It is that acting as someone else touches the authorization
server, the gateway's session store and the audit trail at the same time, and every shortcut through
that produces a system where "who did this" has no answer.

## 2. What exists today

Nothing.

```
grep -ril "impersonat\|act_as\|actAs\|on-behalf\|token-exchange" store-core store-commons store-pod
```

returns **zero files**. There is no partial implementation to finish, no dormant column, no
half-written filter. Every piece below is new.

## 3. Why this is not a front-end change

**The console never holds a token.** `store-core-gateway` is a Spring Cloud Gateway configured as an
`oauth2Login` client: the browser holds a session cookie, the gateway holds the `OAuth2AuthorizedClient`
against that session, and `GatewayRouteLocatorImpl` puts a `tokenRelay()` filter on every backend
route so the access token is attached server-side on the way out.

Three consequences, and they decide the whole design:

1. **"Act as this user" means swapping the authorized client held in the gateway's session**, not
   minting something the browser can carry. There is no place in the browser to put a second token,
   and putting one there would be a worse system than not having the feature.
2. **The swap is a server-side state change with a lifetime**, so it needs an explicit end, a TTL,
   and a way for the console to know it is in effect.
3. **Every downstream service must be able to tell an impersonated call from a genuine one.** Today
   a JWT's `sub` is the username and nothing else distinguishes the two.

## 4. The authorization server: RFC 8693 token exchange

uaa (`store-core/uaa`) is a Spring Authorization Server. Add the token-exchange grant:

```
POST /oauth2/token
grant_type          = urn:ietf:params:oauth:grant-type:token-exchange
subject_token       = <the operator's own access token>
subject_token_type  = urn:ietf:params:oauth:token-type:access_token
requested_token_type= urn:ietf:params:oauth:token-type:access_token
audience            = <the client the console uses>
resource            = <optional: the store being investigated>
actor_token         = —                       # not used; the subject token *is* the actor
scope               = <the target's scopes, never wider>
```

Registered for the console's client only, and refused unless the subject token carries
`ROLE_SUPER_ADMIN` or `ROLE_SUPPORT`.

**The issued token must carry an `act` claim** naming the real actor, per RFC 8693 §4.1:

```json
{
  "sub": "merchant-username",
  "act": {"sub": "support-username", "role": "ROLE_SUPPORT"},
  "exp": "…"
}
```

`sub` is the merchant, so every existing `@PreAuthorize`, every `hasPermission(...)` and every
`StoreMerchantId` resolution keeps working unchanged — which is the point of using `sub` rather than
inventing a parallel identity. `act` is what makes the call *legible* afterwards.

### Hard exclusions

- **Never mint a refresh token** for an exchanged subject. An impersonation that can renew itself is
  an impersonation nobody can end.
- **Never exceed the target's scopes.** The exchange narrows; it must not widen. A support engineer
  acting as a store moderator has a store moderator's permissions and no more.
- **Never allow impersonating another platform administrator.** Refuse when the target holds
  `ROLE_SUPER_ADMIN` or `ROLE_SUPPORT`. Privilege escalation by impersonation is the obvious attack
  and the refusal costs nothing.
- **Never allow chaining.** Refuse a subject token that already carries an `act` claim.
- **Short TTL.** Fifteen minutes, and no longer than the operator's own token.

## 5. The gateway: swapping the authorized client

Two endpoints on the gateway, not on a backend service, because the gateway is what holds the
session:

```
POST /api/v1/impersonation      { "userId": "<uaa UUID>", "reason": "<free text, required>" }
  → 200 { "actingAs": "merchant-username", "expiresAt": "…" }
DELETE /api/v1/impersonation
  → 204
```

`POST` performs the token exchange above, then replaces the `OAuth2AuthorizedClient` in the session's
`ServerOAuth2AuthorizedClientRepository` with one holding the exchanged token — keeping the
operator's original authorized client alongside it, under a distinct key, so `DELETE` can restore it
without a re-login.

Requirements on that state:

- **Its own TTL**, independent of the session's, enforced on every request rather than only at
  issuance. When it lapses the gateway restores the original client silently; it must never fall
  back to the *merchant's* identity.
- **`GET /api/v1/auth/me` reports it.** The response gains `actingAs` (the merchant) beside the
  existing principal, so the console can render the banner without a second call. This is the one
  change the console strictly needs.
- **Survives nothing.** Not a browser restart, not a new tab in another browser, not a session
  resumed from a persistent store. It is per-session, in-memory, short-lived state.
- **`reason` is required** and is carried into the audit row. An impersonation with no stated reason
  is one nobody can review.

## 6. Audit

One row per issuance and one per end, both mandatory, both written by the gateway in the same request
that changes the state.

`tenancy.tenancy_audit` already has the right shape — entity type, entity id, action, from/to, actor,
source, detail — so the cheapest correct answer is a new `AuditEntityType.IMPERSONATION` with the
target's uaa id as the entity, the operator as the actor, and the reason as the detail. A dedicated
`uaa_audit` table is the alternative if uaa is judged the better owner; the requirement is the rows,
not the table.

Every audit row written **during** an impersonation must also record the real actor. Services that
write audit rows read `authentication.getName()`, which is the merchant during an impersonation, so
each needs to read the `act` claim as well:

- `TenancyAuditService.record(...)` — the actor becomes `merchant (via support-username)`, or an
  additional `on_behalf_of` column.
- `PodAuditEntity.of(...)` — the same.

Without this, the audit trail says a merchant did something they did not do, which is worse than no
audit trail at all.

## 7. The console

Small, and last:

- The row action on `/platform/users` and the organization Users tab is enabled and calls
  `POST /api/v1/impersonation` with the operator's stated reason.
- **A non-dismissible banner occupies the shell for the whole duration**, naming the merchant, the
  time remaining, and carrying the only control that ends it. Not a toast, not a badge: the failure
  mode being designed against is an operator forgetting they are someone else.
- The shell reads `actingAs` from `auth/me`, so a reload keeps the banner. Everything else in the
  console is unchanged — that is the payoff of `sub` being the merchant.
- Ending it calls `DELETE` and reloads the store list, because the merchant's rail is not the
  operator's.

## 8. Order of work

1. uaa: the token-exchange grant, the `act` claim, the four refusals. Testable on its own with a
   `.http` file.
2. Gateway: the two endpoints, the authorized-client swap, the TTL, `actingAs` on `auth/me`.
3. Audit: `IMPERSONATION` rows, and the `act`-aware actor in both existing audit services.
4. Console: the banner, the enabled row action, the reason prompt.

Nothing in step 4 is worth starting before step 3 exists. An impersonation feature without its audit
trail is the version that has to be turned off again.
