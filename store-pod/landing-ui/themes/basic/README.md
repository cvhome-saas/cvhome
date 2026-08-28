# basic theme — The Catalogue Page

The platform's multi-purpose default (v1.0.0, 2026-08-22): one continuous ruled catalogue of entries with big
condensed prices, a thumb-index strip of categories under the masthead, the merchant's primary as flat fields
only. Category-neutral by design — a bakery, a phone shop and a jeweller can all wear it. Visual system in
`DESIGN.md`; the direction contract is the comment at the top of `src/layout/Root.tsx`.

Dev: `http://org1-store1.spg-507f1f77.gateway.com/en?theme=basic` (and `/ar?theme=basic`). Not yet mapped from
any legacy `Theme` enum value nor set as `STOREFRONT_FALLBACK_THEME` — see
`storefront/src/shell/theme/legacy-theme-map.ts` when that decision is made. Build guide:
`.agents/skills/project-structure/references/new-landing-ui-template.md`.
