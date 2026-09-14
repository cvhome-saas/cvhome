# landing-ui: Next.js 16.0.0 → 16.3.5, React 19.2.0 → 19.2.8

A security upgrade. It changes no feature, and it ships alone with its own QA. cvhome#357 and the orchestrator plan
`landing-ui-cpu-memory.md` flagged it as urgent and kept it out of their own scope.

## Context

landing-ui pins `next` 16.0.0 and `react`/`react-dom` 19.2.0:

- `storefront/package.json`: `next`, `react`, `react-dom` and `eslint-config-next` are exact.
- The root `package.json` `overrides` force `next`, `react` and `react-dom` on every workspace.
- No lockfile is committed (`**/package-lock.json` is gitignored, and Gradle `clean` deletes it). Every build, CodeBuild's
  included, resolves fresh, so these exact pins are the only control.

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
   skips any path containing a dot, which is exactly how those forms used to miss it. Proved by SEC-02, before and
   after.
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
   symlinked directory. The globs name `@aws-sdk`, `@smithy` and the like, not the workspace symlinks; the build
   proves it.

**Next internals this app depends on**, each proved on the built output:

- `start.mjs` replaces the standalone `server.js`: `startServer` and `__NEXT_PRIVATE_STANDALONE_CONFIG`.
- `apply-prefix.mjs` rewrites the sentinel `assetPrefix` Turbopack bakes into manifests, `.rsc` files and chunks.
- `copy-instrumentation.mjs` follows Turbopack's chunk references with regexes.
- `prune-font-subsets.mjs` depends on what `next/font/google` emits.
- `locale-utils.ts` imports a type from `next/dist/…`.
- `proxy.ts` reads next-intl's `x-middleware-rewrite`.

**Fallback rule.** A 16.3 default that breaks the storefront is fixed at its cause. If that is impossible, that one
flag is turned off, with the reason recorded here and in the PR. No blanket rollback of defaults.

**Out of scope:**

- the landing-ui lint, typecheck and test step in `verify-before-push.sh`;
- a committed lockfile;
- `docker.sh`'s `npm ci`.

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
- `./gradlew :store-pod:landing-ui:build`: CodeBuild's path.

## Phase 4 — QA and this plan's record (commit 4)

- `qa/landing-ui-qa.md`:
  - a §SEC section:
    - SEC-01: no known advisory;
    - SEC-02: the theme trees stay private on every URL form, before and after;
    - SEC-03: the dev server behind spg;
  - the cases the upgrade could break, re-run and re-dated.
- Deviations and verification below.

## Other repos

- **load-testing:** no change planned. `page-budget.mjs` parses `__RSC_MANIFEST` and requests `RSC: 1`. Both are checked
  against a 16.3.5 build. If either breaks, that is a load-testing PR through the orchestrator.
- **cvhome-platform, public-dkr, lcl:** nothing. The base image, port, env and routes are unchanged.

## Deviations, as built

## Verification
