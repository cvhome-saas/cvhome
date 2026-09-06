# cvhome observability: SLI/SLO plan, provisioned Grafana dashboards, and the issues found on the way

Implementation plan for PR `feat/observability`; the delivered files are listed in `extra/monitoring/docs/README.md`.

## Context

`cvhome` already ships a full local observability stack (`docker-compose-lcl.yml`, `extra/monitoring/*.yml`):
otel-collector-contrib 0.150.1 with `spanmetrics` + `servicegraph` connectors, Loki 3.6, Tempo 2.10, Prometheus 3.11
(remote-write receiver on, used by k6), Grafana 12.4 anonymous-admin. All 12 Spring Boot services carry the OTel
Spring Boot starter 2.27.0 + Micrometer; `landing-ui` runs the OTel Node SDK; `spg` (Caddy) emits per-route spans.
`load-testing` streams every k6 sample into the same Prometheus with `testid/layer/profile/name/store` labels and keeps
the only SLO numbers (`k6/config/thresholds.js`).

What is missing: **no Grafana dashboard exists** (only datasources are provisioned), no SLI/SLO definition on the
application side, no recording/alert rules, no Loki→Tempo link, and several signal defects found during discovery
(below). Goal: a monitoring plan and 12 provisioned dashboards (JSON in-repo, loaded when Grafana starts) that detect
errors (400/401/403/404/409/422/429/5xx), latency, service-to-service failures, SQL latency, saturation
(pool/threads/GC/CPU), and correlate traces → logs → metrics, so k6 load runs show where the system breaks.

## Phase 0 — discovery, DONE on the live stack (2026-09-06, `OTEL_SDK_DISABLED=false lcl start -d --infra all`)

Verified signal inventory at `localhost:8889/metrics`, Loki, Tempo. Dashboards must use these names.

| family | live name(s) & labels | verdict |
|---|---|---|
| HTTP server (Micrometer via OTel bridge) | `http_server_requests_seconds_{count,sum,bucket}` scope `io.opentelemetry.micrometer-1.5`; labels `service_name, application, method, uri (templated), status, outcome, exception, error` | primary per-URI signal, but **`_bucket` has only `le="+Inf"`** (no p95) and is **duplicated** as `http_server_requests_milliseconds_*` (Micrometer OTLP registry, scope `""`) |
| HTTP server (OTel native) | `http_server_request_duration_seconds_*` scope `spring-webmvc-6.0`; labels `http_request_method, http_response_status_code`; buckets 5ms…10s | **no `http_route`** → not usable per URI |
| HTTP client (OTel native) | `http_client_request_duration_seconds_*` scope `spring-webflux-5.3`; labels `server_address, server_port, http_response_status_code, error_type` | secondary; port not service |
| Span metrics | `traces_span_metrics_calls_total`, `traces_span_metrics_duration_milliseconds_{bucket,count,sum}`; labels `service_name, span_kind, span_name, status_code`; buckets 2ms…15s | **every HTTP request yields two SERVER spans** (`GET /api/v2/products/search` from OTel webmvc, `http get /api/v2/products/search` from Micrometer Tracing); scheduled tasks doubled too; no route/status/db dimensions |
| JDBC spans | CLIENT spans `SELECT catalog.product`, `INSERT checkout.sales_order`, `UPDATE catalog.outbox_instance`, DDL; attrs (old semconv) `db.system, db.operation, db.name, db.sql.table, db.statement` | SQL latency per statement available via spanmetrics |
| Service graph | `traces_service_graph_request_total`, `_failed_total`, `_{server,client}_seconds_*`; labels `client, server, connection_type ("" s2s / "database" server="cvhome" / "virtual_node"), failed`; buckets 2ms…15s | works; edges seen: catalog→merchant, inventory→checkout (`failed="true"`), gateway→billing/pod-registry, landing-ui→spg, every service→DB |
| JVM (semconv, scope `runtime-telemetry-java8`) | `jvm_memory_{used,committed,limit,used_after_last_gc}_bytes{jvm_memory_pool_name,jvm_memory_type}`, `jvm_gc_duration_seconds_*{jvm_gc_name,jvm_gc_action}`, `jvm_thread_count`, `jvm_cpu_recent_utilization_ratio`, `jvm_cpu_time_seconds_total`, `jvm_class_*` | present (the `otel.instrumentation.runtime-metrics.enabled: false` key is ineffective) |
| Process/system (Micrometer) | `process_cpu_usage`, `system_cpu_usage`, `system_load_average_1m`, `process_files_{open,max}`, `process_uptime_seconds` | duplicated across both scopes |
| Pool | `hikaricp_connections{,_active,_idle,_pending,_max,_min}`, `hikaricp_connections_timeout_total`, `hikaricp_connections_{acquire,usage,creation}_seconds_*` (`pool="HikariPool-1"`), `jdbc_connections_*` | fine; join on `service_name` |
| Tomcat / executor | absent | dropped by `filter/drop_metrics` |
| Resource labels | `service_name`, `service_instance_id`, `host_name`, `host_arch`, **`service_version="${version}"` literal** | `${version}` never resolves |
| landing-ui spans | SERVER `GET /[locale]`; CLIENT `fetch GET http://spg-…/catalog/api/v2/products/search?store=…&lang=…` | **full URL incl. query in span name** → spanmetrics cardinality explosion (already the bulk of series) |
| Loki | stream labels `service_name`, `service_instance_id`; structured metadata `severity_text`, `detected_level`, `trace_id`, `span_id`, `scope_name`; plain text lines; 12 Java services, not landing-ui/spg | DEBUG on three broad packages |
| Error → trace join | ProblemDetail `traceId` **equals** the log line trace id and Loki `trace_id` (verified with a 404) | bare **401 from the JWT filter has no body** (`BearerTokenAuthenticationEntryPoint`), recorded as `status="401", uri="UNKNOWN"` |
| Gateway | `ObservationThreadLocalAccessor` scope-leak WARN every 5s (`PodClient.refreshRoutes`, `StoreBillingStatusClient.refresh`) | real bug |
| k6 | `k6_http_reqs_total`, `k6_http_req_duration_{p95,p99,avg,max,min}` (seconds), `k6_http_req_failed_rate`, `k6_journey_duration_ms_p95`, `k6_journey_errors_rate`, `k6_dropped_iterations_total`, `k6_vus`, `k6_orders_placed_total`, `k6_browser_web_vital_*_p75` | fine |

