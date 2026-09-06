# Reading a load test

How a k6 run from `../load-testing` shows up here, and how to turn it into findings.

## Before the run

- Start the stack with telemetry on: `OTEL_SDK_DISABLED=false lcl start -d --infra all` (in `cvhome`).
- From `load-testing`: `make preflight` (is everything answering, is Prometheus ready) then the script:
  `make storefront-browse PROFILE=load PEAK_VUS=50`, `make shopper-guest-checkout PROFILE=load RATE=60 DURATION=10m`,
  `make mixed-production-mix PROFILE=load`, `make storefront-breakpoint` (ramps until an SLO breaks and aborts).
- `bin/k6run` tags every sample with a `testid` (`<script>-<profile>-<utc>`), streams it to Prometheus, and posts a
  Grafana annotation at start and end so the run is a shaded region on every dashboard.

## During and after

Open **Load test vs app** (`make dash` opens it for the last `TESTID`) and pick the run in the *Test run* variable.
The page is one time axis, k6 on top, the application underneath:

| row | what to read |
|---|---|
| *The run* | VUs, requests, failed share, dropped iterations, orders, journey errors. **Dropped iterations > 0 means k6 ran out of VUs** — the load generator, not the app, was the limit; raise `PEAK_VUS`/pre-allocated VUs and rerun before drawing conclusions. |
| *Load and latency, k6 side* | The load shape and the user-visible latency per endpoint name. Compare each name against its threshold in `k6/config/thresholds.js`. The *failures by endpoint and status* panel says what failed: 429 the limiter, 402 the billing guard, 5xx the platform, 409 contention. |
| *What the application did* | Server-side rate, p95 and 5xx per service, and the service-to-service failure share. If k6 p95 is high but app p95 is low, the time is in spg / landing-ui / the network (Edge dashboard). |
| *What ran out* | Every ceiling as a share on one axis — request threads, database pool, CPU, GC — plus pool waiting, SQL cost and heap. **The first line to reach 0.8 is the bottleneck of this run.** |

**The knee.** On Bottlenecks → *Traffic vs p95*, the request rate flattens while p95 climbs: that is the capacity of
the system as configured. The saturation strip at that moment names the resource.

## What a finding looks like

Write it in `load-testing/docs/baseline.md`, one row per script and profile:

| script / profile | testid | k6 p95 (worst name) | app p95 | 5xx | first resource at 0.8 | note |
|---|---|---|---|---|---|---|
| storefront-browse / load PEAK_VUS=50 | browse-load-2026… | catalog:search 0.62 s | catalog 0.41 s | 0 | catalog pool 0.8 at 40 VUs | search p95 over its 0.5 s threshold; `SELECT catalog.product_description` p95 0.3 s |

Then the drill: Service RED (route), Database & SQL (statement, statements per request), Service-to-Service (edge),
JVM & Runtime (GC/heap during a soak). The fix goes in the application; the number goes back into
`thresholds.js` once it is met.

## Reading the profiles

- **smoke** proves contracts, not latency: every journey once. Use it to check the dashboards have data.
- **load** is the baseline: the numbers to record.
- **stress / spike** loosen the SLO multipliers (2×/3×): they answer "does it degrade gracefully" — watch for 5xx and pool timeouts, not p95.
- **soak** runs for hours: read JVM & Runtime *Heap after GC* (a rising floor is a leak), *Cache size*, gateway sessions, file descriptors.
- **breakpoint** ramps until a threshold breaks and aborts: the annotation end is the knee; the saturation strip at that moment is the bottleneck.

## Correlating names

k6 names its requests `service:endpoint`; the application sees route templates. The main pairs:

| k6 `name` | service | `uri` |
|---|---|---|
| `catalog:product` | catalog | `/api/v2/products/{slug}` |
| `catalog:search` | catalog | `/api/v2/products/search` |
| `catalog:products-by-category` | catalog | `/api/v2/products` |
| `inventory:availability` | inventory | `/api/v1/availability` |
| `content:layout` | content | `/api/v1/storefront/layout/{code}` |
| `checkout:cart-create` / `cart-update` | checkout | `/api/v1/cart`, `/api/v1/cart/{code}` |
| `checkout:checkout` | checkout | `/api/v1/cart/{code}/checkout` |
| `page:home` / `page:product` | landing-ui | span `GET /[locale]`, `GET /[locale]/product/[slug]` |
| `spg:domain-lookup` | merchant | `/api/v1/router/public/lookup-by-domain` |

(Exact templates: Service RED → *Requests / s by route* while the run is going.)
