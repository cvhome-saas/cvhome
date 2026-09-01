# QA — spg (`store-pod/spg`)

spg is the pod's own edge: a Caddy instance that every **shopper** request goes through. It resolves the
hostname to a store, proxies `/catalog`, `/inventory`, `/content`, `/merchant`, `/checkout`, `/cua` and
`/payment` to their services, falls through to landing-ui for the page itself, compresses every JSON body,
issues TLS certificates on demand, and stamps the trace headers.

It has no `src` — it is a `Caddyfile`, a `Dockerfile` and a `compose.yml` — so nothing here is unit-testable.
Every case is a request.

- **Scope** — hostname → store resolution (`domain_lookup`), the seven proxied prefixes, `X-Forwarded-Port`,
  on-demand TLS via `ask`, compression, and the trace headers
- **Runs on** — `lcl start -d --stack <name>` brings it up as **infra**, in Docker, on
  `http://<store>.spg-507f1f77.gateway.com` (port 80 on the default stack; read the real one from
  `lcl urls`)
- **Cases** — 13 (3 verified, 0 unit only, 10 not verified)
- **Also see** — [merchant](../../merchant/merchant-service/qa/merchant-qa.md) (which answers
  `lookup-by-domain` and `ask-for-tls`), [landing-ui](../../landing-ui/qa/landing-ui-qa.md) (what it falls
  through to), [gateway](../../../store-core/gateway/gateway-service/qa/gateway-qa.md) (the *seller* edge — a
  different gateway entirely)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins and the seeded ids are in