## Delivery: ONE PR per repo (user decision)

- **cvhome:** one worktree `git worktree add .claude/worktrees/feat-observability -b feat/observability origin/main`,
  one PR `feat: observability — SLO rules, provisioned Grafana dashboards, telemetry fixes, monitoring docs`, carrying
  all bug fixes I1–I9 **and** the full implementation (pipeline, rules, dashboards, custom meters, docs). Commits stay
  one-per-item (I1, I2, …, Phase 1, Phase 2, …) so the PR reads as a sequence and any item can be reverted alone.
  Shipped with `/go`; body per `.github/PULL_REQUEST_TEMPLATE.md` (Why → What → Not obvious → Deviations →
  Verification, untouched checklist sections deleted); labels `type/enhancement` + `warn/behavior-change` (I4, I8);
  plan copied to `.agents/plans/observability-dashboards.md`; gates: `checkstyleMain checkstyleTest`,
  `build -x test -x check`, touched modules `:test`, landing-ui root `npm run build && npm run lint`, stack QA.
- **load-testing:** one PR `feat: grafana annotations, dashboard target, monitoring docs link` (Phase 6).

### Issues found (fixed inside the single PR, one commit each)

| # | commit | label | fix | files | verification |
|---|---|---|---|---|---|
| I1 | `fix/metrics-double-export` | type/bug | Micrometer metrics exported twice (OTLP registry + OTel bridge). Keep the bridge (same resource as traces/logs, seconds, honours `otel.sdk.disabled`); set `management.otlp.metrics.export.enabled: false` | `store-commons/autoconfigure/src/main/resources/common-config.yml` | `curl :8889/metrics \| grep -c http_server_requests_milliseconds` → 0 |
| I2 | `fix/http-latency-histograms` | type/bug | No latency buckets → no p95. Add `management.metrics.distribution.slo.http.server.requests: 50ms,100ms,250ms,400ms,500ms,600ms,800ms,1s,2s,2.5s,3s,5s,10s` and `http.client.requests: 50ms,100ms,250ms,500ms,1s,2s,5s` (SLO-aligned explicit buckets, `percentiles-histogram: false`) | same file | `_bucket` shows 13 `le` values + `+Inf`; if the bridge ignores `slo`, fall back to flipping I1 to the OTLP registry (one `sed` `_seconds`→`_milliseconds` in rules/dashboards) |
| I3 | `fix/service-version-placeholder` | type/bug | `${version}` literal on every series/log. `processResources` in `store-commons/autoconfigure/build.gradle` filters `common-config.yml` with `ReplaceTokens` (`@version@` ← `project.version`; not `expand()`, which would eat the other `${…}`); use `@version@` in `management.metrics.tags`, `management.observations.key-values`, `otel.resource.attributes` | `store-commons/autoconfigure/build.gradle`, `common-config.yml` | `service_version="1.0.16"` at the collector and in Loki |
| I4 | `fix/duplicate-server-spans` | type/bug, warn/behavior-change | Two server spans per request. **Recommended: `management.tracing.enabled: false`** (Boot's Micrometer Tracing off; OTel starter keeps semconv spans with `http.route`, `http.response.status_code`, `db.*`; metrics unaffected — they come from `DefaultMeterObservationHandler`). Follow-ups in the same PR: `ProblemDetailFactory.traceId()` reads MDC `trace_id` then `traceId`; `logging.pattern.correlation: "[%X{trace_id}-%X{span_id}] "`; `ProblemDetailFactoryTest` updated. Alternative B (no app change): collector `filter/spans` dropping `span.name matches "^(http\|task) .*"` | `common-config.yml`, `store-commons/autoconfigure/.../errors/web/ProblemDetailFactory.java` (+ test) | repeat the 404 test: body `traceId` == log line id == Loki `trace_id`; spanmetrics shows one SERVER span per route; `traces_span_metrics_calls_total{span_name=~"^http .*"}` absent |
| I5 | `fix/gateway-observation-scope-leak` | type/bug | Scope-leak WARN every 5s on `scheduling-1`: the `@Scheduled` refreshers block a reactive `@HttpExchange` client while automatic context propagation opens an Observation scope on the scheduler thread. Wrap the blocking call in `ContextSnapshotFactory…captureAll().wrap(...)` / run the refresh reactively | `store-core/gateway/gateway-service/src/main/java/com/asrevo/cvhome/gateway/client/PodClient.java`, `.../StoreBillingStatusClient.java` (+ test asserting no WARN) | `lcl logs store-core-gateway \| grep -c ObservationThreadLocalAccessor` → 0 after 1 min |
| I6 | `fix/landing-ui-span-names` | type/bug | Full URL + query string as CLIENT span name. `requestHook` for `@opentelemetry/instrumentation-undici` and `startOutgoingSpanHook`/`applyCustomAttributesOnSpan` for `instrumentation-http` → `GET /catalog/api/v2/products/search` (24-hex ids → `{id}`); keep `url.full` as attribute. Belt-and-braces: collector `transform/span_names` (in Phase 1) | `store-pod/landing-ui/storefront/src/shell/telemetry.ts` | root `npm run build && npm run lint`; `count(count by (span_name)(traces_span_metrics_calls_total{service_name="landing-ui"}))` < 50 |
| I7 | `fix/otel-runtime-metrics-keys` | type/chore | `otel.instrumentation.runtime-metrics.enabled` / `jvm-metrics.enabled` keys do nothing (JVM metrics arrive anyway). Remove the dead keys, keep one comment saying JVM comes from `runtime-telemetry-java8` and `management.metrics.enable.jvm: false` avoids the Micrometer duplicate | `common-config.yml` | JVM series unchanged after restart |
| I8 | `chore/fargate-log-levels` | type/chore, warn/behavior-change | DEBUG for `com.asrevo`, `org.springframework.web`, `org.springframework.security` in every profile. Move DEBUG to `lcl-config.yml`, set INFO/WARN/WARN in `fargate-config.yml` | `store-commons/autoconfigure/src/main/resources/{common,lcl,fargate}-config.yml` | log volume per service in Loki drops under a `fargate`-profile smoke |
| I9 | `chore/otel-bom-from-catalog` | type/chore | 11 of 12 services hardcode `2.27.0` in `mavenBom "io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.27.0"` (repo rule: versions only in `libs.versions.toml`); tenancy already uses `libs.versions.opentelemetryInstrumentationBom.get()` | the 11 `*-service/build.gradle` + `store-core/uaa/build.gradle`, `store-pod/cua/build.gradle` | `grep -rn '2.27.0' --include=build.gradle` → 0 |

## Design

### Signal model: traces → logs → metrics
Traces (Tempo) are the truth for one request: spg span → landing-ui → service server span → s2s client span → peer →
JDBC spans → outbox task. Logs (Loki) join on `trace_id`. Metrics (Prometheus) are the aggregates (Micrometer HTTP /
Hikari / JVM / Tomcat + collector `spanmetrics` / `servicegraph` + k6); dashboards live here and link out.

### Pipeline changes (cvhome, Phase 1)

`extra/monitoring/logging-otel-collector-config.yml`:
- `spanmetrics`: `histogram.unit: s`, explicit buckets `[5ms,10ms,25ms,50ms,100ms,250ms,400ms,500ms,600ms,800ms,1s,2s,2.5s,3s,5s,10s]`; `dimensions`: `http.request.method, http.route, http.response.status_code, error.type, db.system, db.operation, db.sql.table, server.address`; `exemplars.enabled: true`; `resource_metrics_key_attributes: [service.name]` (no per-instance series); `metrics_flush_interval: 15s`; `metrics_expiration: 10m`.
- `servicegraph`: `latency_histogram_buckets [10ms,50ms,100ms,250ms,500ms,800ms,1s,2s,5s,10s]`, `dimensions: [http.response.status_code]`, `store.ttl: 5s`, `database_name_attribute: db.name`.
- `resource/trim` processor: delete `host.arch`, `host.name`, `os.type` (local only, open decision).
- `transform/span_names` (OTTL, CLIENT spans): strip `\?.*$`, `^fetch (\w+) https?://[^/]+` → `$$1`, 24-hex / numeric path ids → `{id}`.
- `filter/drop_metrics`: stop dropping `^tomcat.*` and `^executor.*`; keep the rest.
- `prometheus` exporter: `enable_open_metrics: true` (exemplars).
- Java side for Tomcat metrics: `server.tomcat.mbeanregistry.enabled: true` in `common-config.yml`.
- Hikari pool naming: `spring.datasource.hikari.pool-name: ${spring.application.name}`.

`extra/monitoring/prometheus.yml`: `evaluation_interval: 15s`, `rule_files: [/etc/prometheus/rules/*.yml]`,
`metric_relabel_configs` labeldrop `otel_scope_schema_url|otel_scope_version`. Compose: `--enable-feature=exemplar-storage`,
volume `./extra/monitoring/prometheus-rules:/etc/prometheus/rules:ro`.

`extra/monitoring/grafana-datasources.yml`: Prometheus `exemplarTraceIdDestinations` (`trace_id` → tempo); Loki
`derivedFields` (`matcherType: label`, `matcherRegex: trace_id`, url `$${__value.raw}`, datasource tempo); Tempo
`tracesToLogsV2` with `tags: [{key: service.name, value: service_name}]` and ±1m shift, `tracesToMetrics` (rate,
error ratio, p95 over `traces_span_metrics_*` with `$$__tags`), `nodeGraph.enabled`, `serviceMap`, `lokiSearch`.

Grafana provisioning: `extra/monitoring/grafana-dashboards.yml` (provider `cvhome`, `type: file`,
`foldersFromFilesStructure: true`, `allowUiUpdates: false`, `updateIntervalSeconds: 10`, path
`/var/lib/grafana/dashboards`); dashboards under `extra/monitoring/grafana/dashboards/<folder>/<name>.json`; compose
mounts both, `GF_DASHBOARDS_DEFAULT_HOME_DASHBOARD_PATH=/var/lib/grafana/dashboards/platform/platform-overview.json`,
`GF_FEATURE_TOGGLES_ENABLE=traceqlEditor`. Every JSON: `"id": null`, fixed `uid`, `"editable": false`,
datasources as `{"type":"prometheus","uid":"prometheus"}` (never `${DS_*}` inputs), `schemaVersion` 41, tag `cvhome`.
`lcl.yml compose.default` stays `[postgres, minio, spg]`; monitoring remains `--infra all`.

### SLI / SLO catalogue → recording rules (`extra/monitoring/prometheus-rules/cvhome-recording.yml`)
All HTTP selectors carry `uri!~"/actuator.*|UNKNOWN|root"`; base label `service_name`.

| SLI | SLO (local baseline, mirrors `thresholds.js`) | rule | PromQL |
|---|---|---|---|
| Request rate | — | `cvhome:http_server_requests:rate5m` | `sum by (service_name, method, uri, status) (rate(http_server_requests_seconds_count{…}[5m]))` |
| Availability (5xx) | 99.9 % | `cvhome:http_server_errors:ratio_rate5m` | 5xx rate / all rate, by `service_name` |
| Client-error ratio (4xx excl. 401/403) | tracked | `cvhome:http_server_4xx:ratio_rate5m` | `status=~"4..",status!~"401\|403"` |
| Auth rejections | tracked | `cvhome:http_server_auth_rejections:rate5m` | `sum by (service_name, status) (rate(…{status=~"401\|403"}[5m]))` |
| Latency p95 per route | product 500ms · search 800 · category 600 · availability 400 · layout 500 · cart 800 · checkout 2s · admin GET 1s / POST,PUT 2.5s · login 2.5s | `cvhome:http_server_requests:p95_5m` | `histogram_quantile(0.95, sum by (service_name, uri, method, le) (rate(http_server_requests_seconds_bucket{…}[5m])))` |
| Latency p95 per service (spans, covers spg/landing-ui) | same | `cvhome:span_server:p95_5m` | over `traces_span_metrics_duration_seconds_bucket{span_kind="SPAN_KIND_SERVER"}` |
| Storefront latency good-ratio (GET < 500ms, catalog/content/merchant/inventory) | 95 % | `cvhome:slo_latency_storefront:good_ratio_rate5m` | `bucket{le="0.5"} / count` |
| Checkout latency good-ratio (POST/PUT < 2s, checkout/payment) | 95 % | `cvhome:slo_latency_checkout:good_ratio_rate5m` | `bucket{le="2"} / count` |
| SQL p95 per statement | p95 < 100ms | `cvhome:sql:p95_5m` | `…{span_kind="SPAN_KIND_CLIENT",db_system="postgresql"}` by `span_name` |
| SQL rate | — | `cvhome:sql:rate5m` | by `db_operation, db_sql_table` |
| Queries per request (N+1) | < 10 | `cvhome:sql_per_request:ratio5m` | sql rate / server-span rate |
| s2s failure ratio per edge | < 0.5 % | `cvhome:s2s_failed:ratio_rate5m` | `traces_service_graph_request_failed_total{connection_type=""} / request_total` by `client, server` |
| s2s p95 per edge | 500ms | `cvhome:s2s:p95_5m` | over `traces_service_graph_request_client_seconds_bucket` |
| HTTP client errors | — | `cvhome:http_client_errors:rate5m` | `http_client_request_duration_seconds_count{http_response_status_code=~"4..\|5.."}` by `server_address` |
| Pool utilisation / pending / timeouts / acquire p95 | < 80 % / 0 / 0 / 10ms | `cvhome:hikari_pool:utilisation`, `cvhome:hikari_pending:max`, `cvhome:hikari_timeouts:rate5m`, `cvhome:hikari_acquire:p95_5m` | `hikaricp_connections_*` |
| Tomcat thread saturation | < 80 % | `cvhome:tomcat_threads:utilisation` | `tomcat_threads_busy_threads / tomcat_threads_config_max_threads` |
| GC pause share / heap after GC / CPU | < 5 % / < 80 % / < 80 % | `cvhome:jvm_gc_pause:ratio5m`, `cvhome:jvm_heap_after_gc:ratio`, `cvhome:jvm_cpu:ratio` | `jvm_gc_duration_seconds_sum{jvm_gc_action=~".*pause.*"}`, `jvm_memory_used_after_last_gc_bytes / jvm_memory_limit_bytes{jvm_memory_type="heap"}`, `jvm_cpu_recent_utilization_ratio` |
| Burn rates (5m/30m/1h/6h) | availability 99.9 %, latency 95 % | `cvhome:slo_availability:burn_rate<W>`, `cvhome:slo_latency_storefront:burn_rate<W>` | error ratio / 0.001; (1 − good ratio) / 0.05 |

Alerts (`cvhome-alerts.yml`, surfaced via `ALERTS` on the overview; Grafana alerting is an open decision):
`CvhomeAvailabilityBurnFast` (1h > 14.4 and 5m > 14.4), `CvhomeAvailabilityBurnSlow` (6h > 6 and 30m > 6),
`CvhomeLatencyStorefrontSlo` (< 0.95 for 5m), `CvhomeS2sEdgeFailing` (> 5 % for 2m), `CvhomeHikariPending`
(pending > 0 for 1m or timeouts > 0), `CvhomeTomcatSaturated` (> 80 % for 2m), `CvhomeGcPressure` (> 5 % or heap-after-GC
> 85 %), `CvhomeOutboxBacklog` (failed > 0 or oldest pending > 300s, after custom meters), `CvhomeServiceDown`
(`time() - max by (service_name)(timestamp(process_uptime_seconds)) > 90`).

### Dashboard catalogue (12 JSONs)
Common variables: `service` (`label_values(http_server_requests_seconds_count, service_name)`, multi/All), `uri`,
Loki `lsvc`, Tempo `tsvc`, k6 `testid`. Standard data links on every latency/error panel: Tempo Explore
(`{resource.service.name="${__field.labels.service_name}" && span:kind=server && status=error}`) and Loki Explore
(`{service_name="…"} | detected_level=~"error|warn"`), plus dashboard link to Service RED with `var-service`.

| uid | title (folder) | key panels (type · query) |
|---|---|---|
| `cvhome-platform-overview` | Platform Overview / SLO (platform) | availability 1h stat; burn-rate fast/slow; storefront & checkout latency SLO stats; RPS stacked by service; errors by status class; service table (rate, 5xx ratio, p95, pool util, CPU; row link → RED); service map (nodeGraph via Tempo `serviceMap`); firing alerts `ALERTS{alertstate="firing"}`; up services `count(count by (service_name)(process_uptime_seconds))` |
| `cvhome-service-red` | Service RED (platform) | RPS by uri; status-class stack; **8 stat panels 400/401/403/404/409/422/429/5xx** (`increase(...{status="400"}[$__range])`, each linking to Loki/Tempo filtered on that status); 4xx/5xx by uri+exception table; p50/p95/p99 with exemplars; latency heatmap; slowest routes `topk(15, cvhome:http_server_requests:p95_5m)`; exceptions by class; span-level errors; error logs panel; auth rejections (after custom meter) |
| `cvhome-service-to-service` | Service-to-Service (platform) | nodeGraph (Tempo serviceMap); edge table (rate, failed ratio, p95 joined on client+server, row link to TraceQL `{resource.service.name="client" && span:kind=client && span.server.address="server" && status=error}`); edge p95 over time; `http_client_request_duration_seconds` errors by `server_address`/status and `error_type`; DB edges; virtual-node/unknown peers |
| `cvhome-database-sql` | Database & SQL (data) | pool active/idle/pending/max; utilisation gauge; acquire p95; usage p95; timeouts stat; SQL rate by op/table; SQL p95 by statement (threshold 100ms); top-20 statements table (rate, p95, error; link to TraceQL `{name="…"}`); SQL heatmap; queries-per-request; N+1 suspects TraceQL `{resource.service.name=~"$service" && span.db.system="postgresql"} \| count() > 20`; runtime DDL smell table; `jdbc_connections_*` |
| `cvhome-jvm-runtime` | JVM & Runtime (platform) | heap/non-heap by pool; used-after-GC ratio; GC pause p95 and count by `jvm_gc_name`; GC share; threads; CPU (`jvm_cpu_recent_utilization_ratio`, `process_cpu_usage`, `system_cpu_usage`, load); open files; classes; uptime |
| `cvhome-bottlenecks` | Bottlenecks / Saturation (platform) | tomcat busy/current/max + utilisation; connections; executor active/queued/pool by `name`; Hikari pending + util; GC share; CPU; **saturation matrix** table per service (colour cells); rate-vs-p95 overlay (knee); `k6_vus` overlay when present; collector health (`otlp_exporter_*`, `up{job="otel-collector"}`) |
| `cvhome-logs-errors` | Logs & Errors (observability) | volume by level; errors by service; top error messages (regexp first 80 chars); exception classes (`regexp "(?P<ex>[A-Za-z.]+(Exception\|Error))"`); error-code lines (`^(?P<code>[A-Z_]{4,}) \[traceId=`); scope-leak WARN counter (must be 0 after I5); logs panel `{service_name=~"$lsvc"} \| detected_level=~"$level" \|~ "$search"` with View-trace derived field; error trace ids table |
| `cvhome-traces` | Traces (observability) | TraceQL tables: slow server spans `> 500ms` with `select(span.http.route, span.http.response.status_code)`; error traces; 5xx roots; 403 / 404; s2s failures `{span:kind=client && status=error}`; slow SQL `{span.db.system="postgresql" && duration > 100ms}`; outbox runs `{name="task outboxProcessingScheduler.process"}`; top span rates/errors from spanmetrics with links |
| `cvhome-edge` | Edge: spg + landing-ui (edge) | spg route RPS/p95/status classes from spanmetrics `service_name="spg"`; landing-ui page render p95 by `span_name`; upstream fetch p95/errors by normalised name; landing-ui→spg edge; span-name cardinality guard (red > 200); k6 Web Vitals p75 |
| `cvhome-auth` | Auth: uaa/cua/gateway (edge) | 401/403 per service; bare-401 share (`uri="UNKNOWN"`); token/authorize/login/jwks RED for uaa & cua; JWKS client fetches; 402/401 at gateway; 429 rate-limit; `cvhome_auth_rejections_total` by status/reason (after custom meter); login failure logs; gateway session gauge (after custom meter) |
| `cvhome-load-test-vs-app` | Load Test vs App (load-testing) | run header stats (VUs, reqs, failed rate, dropped iterations, orders); VUs + k6 RPS; k6 p95 by `name` vs threshold overrides from `thresholds.js`; app RPS and app p95 (`rate[30s]`) on the same time axis; k6 unexpected vs app 5xx; saturation strip (pool, tomcat, CPU, GC); SQL p95 + queries/request; s2s failure ratio; journeys p95/errors; run annotations (tag `k6`, `testid:$testid`); error logs during run |
| `cvhome-outbox-events` | Outbox & Events (data) | records by status + oldest pending age (after custom meter); processor run rate/p95/error from `span_name="task outboxProcessingScheduler.process"` (available today); failed task spans TraceQL; outbox SQL by `db_sql_table=~".*outbox.*"`; outbox logs |

### Custom meters (cvhome, Phase 4)
| change | files | shape |
|---|---|---|
| Auth rejection counter | new `store-commons/autoconfigure/.../errors/web/AuthRejectionMetrics.java`, registered in `ErrorHandlingAutoConfiguration`; `SecurityErrorHandler` increments; a shared `Customizer<OAuth2ResourceServerConfigurer>` bean sets a metrics-wrapping `AuthenticationEntryPoint`/`AccessDeniedHandler` that delegates to `BearerTokenAuthenticationEntryPoint` (bare-401 body unchanged) | `cvhome.auth.rejections{status, reason, source=advice\|filter}` → `cvhome_auth_rejections_total` |
| Caffeine stats | `store-pod/{catalog,checkout,payment}/*/config/CacheConfig.java` | `recordStats()` → Boot `CacheMetricsAutoConfiguration` → `cache_gets_total{result}`, `cache_size`, `cache_evictions_total`; panel in Bottlenecks |
| Outbox gauge | `MeterBinder` in autoconfigure (JPA pods) + tenancy (JDBC), `@Scheduled` refresh 15s | `cvhome.outbox.records{status}`, `cvhome.outbox.oldest_pending.seconds` |
| Gateway session gauge | gateway in-memory `WebSessionStore` | `cvhome.gateway.sessions` |

### Documentation set — provider-neutral, for non-experts (Phase 5, `extra/monitoring/docs/`)

The dashboards JSON is the Grafana rendering; **the docs are the source of truth** so the same monitoring can be
rebuilt on CloudWatch, Datadog, or any other provider. Every KPI and every panel is described once, in plain language,
with its data source, its query, and how to port it. Markdown in-repo (reviewable in the PR).

| file | audience | content |
|---|---|---|
| `README.md` | everyone | what monitoring is here, how to start it (`--infra all`, `OTEL_SDK_DISABLED=false`), the three signals in one paragraph each, where to click when "something is slow" / "something is failing", links to the other docs |
| `concepts.md` | non-expert | plain-language glossary: metric, log, trace, span, label, histogram/percentile (why p95 not average), SLI/SLO/error budget/burn rate, RED/USE, saturation, N+1, connection pool, GC. One short example per term, taken from cvhome |
| `signals.md` | engineer | the signal inventory (the Phase 0 table): every metric family, its labels, source (Micrometer / OTel / connector / k6), export path, and which switch turns it on/off. Provider-neutral naming column (OTel semconv name) beside the Prometheus name |
| `kpis.md` | everyone | **one entry per KPI/SLI** (≈ 30): *What it means* (one sentence a manager understands) · *Why it matters* (what breaks when it moves) · *How it is measured* (source signal, labels, window) · *Query* (PromQL, plus the recording-rule name) · *Target / thresholds* (green/amber/red, and where the number comes from — k6 `thresholds.js`) · *Where it is shown* (dashboard uid + panel) · *What to do when red* (first checks) · *Porting notes* (the underlying OTel metric/attribute names, so a CloudWatch/Datadog query can be written) |
| `dashboards.md` | everyone | **one section per dashboard, one row per panel**: title, question the panel answers, type, query (PromQL / LogQL / TraceQL), unit, thresholds, links. Generated from the JSON by `extra/monitoring/scripts/dashboard-docs.mjs` (reads each dashboard, emits the tables) so JSON and docs cannot drift; the script also validates (uid unique, `id: null`, datasource uids); CI runs it in check mode and fails on diff |
| `alerts.md` | on-call | each alert rule: condition in words, window, severity, what it usually means, runbook steps, how to silence |
| `runbooks.md` | on-call | scenario-driven: "storefront slow", "5xx spike", "checkout failing", "pool exhausted", "service-to-service failing", "auth failures", "SQL slow / N+1", "outbox stuck", "GC pressure" — each: symptoms on which panel → drill path (metrics → traces → logs) → likely causes → fix |
| `load-testing.md` | QA / perf | how a k6 run appears on the dashboards, the `$testid` picker, annotations, reading the knee, what to record in `load-testing/docs/baseline.md` |
| `porting.md` | architect | mapping table Prometheus/Loki/Tempo → CloudWatch (AMP/Logs Insights/X-Ray, the existing `aws-otel-collector` config) and generic OTLP providers: which collector exporters change, which metric names change (`_seconds` ↔ unit-suffixed), which queries need rewriting (histogram_quantile ↔ provider percentile functions), which features have no equivalent |

`kpis.md` and `dashboards.md` are the deliverable that lets the platform be re-implemented anywhere: a reader with no
Grafana knowledge can rebuild every graph from its row.

### load-testing changes (Phase 6)
- `bin/k6run`: `grafanaUrl` in `k6/config/env/lcl.json` (`GRAFANA_URL` override, `NO_GRAFANA=1` skip); POST `/api/annotations` with tags `k6, testid:…, profile:…, layer:…` at start, PATCH `timeEnd` on EXIT trap (region); `exec` becomes a plain call.
- `Makefile`: `dash` (opens `/d/cvhome-load-test-vs-app?var-testid=$(TESTID)`), Grafana health in `preflight`.
- `docs/prometheus.md`: "Dashboards" section (uids, `$testid`, annotation behaviour, k6 `name` ↔ app `uri` map).
- `docs/baseline.md`: first baseline table per journey after Phase 7's runs.

## Phases — all in worktree `feat-observability`, branch `feat/observability`, one commit per row, one PR

| phase | commits | scope | verification |
|---|---|---|---|
| A | I1–I9 | the nine fixes (table above), in that order (I1–I4 change metric/span names the rest relies on) | per-row checks + `./gradlew checkstyleMain checkstyleTest build -x test -x check`, touched `:test`; the 404 trace-join test after I4 |
| B | pipeline | collector, `prometheus.yml`, datasources, compose mounts, tomcat mbeans, pool-name, plan copy to `.agents/plans/observability-dashboards.md` | `OTEL_SDK_DISABLED=false lcl start -d --stack observability --infra all`; `promtool check config`; collector: SLO buckets present, `tomcat_threads_busy_threads` present, no `span_name="fetch GET http…"`, one server span per route, `service_version="1.0.16"`; Loki line shows "View trace"; Tempo span shows "Related metrics" |
| C | rules | recording + alert rules, `tests/cvhome.test.yml` | `promtool check rules`, `promtool test rules`; `/api/v1/rules` all `health: ok`; `cvhome:http_server_errors:ratio_rate5m` returns 12 series |
| D | dashboards | provider + 12 JSONs + `extra/monitoring/scripts/dashboard-docs.mjs` (validate + generate `dashboards.md`) wired into the CI quality job | script passes in check mode; `/api/search?type=dash-db` → 12, each `meta.provisioned: true`; `make smoke` → Load Test vs App shows the testid; trigger 404 and 403 (`.http` request as another store's principal) → RED stats increment within 30s, table row opens a Tempo trace, Logs panel line opens Tempo |
| E | custom meters | auth-rejection counter, cache stats, outbox gauge, gateway session gauge + dependent panels | unit tests (SimpleMeterRegistry, `@JdbcTest`), `./gradlew check`; `cvhome_auth_rejections_total`, `cache_gets_total`, `cvhome_outbox_records` visible |
| F | docs | the nine files under `extra/monitoring/docs/` (table above); `qa/lcl-qa.md` case 15 extended (dashboards, buckets, version, no duplicates, no WARN, Loki→Tempo) and case 16 (SLO rules evaluate) | every KPI in `kpis.md` has a matching panel in `dashboards.md` and vice versa (checked by the script); a non-expert reviewer can follow `runbooks.md` "storefront slow" on the live stack |
| G | load-testing PR | k6run annotations, `make dash`, `docs/prometheus.md` link to `extra/monitoring/docs/`, `docs/baseline.md` after running `storefront-browse`, `shopper-guest-checkout`, `mixed-production-mix` at `PROFILE=load` and `storefront-breakpoint` | `make smoke` → annotation region visible; `make dash`; `make prom-check` green; baseline table filled with findings |

Final PR verification (in the PR body): every gate above, plus a fresh `lcl stop --hard`/`start` proving dashboards
and rules load from a clean Grafana and Prometheus with no manual step.

## Open decisions (defaults chosen; say if you want otherwise)
1. Duplicate spans: Option A `management.tracing.enabled: false` + MDC key change (chosen) vs collector filter only.
2. Micrometer path: OTel bridge (chosen); flip to OTLP registry only if `slo` buckets do not appear.
3. `postgres-exporter` under `--infra all` for `pg_stat_activity`/locks (not included; cheap add later).
4. Alerts via Prometheus rules + `ALERTS` panel (chosen) vs Grafana unified alerting provisioning.
5. Grafana/Prometheus host ports are fixed at 3000/9090 in compose; parameterise like `LCL_PORT_*` later so parallel stacks each get a Grafana.
6. Error budget excludes all 4xx (chosen); 401/403/429 tracked separately.
7. `host.name`/`service_instance_id` labels: trimmed locally, keep on Fargate.

## Note
The `default` lcl stack was started during discovery with OTel on (`lcl status` shows every service UP); leave it up
for Phase 1 QA or `lcl stop` it.
