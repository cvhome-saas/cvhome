# Reaching a service: ports, gateways, and local domains

**Every service has two addresses: its own port, and a path behind its gateway.** The port is the direct,
in-cluster address; the gateway path is the only address that exists from outside the service's namespace.
Which one you use is not a style choice — `common-config.yml` records it per service in
`gateway-service-name`, and `ServiceUrlBuilder` picks between them at runtime (`service-to-service.md`).

```
merchant-service, two ways to reach the same endpoint:

  direct   http://merchant.gateway.com:8120/api/v1/store/...        ← inside the pod namespace
  gateway  http://spg-507f1f77.gateway.com/merchant/api/v1/store/…  ← from anywhere else
```

The `/merchant` prefix is stripped by Caddy, so the service itself sees the identical path either way.

## The two edges

| Edge | Service | Port | Local domain | Fronts |
|---|---|---|---|---|
| Platform | `store-core/gateway` (Spring Cloud Gateway) | 8000 | `gateway.com` | `tenancy`, `seller-ui`, and **all pods** via `/spg/**` |
| Pod | `store-pod/spg` (Caddy) | 80 / 443 | `spg-507f1f77.gateway.com` | `merchant`, `catalog`, `checkout`, `payment`, `cua`, `landing-ui` |

### `spg` — path → pod service (`store-pod/spg/Caddyfile`)

| Path | Target | Prefix |
|---|---|---|
| `/merchant*` | `http://merchant.{$NAMESPACE}:8120` | stripped (`handle_path`) |
| `/catalog*` | `http://catalog.{$NAMESPACE}:8122` | stripped |
| `/checkout*` | `http://checkout.{$NAMESPACE}:8123` | stripped |
| `/payment*` | `http://payment.{$NAMESPACE}:8125` | stripped |
| `/cua*` | `http://cua.{$NAMESPACE}:8124` | **kept**, plus `X-Forwarded-Prefix: /cua` |
| everything else | `http://landing-ui.{$NAMESPACE}:8110` | after `domain_lookup` injects the store headers |

`{$NAMESPACE}` is `gateway.com` locally and `store-pod-<id>.cvhome.lcl` on AWS — the routing table itself never
changes between environments. `cua` keeps its prefix because OAuth2 issuer and redirect URIs must match the
externally visible URL; that same `…/cua` string is what appears in the pod's `issuer-uri-set`
(`authentication.md`).

### `store-core-gateway` — path → platform service

| Path | Target |
|---|---|
| `/tenancy/**` | `lb://tenancy` (`StripPrefix=1`, token relay) |
| `/spg/**?store=<id>&pod=<podId>` | the matching pod's `spg`, route built at runtime by `PodClient` |
| anything else on `gateway.com` / `www.` / `seller-ui.` | `lb://seller-ui` |

`uaa` (8001) is reached on its own host, `uaa.gateway.com:8001`, not through a gateway path.

So a seller editing a product traverses **two** gateways:
`gateway.com:8000/spg/catalog/api/v1/products?store=…&pod=…` → strip `/spg` → pod's Caddy → strip `/catalog` →
`catalog:8122`. See `multi-tenancy.md` for how that pod route is discovered.

## Local setup

Locally the Java services run on the host and only infrastructure runs in Docker
(`docker-compose-lcl.yml`: postgres, `spg`, otel-collector, loki, tempo, prometheus, grafana).

`spg` is the piece that must resolve service hostnames, so the compose file maps each one back to the host:

```yaml
spg:
  ports: ["80:80", "443:443", "2019:2019"]
  volumes: ["./store-pod/spg/Caddyfile:/etc/caddy/Caddyfile"]   # the real Caddyfile, mounted
  extra_hosts:
    - "merchant.gateway.com:host-gateway"      # …and catalog, checkout, cua, payment,
    - "spg-507f1f77.gateway.com:host-gateway"  #    landing-ui, host.docker.internal
  environment:
    NAMESPACE: gateway.com
    ASK_TLS_URL:       http://spg-507f1f77.gateway.com:80/merchant/api/v1/router/public/ask-for-tls
    DOMAIN_LOOKUP_URL: http://spg-507f1f77.gateway.com:80/merchant/api/v1/router/public/lookup-by-domain
```

Note both Caddy hooks point back **through spg itself** at `/merchant/...` — the container calls the gateway
path, not `merchant:8120` directly.

### `extra/scripts/configure-domain.sh`

Because everything is addressed by hostname rather than `localhost`, local dev needs those names in DNS. The
script appends `127.0.0.1` entries to `/etc/hosts` (idempotent — it greps before appending) and must be run
with `sudo`:

```bash
sudo ./extra/scripts/configure-domain.sh
```

It registers three groups:

| Group | Entries |
|---|---|
| Platform | `gateway.com`, `www.gateway.com`, `uaa.gateway.com`, `seller-ui.gateway.com` |
| Pod | `spg-507f1f77.gateway.com`, plus `merchant/catalog/checkout/cua/payment/landing-ui.gateway.com` |
| Tenant storefronts | `org1-store1`, `org1-store2`, `org2-store1`, `org2-store2` `.spg-507f1f77.gateway.com` |

The tenant entries are the shopper path end to end: the browser hits
`org1-store1.spg-507f1f77.gateway.com` → Caddy's fall-through route → `domain_lookup` asks `merchant-service`
which store owns that domain → `Store-Id`/`Theme` headers → `landing-ui:8110` renders that tenant
(`multi-tenancy.md`, `landing-ui.md`).

**Adding a store or pod locally means adding a hosts entry here too** — nothing resolves the new domain
otherwise. `507f1f77` is the shortened local `PodId` from `store-pod-lcl-config.yml`; a second local pod would
need its own `spg-<short>.gateway.com` name.

## Which address to use

| Situation | Address |
|---|---|
| Service → service, same namespace | `lb://<service>` — let `RestClientBuilder` resolve it |
| Service → service, different namespace | also `RestClientBuilder`; it emits `lb://spg.<ns>/<service>` for you |
| Manual curl / Postman against a pod service | `http://spg-507f1f77.gateway.com/<service>/...` (gateway path) |
| Debugging one service in isolation | `http://localhost:<port>/...` — bypasses tracing headers and, for `cua`, the `/cua` prefix |
| Browser, seller console | `http://gateway.com:8000` |
| Browser, storefront | `http://org1-store1.spg-507f1f77.gateway.com` |

Never hardcode either form in code. Ports and domains live in `common-config.yml`; the routing lives in the
`Caddyfile` and `GatewayRouteLocatorImpl`/`PodClient`.

## Related

- `configuration.md` — `common-config.yml` service registry, `namespace`, `gateway-service-name`
- `service-to-service.md` — `ServiceUrlBuilder`'s same-namespace vs cross-namespace decision
- `multi-tenancy.md` — dynamic `/spg/**` pod routes, `ask-for-tls` and `lookup-by-domain`
- `store-pod.md` — the `spg` module itself
- `build-system.md` — running the local stack
