# landing-ui: Next.js 16.0.0 → 16.3.5, React 19.2.0 → 19.2.8

A security upgrade. It changes no feature, and it ships alone with its own QA. cvhome#357 and the orchestrator plan
`landing-ui-cpu-memory.md` flagged it as urgent and kept it out of their own scope.

## Context

landing-ui pins `next` 16.0.0 and `react`/`react-dom` 19.2.0:

- `storefront/package.json`: `next`, `react`, `react-dom` and `eslint-config-next` are exact.
- The root `package.json` `overrides` force `next`, `react` and `react-dom` on every workspace.
- No lockfile is committed (`**/package-lock.json` is gitignored, and Gradle `clean` deletes it). Every build,
  CodeBuild's included, resolves fresh, so these exact pins are the only control.

osv.dev lists 37 advisories against the 16.0.x line: 3 critical, 15 high, 16 moderate, 3 low. The critical and high
ones:

| Advisory | Severity | What | Fixed on 16.x | Reachable here |
|---|---|---|---|---|
| GHSA-9qr9-h5gf-34mp (CVE-2025-55182) | critical | RCE in the RSC flight protocol | 16.0.7 | yes: every App Router page, server actions or not |
| GHSA-2xp9-vwfh-vxw4 | critical | RCE in the image optimizer (AVIF) | 16.3.3 | no: `images.unoptimized` answers 404 on `/_next/image` |
| GHSA-p293-qw3h-jr36 (CVE-2026-75604) | critical | RCE on Windows hosts | 16.3.3 | no: Linux |
| GHSA-mwv6, -5j59, -h25m, -q4gf, -8h8q | high | RSC denial of service | 16.0.9 to 16.2.5 | yes |
| GHSA-267c-6grr-h53f, -26hh-7cqf-hhc6 | high | proxy bypass via segment-prefetch routes | 16.2.5, 16.2.6 | yes: `proxy.ts` guards `/t/…` |
| GHSA-492v-c6pp-mqqv | high | proxy bypass via dynamic route parameter injection | 16.2.5 | yes |
| GHSA-6gpp-xcg3-4w24 | high | proxy bypass, Turbopack with a single `config.i18n.locales` entry | 16.2.11 | no: next-intl, no `config.i18n` |
| GHSA-36qx-fr4f-26g5 | high | proxy bypass, Pages Router with i18n | 16.2.5 | no: App Router only |
| GHSA-p9j2-gv94-2wf4 | high | SSRF in rewrites via a forged host | 16.2.11 | yes: the proxy rewrites |
| GHSA-c4j6-fc7j-m34r | high | SSRF via WebSocket upgrades, self-hosted | 16.2.5 | yes: self-hosted |
| GHSA-mg66-mrh9-m8jx, -m99w-x7hq-7vfj, -89xv-2m56-2m9x | high | Cache Components DoS, Server Actions DoS and SSRF | 16.2.5, 16.2.11 | no: none of those features used |

The last release of each line (osv.dev):

- 16.0.11: 32 open advisories.
- 16.1.7: 25 open.
- 16.2.12: 2 open, both critical.
- 16.3.3, 16.3.4 and 16.3.5: none.

The React-side CVEs (react-server-dom-*: 55182, 55183, 55184, 67779, 23864, 23869, 23870, 44907) are fixed up to
19.2.8. The App Router renders with the React build vendored inside `next`, so the fix arrives with `next`. `react` is
bumped to match, and for the audit.

What else uses these packages:

- Nothing outside landing-ui: ui-kit, console-ui and uaa-fe are Angular 20.
- Inside it, the libs' and themes' peer ranges (`next 16.x`, `react 19.x`) and next-intl 4.x (`next ^16`) already
  admit the new versions.

## Why the design is what it is

**16.3.5, not the smallest patch that clears the critical RCE.** 16.0.7 fixes GHSA-9qr9 and leaves the proxy bypasses,
the SSRF and the DoS open. Only 16.3.3 and later clear everything. Within 16.3:

| Patch | Fixes | Why it matters here |
|---|---|---|
| 16.3.1 | `headers()` in the proxy stale after `request.headers` changed | — |
| 16.3.2 | standalone output missing `@swc/helpers` ESM files once copied out of the tree | the Dockerfile copies `.next/standalone` |
| 16.3.2 | Turbopack worker chunks failed with `assetPrefix` | `start.mjs` sets a runtime prefix |
| 16.3.3 | the security release; AVIF optimisation disabled | — |
| 16.3.4 | `crossorigin=""` on chunk scripts broke cross-origin `assetPrefix` CDNs | the CDN mode of `STATIC_ASSETS_*` |
| 16.3.5 | per-request leak with `'use cache'`; image disk cache entries | — |

**React 19.2.8, not 19.3.0.** 19.2.1 to 19.2.8 are security and DoS fixes only. 19.3.0 is a feature release, and 19.2.8
is what Next 16.3.5's own `create-next-app` pins. `@types/react` stays `^19`.

**What 16.1 to 16.3 change for this app**, and where each is handled:

1. **`next dev` blocks cross-origin requests to `/_next/*`** (16.2). lcl serves the dev server through spg at
   `<store>.spg-507f1f77.gateway.com`, so the HMR socket is refused. Handled in phase 2.
2. **The proxy also runs on the `.rsc` and segment-prefetch forms of a URL** (16.2.5, the bypass fix). The matcher
   skips any path containing a dot, which is exactly how those forms used to miss it. SEC-02 probes every form, before
   and after. The `.rsc` form still reached the tree on 16.3.5, so phase 4 closes it.
3. **`experimental.validateRSCRequestHeaders` defaults to true** (16.3): an `RSC: 1` request whose `_rsc` does not
   match gets a 307. A browser sends the matching pair. load-testing's `page-budget.mjs` sends `RSC: 1` by hand and
   follows redirects; checked in the verification.
4. **The client router's defaults changed** (16.3): prefetch inlining, optimistic routing, vary params, a new scroll
   handler. Client navigation, back/forward and variant selection are re-QA'd (THM-09, VAR).
5. **`instrumentation.ts` also runs for the Node.js proxy in standalone** (16.3). A second `NodeSDK.start()` in one
   process would double the spans and the CPU. Checked; a guard is added only if it happens.
6. **`next build` type-checks with the project's own `tsc`, and may rewrite `tsconfig.json`** (16.3). The tsconfigs
   use `moduleResolution: bundler` and no `baseUrl`; `git status` after the build proves nothing moved.
7. **Turbopack's build cache is on** (16.3). The Dockerfile copies only `standalone` and `static`, so the image does
   not change.
8. **Still open in 16.3.5:** a Turbopack build crash (EISDIR) when an `outputFileTracingIncludes` glob meets a
   symlinked directory. The plan assumed the globs (`@aws-sdk`, `@smithy` and the like) could not meet one. They
   can: see phase 6.

**Next internals this app depends on**, each proved on the built output:

- `start.mjs` replaces the standalone `server.js`: `startServer` and `__NEXT_PRIVATE_STANDALONE_CONFIG`.
- `apply-prefix.mjs` rewrites the sentinel `assetPrefix` Turbopack bakes into manifests, `.rsc` files and chunks.
- `copy-instrumentation.mjs` follows Turbopack's chunk references with regexes, and copies what the hook requires.
- `prune-font-subsets.mjs` depends on what `next/font/google` emits.
- `locale-utils.ts` imports a type from `next/dist/…`.
- `proxy.ts` reads next-intl's `x-middleware-rewrite`.

**Fallback rule.** A 16.3 default that breaks the storefront is fixed at its cause. If that is impossible, that one
flag is turned off, with the reason recorded here and in the PR. No blanket rollback of defaults.