[`references/qa-testing.md`](../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5. Only
what is specific to the edge is below.

The four demo storefronts are `org1-store1.`, `org1-store2.`, `org2-store1.`, `org2-store2.` on
`spg-507f1f77.gateway.com`, all in `/etc/hosts` via `configure-domain.sh`. The `.asrevo.com` custom domains are
seeded in merchant's routing table but **do not resolve locally** — drive those through the API, not the
browser.

```bash
curl -sI http://org1-store1.spg-507f1f77.gateway.com/                 # the storefront
curl -s  http://org1-store1.spg-507f1f77.gateway.com/catalog/api/v1/category-hierarchy?store=<id>
docker logs lcl-default-spg-1 --tail 50                               # Caddy's own log
docker exec lcl-default-spg-1 caddy validate --config /etc/caddy/Caddyfile
```

On a shifted stack every one of those ports moves; `lcl ports --stack <name>` prints the map, and the
`Caddyfile` picks them up through `{$LCL_PORT_*}`.

> **Always reach a storefront through the spg host.** Hitting landing-ui's own port directly makes
> `FALLBACK_STORE_ID` answer for every hostname, so every store looks like the same store — an easy hour to
> lose.

---

## RTE — The proxied prefixes

Seven `handle_path` blocks strip their prefix and proxy to the service; everything else falls through to
landing-ui. `/cua*` is the one exception — it uses `handle` (not `handle_path`, so the prefix is **kept**) and
adds `X-Forwarded-Prefix: /cua`, because an authorization server has to build absolute URLs.

### RTE-01 — Every prefix reaches its service · critical · [not verified]

- **Steps** — through `http://org1-store1.spg-507f1f77.gateway.com`, request one path under each of
  `/catalog`, `/inventory`, `/content`, `/merchant`, `/checkout`, `/payment`, and `/cua`.
- **Expect** — each reaches its service (a typed 4xx is a reach; landing-ui's HTML is not). Note the pod path
  takes `?store=<id>` and **no** `pod` parameter — that predicate belongs to the *platform* gateway.

### RTE-02 — `/cua` keeps its prefix, and the others lose theirs · critical · [not verified]

- **Steps** — compare what catalog logs as its request path with what cua logs.
- **Expect** — catalog sees `/api/v1/...` (prefix stripped by `handle_path`); cua sees `/cua/...` plus
  `X-Forwarded-Prefix: /cua`. Getting this backwards breaks the shopper login redirect and nothing else, which
  is why it is easy to miss.

### RTE-03 — Anything unmatched is the storefront · critical · [not verified]

- **Steps** — request `/`, a product page and an unknown path.
- **Expect** — landing-ui renders all three (the unknown one as its 404 page — except on the Next **dev**
  server, which 500s instead; see landing-ui 99).

### RTE-04 — Merchant routes remain on merchant-service · critical · [verified]

_Was ROUTE-03 in `qa/split-merchant-content-services.md`; renumbered so it does not read as a typo for RTE-03._

- **Steps** — request the merchant store read through the edge and confirm which service answers.
- **Expect** — merchant-service answers; the content prefixes go to content-service.

---

## DOM — Hostname → store

`domain_lookup` asks merchant on every request (cached for `DOMAIN_LOOKUP_TTL`, default 5m) which store a
hostname belongs to, and `on_demand_tls.ask` asks whether a certificate should be issued for it at all.

### DOM-01 — A storefront hostname resolves to its store · critical · [not verified]

- **Steps** — `curl -sL http://org1-store1.spg-507f1f77.gateway.com/`.
- **Expect** — **200** carrying *that* store's title and catalogue, not another store's. Repeat for
  org1-store2: the two pages must differ.
- **Partly seen** — [`qa/lcl-qa.md`](../../../qa/lcl-qa.md) case 02 records
  `curl -sL http://org1-store1.spg-507f1f77.gateway.com/` → 200 on a running stack, which is the first half
  of this case. The second half — that org1-store2 renders a *different* page — has never been run.
- **Also touches** — merchant's `lookup-by-domain`, asserted from its side in
  [merchant-qa.md](../../merchant/merchant-service/qa/merchant-qa.md) DOM-01. Merchant's log line
  `header lookup:` says exactly what hostname the edge asked about, which is usually the whole answer.

### DOM-02 — An unknown hostname resolves to nothing, harmlessly · critical · [not verified]

- **Steps** — request the edge with a `Host` header no store owns.
- **Expect** — a clean refusal or the fallback, never another store's content and never a 500.

### DOM-03 — TLS is only offered for hostnames we actually serve · critical · [not verified]

- **Steps** — with `on_demand` TLS configured, attempt an HTTPS handshake for a hostname merchant does not
  know.
- **Expect** — the `ask` endpoint refuses and no certificate is requested. `ask-for-tls` restates whether a
  domain is **allocated**, not whether a certificate was issued — see merchant-qa.md DOM-03.
- **Local note** — the ACME CA defaults to Let's Encrypt **staging**; do not point a local stack at production.

### DEP-04 — The storefront edge with merchant down · critical · [not verified]

_From `qa/merchant-store-service.md` §DEP — kept with its original id. It is the edge's half of a merchant
outage; the other four services' halves are merchant-qa.md DEP-01…03._

- **Steps** — stop merchant and open a storefront hostname.
- **Expect** — Caddy's `lookup-by-domain` fails, so the request cannot be identified. Confirm the shopper gets
  a store-not-found or error page rather than a hanging request or another store's content. Then restart
  merchant and confirm it recovers **without** clearing anything by hand.

---

---

## HDR — Headers the edge is responsible for

### HDR-01 — `X-Forwarded-Port` is set, and the shopper redirect keeps its port · critical · [not verified]

- **Why it exists** — Caddy sends `X-Forwarded-Proto` and `X-Forwarded-Host` but never `X-Forwarded-Port`, and
  Tomcat's `RemoteIpValve` (`forward-headers-strategy: NATIVE`) strips the port out of `X-Forwarded-Host` and
  falls back to 80/443. Anything building an absolute URL from the request — cua's
  `DynamicRegisteredClientRepository` derives the shopper login `redirect_uri` that way — would drop the port.
- **Steps** — on a **shifted** stack (`--stack xxx`), start a shopper login from the storefront.
- **Expect** — `redirect_uri=http://org1-store1.spg-507f1f77.gateway.com:<spg-b>/callback`, with the port. On
  the default stack the port is 80 and the bug is invisible, which is why this must be run on a shifted stack.
- **Cross-reference** — [`qa/lcl-qa.md`](../../../qa/lcl-qa.md) case 09 records the same observation from the
  stack's side, where it is still marked not verified.

### HDR-02 — JSON is compressed; Next's HTML is not double-compressed · high · [not verified]

- **Why it exists** — Spring Boot leaves `server.compression` off, so every JSON body from the pod services
  used to reach the shopper uncompressed (a category hierarchy is ~6 KB that gzips to 1.5 KB). Caddy skips
  anything that already carries `Content-Encoding`, so landing-ui's HTML — Next gzips it itself — passes
  through untouched.
- **Steps** — request a category hierarchy and a storefront page with `Accept-Encoding: gzip, zstd`.
- **Expect** — the JSON comes back `Content-Encoding: zstd` or `gzip` and materially smaller; the HTML carries
  exactly one `Content-Encoding`.

### HDR-03 — Every response carries a trace id · [not verified]

- **Steps** — request any proxied path and read the response headers.
- **Expect** — `X-Trace-Id` and `X-Span-Id`, and the span named after the service (`catalog`, `cua`,
  `landing-ui`…). That id is what joins the request to its trace in Tempo.

---

## MIG — What the service split left at the edge

### MIG-03 — The legacy compatibility API really is gone · critical · [verified]

Phase 7 deleted `LegacyContentApi`, the Caddy `@legacy_content` alias and `store-pod/content-deprecated`.

- **Steps** — call `/api/v1/content/pages` and `/api/v1/content/boxes/header-message` directly on 8121 and
  through spg; then click through the storefront and the console watching the network panel.
- **Expect** — **404** on the legacy paths, 200 on `/api/v1/storefront/**`, and **nothing on any screen still
  calls the old paths**. The second half matters more than the first.

### MIG-04 — Platform SPG requests include the pod selector · [verified]

Requests with only `store` returned 404 because the platform gateway route also predicates on `pod`. All
blocks in `content-api.http` now send `pod={{POD_ID}}`; canonical and compatibility requests then returned 200.

- _Was REG-02 in `qa/split-merchant-content-services.md`._

---

## 99 — Known gaps

**spg is not unit-testable and has no automated coverage at all.** It is a `Caddyfile`; the only check is
`caddy validate`, which catches syntax and nothing else. Every assertion here is a live request.

**Certificates are staged, not real, unless `ACME_CA_URL` says otherwise.** The default is Let's Encrypt
staging, so a browser will not trust a locally issued certificate.

**The domain lookup is cached for five minutes.** Allocating a custom domain and expecting the edge to serve it
immediately will disappoint; wait out `DOMAIN_LOOKUP_TTL` or restart the container.

**`FALLBACK_STORE_ID` makes every host look like the same store** when landing-ui is reached directly rather
than through the edge. That is a local convenience, and the cause of most "why is this the wrong store" reports.

**The legacy `@legacy_content` alias is gone** (MIG-03). `qa/split-merchant-content-services.md` ROUTE-01, -02,
-04 and -05 asserted the opposite — that the old `/spg/merchant/api/v1/content/**` path still answered — and are
superseded rather than lost.

---

Raise anything unexpected against the spg PR. Include the exact `Host` header, the path, and
`docker logs lcl-<stack>-spg-1` around the request — plus merchant's `header lookup:` / `tls ask:` lines, which
say what hostname the edge actually asked about.
