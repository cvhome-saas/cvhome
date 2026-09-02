# `.http` request files

**Every endpoint this repo exposes has a runnable request next to the code that serves it.** Add an endpoint,
add its block; change a path, a query param, a body or a status, change the block in the same commit. The
`.http` file is the executable half of the API contract — the part a reviewer can actually run.

Format is the **IntelliJ HTTP Client** (`.http`), which IntelliJ/JetBrains IDEs run inline and
`ijhttp` runs from CI or a terminal. Not `.rest`, not a Postman collection, not a `curl` in a comment.

## Where the file goes

One file per `*Api` / `*Controller` **class**, in an `http/` folder inside that class's `-service` module:

```
store-pod/catalog/catalog-service/
  http/
    product-api.http          ← api/v1/product/ProductApi.java
    category-api.http         ← api/v1/category/CategoryApi.java
    product-price-api.http    ← api/v1/product/ProductPriceApi.java
    product-api-v2.http       ← api/v2/product/ProductApiV2.java
  src/main/java/.../api/v1/product/ProductApi.java
```

Name the file after the class, kebab-cased: `ProductApi` → `product-api.http`, `ProductApiV2` →
`product-api-v2.http`, `StoreManagerController` → `store-manager-controller.http`. One class, one file — so
"which file do I update?" is answered by the controller you just edited, and a `git log` on the controller and
on its `.http` file tell the same story.

`External*Api` controllers get one too. They are the server side of a pod's `-external-api` `@HttpExchange`
contract, and being service-to-service they are the endpoints least likely to be exercised by clicking around
a UI — which is exactly why a runnable request earns its place.

## The environment

Two files at the **repo root** — IntelliJ resolves `http-client.env.json` from the request file's own
directory upward, so one pair at the root serves every service.

| File | Committed? | Holds |
|---|---|---|
| `http-client.env.json` | yes | urls, store/org ids, `LANG` — shared, stable, no secrets |
| `http-client.private.env.json` | **no**, gitignored | session ids, anything per-developer or short-lived |

`http-client.private.env.json.example` is the template: copy it, log in to the seller console once, and paste
your session id. Session ids expire, which is why they are not in the committed file — a committed one goes
stale within the day and every request 302s to a login page.

Current keys: `SELLER_UI_URL`, `SPG_URL`, `LANDING_UI_URL`, `UAA_URL`, `STORE_ID`, `STORE_ID_2`, `ORG_ID`,
`LANG`; private: `ORG_ADMIN_SESSION_ID`, `SUPER_ADMIN_SESSION_ID`. Add a key rather than inlining a value.

## Address the endpoint the way real traffic reaches it

**Use the gateway path form, never the service's own port.** A seller request crosses store-core-gateway
*and* the pod's spg; a shopper request arrives on the store's own host. A request aimed at `localhost:8122`
skips both edges, so it silently passes even when the route, the prefix strip or the token relay is broken —
the failures a request file is most useful for catching.

| Caller | Base | Auth |
|---|---|---|
| seller console → pod service | `{{SELLER_UI_URL}}/spg/<service>/…` | `Cookie: STORE-CORE-GATEWAY-JSESSIONID={{ORG_ADMIN_SESSION_ID}}` |
| seller console → platform service | `{{SELLER_UI_URL}}/<service>/…` (e.g. `/manager/…`) | same cookie |
| storefront (public) | `{{LANDING_UI_URL}}/<service>/…` | none |
| service-to-service | `{{SPG_URL}}/<service>/…` | s2s bearer token |

Both `store` and `lang` belong on the url: `?store={{STORE_ID}}&lang={{LANG}}`. `store` is mandatory — the
argument resolver throws without it, so a request file missing it produces a confusing 500 rather than the
endpoint you meant to demonstrate.

## What a block looks like

Separate blocks with `###`, and put the intent on that line — it becomes the request's name in the IDE's
runner and its gutter icon.

```http
### create a product
# @name createProduct
POST {{SELLER_UI_URL}}/spg/catalog/api/v1/private/product?store={{STORE_ID}}&lang={{LANG}}
Cookie: STORE-CORE-GATEWAY-JSESSIONID={{ORG_ADMIN_SESSION_ID}}
Content-Type: application/json

{ "sku": "HTTP-DEMO-001" }

> {%
    client.global.set("PRODUCT_ID", response.body.id);
%}
```

- `# @name <id>` names a request so a later one can chain off it.
- `> {% … %}` is a **response handler**: `client.global.set(...)` captures an id for the next block,
  `client.test(...)` + `client.assert(...)` turn the file into something `ijhttp` can fail on.
- `< ./payload.json` sends a file as the body — use it when a realistic payload would bury the request.
- Cover the endpoint's real shape: the happy path, and the failure the endpoint was written to produce
  (a 404 for another store's row, a 422 a provider refusal maps to). A file with only 200s documents half
  the contract, and the typed-error work is exactly the half it omits.

Reference implementation: **`store-pod/catalog/catalog-service/http/product-api.http`**

`http/` has a human-facing sibling: **`<service>/qa/<module>-qa.md`**, the QA script a person runs for
that same service. `http/` is the machine-checkable path and `qa/` is the one that explains *why* a case
exists; neither replaces the other. Rules: `references/qa-testing.md` §7. — create → chain the
id → patch → cross-store 404 → delete.

## Known-stale files, do not copy

These predate the rename from Shopizer and point at `localhost:8089` with Mongo-style ids. Nothing routes
there. Treat them as deleted; do not extend them, and do not take them as the pattern:

```
store-pod/{catalog,checkout,merchant}/*-service/{store,issue,missed,products,requests}.http
store-core/tenancy/tenancy-service/{store,signup,users,requests,router}.http
store-core/uaa/req.http
store-core/gateway/gateway-service/gateway.http
```

`extra/requests/` is the older *live* tree — organized by caller (`store-ui/{common,org-admin,super-admin}/`,
`landing-ui/`) with its own `http-client.env.json` and a hyphenated `{{SELLER-UI_URL}}`. It still works and is
still fine for scratch or cross-cutting flows that belong to no single controller. New per-endpoint coverage
goes in the service's `http/` folder.

## Checklist

- [ ] New or changed endpoint has a block in `<service>/http/<api-class>.http`
- [ ] File named after the controller class, kebab-cased, one class per file
- [ ] Gateway path form, not the service port
- [ ] `?store={{STORE_ID}}&lang={{LANG}}` present
- [ ] Auth header matches the caller (seller cookie / none for public / s2s bearer)
- [ ] New url or id added to `http-client.env.json`, never inlined; nothing secret in the committed env file
- [ ] At least one non-2xx block for an endpoint with a declared failure mode