**Out of scope:**

- the landing-ui lint, typecheck and test step in `verify-before-push.sh`;
- a committed lockfile;
- `docker.sh`'s `npm ci`;
- the OpenTelemetry advisories `npm audit` reports once next is clean: 4 high and 23 moderate, all in the exact
  `@opentelemetry/*` pins plus `uuid`. They are addressed by moving to `sdk-node` 0.222 and its line, which changes the
  telemetry itself and needs its own measurement.
  - The highs are unreachable here: the Prometheus exporter and the Jaeger propagator are not configured.
  - One moderate is reachable: W3C baggage from a request header is parsed without a bound (GHSA-8988-4f7v-96qf).

## Phase 1 — this plan (commit 1)

## Phase 2 — `allowedDevOrigins` for the dev server behind spg (commit 2)

Harmless on 16.0, where the option exists and only turns today's warning off; required from 16.2. Revertible alone.

- `storefront/next.config.ts`: `allowedDevOrigins` derived from `INTERNAL_SPG`, which lcl already passes. The pod
  host's subdomains are allowed; no host is written into code.
- `references/landing-ui.md` in the `project-structure` skill, both copies (`.agents` and `.claude`).

## Phase 3 — the bump (commit 3)

- `storefront/package.json`: `next` 16.3.5, `react` and `react-dom` 19.2.8, `eslint-config-next` 16.3.5. The root
  `overrides`: `next`, `react` and `react-dom` to the same.
- A fresh `npm install`, as CodeBuild does. `npm ls` must show one copy of each; `npm audit` none against them.
- The root build, `npm run lint`, `npm run typecheck` and `npm test`. Findings from the new `eslint-config-next` are
  fixed in this commit.
- The internals above, on the built output. The instrumentation guard, only if needed.
- `copy-instrumentation.mjs` links Turbopack's external aliases into standalone (see *Deviations*), with
  `copy-instrumentation.test.mjs`.
- `./gradlew :store-pod:landing-ui:build`: CodeBuild's path.

## Phase 4 — every URL form of a theme tree goes through the proxy (commit 4)

SEC-02 on 16.0.0 found that `/t/fashion/en.rsc` answered 200 with the fashion tree's RSC payload when it was sent
`RSC: 1`. On 16.3.5 it answers the same without the header, and through a 307 with it. The matcher's first entry
skips every path with a dot, so the proxy's `/t` 404 never ran. Next's own fix for the prefetch forms does not reach
this matcher.

- `src/proxy.ts`: the matcher gains `/t/:path*`, so every URL under `/t`, whatever its suffix, reaches the 404.
- Nothing is exposed that `/en` does not already serve: the proxy holds no authorization, and the tree renders the same
  store's public data. What breaks is THM-07's contract, that a theme cannot be addressed from outside: with `?theme=`
  previews off in production, `/t/<other theme>/en.rsc` renders a store in a theme it never chose.

## Phase 5 — `next dev` writes no agent files into the app (commit 5)

The first 16.3 dev start wrote `storefront/AGENTS.md` and `storefront/CLAUDE.md`: Next's boilerplate agent rules,
re-created on every start whenever it detects a coding agent. `next.config.ts` sets `agentRules: false`. The repo's
agent rules are its root AGENTS.md and the project-structure skill, and every agent-run lcl stack would otherwise carry
two stray files. 16.0 does not know the key, so this lands after the bump.

## Phase 6 — the S3 SDK reaches standalone as a dependency closure (commit 6)

After any `next dev` in the checkout, `next build` failed with EISDIR on
`node_modules/@store-front/storefront/.next-<stack>/dev/node_modules/@aws-sdk/client-s3-<hash>`. Every lcl stack does
this, and `verify-before-push` then runs the build in the same worktree.

