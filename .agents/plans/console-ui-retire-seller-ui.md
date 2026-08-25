# Module 13 — retire seller-ui (and content Phase 7)

## Context

The console-ui go-live migration (`.claude/plans/agents-requirments-console-ui-go-live-m-woolly-candy.md`)
is further along than its own headings say: Modules 1–12 are **all shipped** on `feat/mirror-console-ui`
(8 users/profile, 9 customers, 10 as the `/subscription` billing page, 11 the platform console, 12 content
— each verified by landed commits and existing features; the plan doc's headings are simply stale).
The nav rail has no routeless items left, no fixture files remain, and `lessons.md` holds 168 entries.

The one step remaining in the named order is **Module 13 — retire seller-ui**, which the content plan
(`.agents/plans/console-ui-content.md`) says also carries **content Phase 7**: delete the legacy content
compat surface. This document is that module's plan, following the standing per-module lifecycle
(plan commit → implementation commits → QA commit).

## Decisions (settled with the user)

| Question | Decision |
|---|---|
| Apex route | **Repoint the catch-all to console-ui.** `gateway.com` / `www.gateway.com` → `lb://console-ui`; the `seller-ui.gateway.com` host is deleted outright. uaa's seeded client already carries the apex redirect URI, so login keeps working. |
| landing-ui `Page`/`Box` | **Full replacement.** Delete the types and the three legacy `/api/v1/content/**` methods; declare slim view models in the theme contract; retype loaders; update all four themes. |
| Reference preservation | **Tag `seller-ui-final`** on the last pre-deletion commit so plan docs citing seller-core as "the reference ported from" stay resolvable. |
| `extra/requests/store-ui` | **Renamed, not deleted** → `extra/requests/console-ui/`, `SELLER-UI_URL` → `GATEWAY_URL` (`http://gateway.com:8000`). The 122 requests hit still-valid backend endpoints. |
| Checkout agreement fallback | **Dropped.** `use-checkout-form`'s `getBox('agreement')` fallback goes; QA verifies demo stores have a TERMS policy. |
| Stale `issue.http` files | **Deleted** (catalog + checkout repro scratch hitting a host:port that never existed). |

## Precondition

The working tree is dirty with unrelated in-flight work (`configure-domain.sh`, `run-lcl.sh`,
console-ui pod-detail files, `plan-dialog.html`, two tenancy Java files, an untracked plan file).
**Commit or stash that first** — this module deletes whole directories and must start clean.

---

## Step 1 — gateway, discovery, config, uaa
`feat(gateway,uaa): apex serves console-ui; drop seller-ui from routing, discovery and config`

- `store-core/gateway/gateway-service/.../GatewayRouteLocatorImpl.java:68–81` — the catch-all route
  (`gateway.com`, `www.gateway.com`, `seller-ui.gateway.com` → `lb://seller-ui`) becomes apex + `www.`
  → `lb://console-ui`; the seller-ui host is removed. Update the line-21 comment ("seller-ui's
  catch-all" → console-ui's).
- `store-commons/autoconfigure/src/main/resources/common-config.yml:8,52–57` — remove `seller-ui`
  from the gateway `sub:` list and the whole `services.seller-ui` block (port 8010).
- `.../fargate-config.yml:13,32` — remove the `seller-ui` service + port.
- `.../lcl-config.yml:38–43` — remove SimpleDiscoveryClient instance `seller-ui-1`.
- `extra/scripts/run-lcl.sh:65` — remove the seller-ui row from `NODE_SERVICES`.
- `extra/scripts/configure-domain.sh:24` — remove the `seller-ui.gateway.com` hosts entry.
- `store-core/uaa/src/main/resources/init-sql/data-common.sql:72–73` — remove the
  `http://seller-ui.gateway.com:8000` redirect URIs/origins from the seeded OAuth client
  (apex + console-ui entries stay).
- `store-core/uaa/.../uaa-fe/.../header.component.html:6` — the header logo text reads "seller-ui";
  rename to the console's name.

The stack still boots after this commit with seller-ui merely unrouted — a safe intermediate state.

## Step 2 — tag, then delete the app
**Tag `seller-ui-final`** on the Step-1 commit. Then:
`chore(seller-ui): delete seller-ui and seller-core`

- `git rm -r store-core/seller-ui` (includes `projects/seller-core`).
- `settings.gradle:79` — remove `'store-core:seller-ui'`.

## Step 3 — content Phase 7, backend half
`chore(content): retire legacy compat — phase 7`

- Delete `store-pod/content/content-service/.../api/v1/LegacyContentApi.java`
  (`@RequestMapping("/api/v1/content")`), its `LegacyContentFacade`, the
  `model/legacy/LegacyContentBox|LegacyContentPage|LegacyContentPageList` classes, and every
  legacy mapper/compat-shape test (grep `Legacy` under content-core to catch them), plus
  `content-service/http/legacy-api.http`.
- `store-pod/spg/Caddyfile:38–48` — delete the `@legacy_content` matcher and its `handle`
  (`/api/v1/content*`, `/api/v1/private/content*`, `/api/v1/private/files` → `content:8121`);
  the surrounding merchant `handle` collapses to the plain `reverse_proxy`.
- `git rm -r store-pod/content-deprecated` — verified already unwired from `settings.gradle`,
  `run-lcl.sh`, `configure-domain.sh` and `docker-compose-lcl.yml`; pure deletion.

## Step 4 — content Phase 7, landing-ui half
`feat(landing-ui): drop legacy Page/Box content shapes`

- `libs/services/src/content-service.ts` — delete `getContents`, `getPage`, `getBox`
  (the whole `/api/v1/content/**` client surface; `getPage` already has no callers).
- `libs/types/src/content.ts` — delete `ContentPage`, `Page`, `Box`.
- `libs/theme/src/contract.ts` — replace `LayoutData.pages: Page[]`, `LayoutData.announcement?: Box`,
  `ContentData.page: Page` with slim contract-owned view models (e.g. `NavPage {code; name; href}`,
  `Announcement {html}`).
- Retype the adapters: `storefront/src/shell/loaders/layout.ts` (`linkAsPage`, `bannerAsBox`),
  `loaders/content.ts` (`asPage`), and the helpers in `loaders/site.ts`.
- `storefront/src/shell/search/category-nav-search-provider.ts:20` — the one live `getContents`
  caller; rewrite to read footer/menu pages off the site document.
- `libs/hooks/src/use-checkout-form.ts:98` — drop the `getBox('agreement')` fallback;
  `getPolicy('TERMS')` alone.
- Mechanically update the four themes (`themes/{starter,basic,beauty,fashion}/src/layout/
  {Footer,Nav,MobileNav,Announcement,Root,IndexStrip}.tsx` + `pages/Content.tsx`) to the new
  view-model fields.
- **Keep** `libs/types/src/constant.ts:1` `CART_DATA_KEY = "seller-ui-cart-data"` — it is a shopper
  localStorage key and renaming it empties every live cart; add a comment saying so.

## Step 5 — docs, requests, provenance sweep
`docs: retire seller-ui from the project map`

- **Provenance sweep:** delete the 53 `Ported from seller-ui` lines across 32 files under
  `store-core/console-ui/src` — the migration framework's promise. Narrative seller-ui mentions
  (~40 comment lines explaining decisions) **stay**; they are history.
- `store-core/console-ui/README.md:6` ("is replacing" → "replaced") and `ARCHITECTURE.md:56` retense.
- `AGENTS.md:55,72` build/run examples; `.github/PULL_REQUEST_TEMPLATE.md:3,95,99` (i18n asset paths,
  angular.json checklist item); `.claude/commands/go.md:22` (the stray-angular.json warning).
- **Both** project-structure skill copies (`.claude/skills/project-structure/` and
  `.agents/skills/project-structure/`): `SKILL.md` description + seller-ui (:8010) service row +
  gateway catch-all note (line 258), and the `references/*.md` set (`new-service.md` alone has 16 hits).
- `extra/requests/`: rename `store-ui/` → `console-ui/`; `http-client.env.json` `SELLER-UI_URL` →
  `GATEWAY_URL` = `http://gateway.com:8000`; sed the 122 `{{SELLER-UI_URL}}` uses.
- Delete `store-pod/catalog/catalog-service/issue.http` and `store-pod/checkout/checkout-service/issue.http`.
- Optional retense of Java doc comments (`SubscriptionApi:66,74`, `OrgManagerApi:105`,
  `BillingStatisticApi`) — "when seller-ui is retired" is now past tense.
- **Untouched, deliberately:** `.claude/plans/*`, `.agents/plans/*`, `.agents/requirments/*`,
  `qa/*.md`, `store-core/console-ui/lessons.md` — historical records keep their references.

## Step 6 — QA
`fix(console-ui,landing-ui): module 13 after QA`

Plus the `lessons.md` closing entry: seller-ui retired, seller-core reference frozen at tag
`seller-ui-final`; the entry at lessons.md:908 that points at content-deprecated gains a note that
those mappers now live in git history.

---

## Verification

1. `./gradlew build -x test -x check`, then targeted:
   `:store-core:gateway:gateway-service:test`, `:store-core:uaa:test`,
   `:store-pod:content:content-service:test checkstyleMain checkstyleTest`.
2. console-ui: `npm run build && npm run lint && npm run test:ci`.
   landing-ui: libs `tsc -b` then `npm run build`.
3. `bash extra/scripts/run-lcl.sh` — full stack boots with no seller-ui and no content-deprecated;
   port 8010 stays closed.
4. Greps:
   - `grep -rn "Ported from seller-ui" store-core/console-ui/src` → 0.
   - `grep -rn "legacy_content\|LegacyContent\|content-deprecated" store-pod store-core --exclude-dir=node_modules` → 0.
   - `grep -rn "seller-ui" --exclude-dir={.git,node_modules,.claude,.agents,dist} .` → only the
     expected residuals: `qa/*.md`, console-ui narrative comments + `lessons.md`, landing-ui
     `constant.ts`, retensed Java doc comments.
5. Browser QA (Chrome, per the standing convention):
   - `http://gateway.com:8000` and `www.` land on console-ui and the OAuth login completes;
     `console-ui.gateway.com:8000` unchanged.
   - Storefront: footer info pages, announcement strip, `/en/content/<slug>`, blog/help/policies,
     and the checkout agreement on a store **with** a TERMS policy and one **without** — confirm no
     request to `/api/v1/content/*` in the network panel.
   - Arabic and all three console themes spot-check.
6. A couple of blocks from the renamed `extra/requests/console-ui/**` run green with `GATEWAY_URL`.

## Risks

- **Checkout agreement**: a store with only the legacy `agreement` box and no TERMS policy loses its
  agreement checkbox. Accepted; QA confirms demo stores carry a TERMS policy.
- **Apex behavior change**: `gateway.com:8000` now serves console-ui — decided above.
- **External deploy config**: no CI in-repo references seller-ui (verified), but any deploy config
  outside this repo still building `:store-core:seller-ui:bootBuildImage` must be updated separately.

## Commits

1. `plan(console-ui): module 13 — retire seller-ui` — this document (as `.agents/plans/console-ui-retire-seller-ui.md`, per the module convention).
2. `feat(gateway,uaa): apex serves console-ui; drop seller-ui from routing, discovery and config`
   — then tag `seller-ui-final`.
3. `chore(seller-ui): delete seller-ui and seller-core`.
4. `chore(content): retire legacy compat — phase 7`.
5. `feat(landing-ui): drop legacy Page/Box content shapes`.
6. `docs: retire seller-ui from the project map`.
7. `fix(console-ui,landing-ui): module 13 after QA`.