- **The mechanism** (`crates/next-api/src/nft.rs` at v16.3.5, vercel/next.js#96626):
  - Turbopack matches `outputFileTracingIncludes` globs anywhere in a path (`contains: true`).
  - It walks into the app through the workspace's own link and collects symlinks as files.
  - Since 16.3 it hashes every match. `node_modules/@aws-sdk/**` met the directory symlink `next dev` writes for the
    hashed external alias. Excludes run after the hashing.
- **The change:**
  - The globs go.
  - `copy-instrumentation.mjs` seeds its dependency-closure copy with the build's own `serverExternalPackages` as
    well, which is exactly the S3 SDK `start.mjs` loads.
  - Standalone carries the SDK's closure and nothing else the globs happened to match, and the image drops by a fifth.
  - A test covers it.

## Phase 7 — QA and this plan's record (commit 7)

- `qa/landing-ui-qa.md`:
  - a §SEC section:
    - SEC-01: no known advisory;
    - SEC-02: the theme trees stay private on every URL form, before and after;
    - SEC-03: the dev server behind spg;
  - the cases the upgrade could break, re-run and re-dated.
- Deviations and verification below.

## Other repos

- **load-testing:** no change needed. `page-budget.mjs` parses `__RSC_MANIFEST` and requests `RSC: 1`, and both still
  work on 16.3.5 (see *Verification*). Its landing-ui traces change shape: the proxy's span is a trace root of its own.
- **cvhome-platform, public-dkr, lcl:** nothing. The base image, port, env and routes are unchanged.

## Deviations, as built

- **16.3's Turbopack took every page down in the standalone build until `copy-instrumentation.mjs` learned its
  aliases.** 16.0 required an external package by its name (`require-in-the-middle`). 16.3 requires it by a hashed
  alias (`require-in-the-middle-2ca7b9c2766f317e`), a symlink the build writes in `.next/node_modules/`, which the
  hook's trace (`instrumentation.js.nft.json`) lists and standalone does not copy. The hook threw while loading, and
  every page answered 500 on `start.mjs`. `next build`, the lint, the typecheck and the tests were all green; only
  running the standalone build found it.
  - The script now recreates each alias from the trace as the same relative link, so it lands on the package the
    script already copies. It fails the build if a link would point at nothing.
  - `copy-instrumentation.test.mjs` covers both cases, plus a build without aliases.
  - This belongs to phase 3: it is needed only from 16.3.
- **Three phases the plan did not have.**
  - Phase 4, the `/t/:path*` matcher: SEC-02, meant to prove that Next's bypass fix covered the trees, found the
    `.rsc` form open on both versions.
  - Phase 5, `agentRules: false`: the first 16.3 dev start wrote the agent files.
  - Phase 6, the S3 SDK by closure: `verify-before-push`'s Gradle build ran after SEC-03's `next dev` in the same
    worktree and failed with EISDIR. The plan had called that bug unreachable here, and it was not. The first full
    verify run is the one that found it.
- **No instrumentation guard.** 16.3 runs the hook for the Node proxy too, but the SDK starts once.
  - The log shows one start, and spans per render are unchanged: 562 for the same 24 renders on each version.
  - What changed is the tree: the proxy's span is now a trace root of its own.
- **SEC-03 ran behind a stand-in for spg, not a full `lcl start`.** A Caddy in front of `next dev` adds org1-store2's
  headers and carries the WebSocket upgrade, which is all the case depends on. The server-side calls went to the load
  stack's spg.
- **The load stack belonged to another session.** It ran landing-ui in MinIO CDN mode from an unmerged load-testing
  branch (`feat/browser-spike`). This PR's image replaced only landing-ui there, and the stack was restored to
  `store-pod/landing-ui:native` afterwards. The CDN mode was a bonus: it exercised `start.mjs`'s S3 sync and the
  runtime prefix, which is where 16.3.0–16.3.3 broke (`crossorigin`).
- **Not re-run:** VAR SF-01 to SF-08, LUI-04 and AUTH-02/04 as written.
  - The k6 checkout journey placed an order and the auth journey registered and signed in, but neither follows
    those cases' steps.
  - Their tags are unchanged, from their runs on 16.0.
- **Found on the way, not changed:**
  - A cart code in `localStorage` that checkout no longer has makes every add a 404, and the storefront never starts
    a new cart (the QA file's known gaps).
  - The language switch on a product page lands on the not-found page, because slugs are per language (THM-09).

## Verification

- **Gates**
  - A fresh `npm install` resolves `next@16.3.5`, `react`/`react-dom@19.2.8` and `eslint-config-next@16.3.5`, one
    copy each.
  - `npm audit` shows 0 advisories against them, down from 31 against `next` 16.0.0 (osv.dev: 37 on the 16.0
    line). 27 remain, all in OpenTelemetry and `uuid`.
  - The root build is clean, with no config warning and no tsconfig rewrite.
  - `npm run lint` has 0 errors and the 4 warnings that predate this change.
  - `npm run typecheck` is clean.
  - `npm test` passes all 106 tests, 4 of them new in `copy-instrumentation.test.mjs`.
  - `npm run build` with a dev directory from `next dev` still in the checkout: before phase 6, EISDIR; after it, the
    build is clean.
- **The standalone build**, on `gcr.io/distroless/nodejs24` at 0.5 vCPU / 1 GiB with telemetry on, against
  org1-store2's recorded backend, over three interleaved rounds against 16.0.0:
  - The generated `server.js` still matches the tail of `start.mjs`.
  - 0 files keep the sentinel prefix after boot.
  - The hook's aliases are linked, and 0 errors are logged.

  | CPU per render (ms) | Home | Category | Product | Search | Mean |
  | --- | --- | --- | --- | --- | --- |
  | 16.0.0 (`main`) | 35.9 | 24.3 | 15.8 | 19.5 | 23.9 |
  | 16.3.5 | 33.3 | 25.5 | 15.0 | 17.8 | 22.9 (−4 %, noise) |

  - Anon memory after the renders: 179–185 → 172 MiB.
  - Spans: 562 = 562.
  - The four pages' prices are identical (22 / 14 / 1 / 9 in the visible HTML). The HTML differs only in Next's own
    markup: where `next-size-adjust` sits, and fewer inline flight scripts.
- **SEC-02.** On 16.0.0 and on 16.3.5 before phase 4, the `.rsc` form answered 200. After phase 4, every form is 404,
  first against the build and then through spg on the load stack.
- **The image**, built from the Dockerfile on the mirror's `nodejs24` (`v24.21.0`, amd64), on the load stack in CDN
  mode:
  - `make page-budget`: 48 of 48 pages pass, with no other theme's files and 222 theme modules attributed.
  - `rscNavKiB` is measured through the 307.
  - `make browser-shopper-auth`: registered and signed in through cua, 0 journey errors, 0 of 80 requests failed.
  - `make browser-shopper-checkout`: one order placed and 113 of 113 checks. 1 of 64 requests "failed": the journey's
    own `goto('/en/')`, whose 308 hop k6 counts.
  - `PROFILE=smoke make storefront-browse`: 17 of 17.
  - THM-08, THM-09 (in Chrome), THM-10 and the system routes pass.
  - After phase 6, rebuilt and swapped in again:
    - the image is 86.8 MB, against 107.5;
    - the S3 SDK loads inside it and uploads the build to MinIO;
    - page-budget passes 48 of 48, every tree form is 404 or a redirect to one, and checkout places an order.
- **SEC-03**, `next dev` behind the stand-in:
  - with `INTERNAL_SPG`: the `/_next/hmr` socket opens and the log has no blocked-origin line;
  - without it: the socket errors and the log reports the block;
  - with `agentRules: false`: no agent files are written.
- `extra/scripts/verify-before-push.sh`: see the PR.
