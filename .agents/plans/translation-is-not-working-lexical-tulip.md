# Professional toast notifications

## Context

`ToastService` (`src/app/shared/ui/toast/toast.ts`) and its renderer `ToastHost`
(`src/app/shared/ui/toast/toast-host.ts`) are the app's only feedback mechanism for
fire-and-forget outcomes — clipboard copy, form validation, "not available yet" actions,
and API errors routed through `NOTIFICATION_PORT`. Both are currently placeholders:

- `ToastMessage` has no `id`; `ToastService` only ever appends — there is no `dismiss()`
  and no auto-expiry, so toasts pile up forever until a full page reload.
- `ToastHost` renders `<p [class]="message.tone">{{ message.text }}</p>` inside a plain
  card. The `tone` class is set but never styled — no color, no icon, no close button, no
  entrance/exit animation. `track message.text` is fragile (two identical messages collide).

The user wants this brought up to the visual and interaction standard of the rest of the
console — animated, iconified, dismissible, "professional toast" polish (auto-dismiss with
a progress indicator, pause-on-hover, per-tone color).

## Design decisions

- **Tone → color/icon**, matching the wash/ink convention already used by
  `action-list.css` (`.action-icon.green/blue/amber/red`) and
  `console-toolbar.css` (`.notification-icon.*`) — same `--chart-N-wash` /
  `--chart-N-foreground` tokens, applied directly as `.toast.success/info/warning/danger`
  classes (no need to route through the generic `Tone` type, since toast tone is already a
  fixed semantic vocabulary):
  | tone | chart slot | icon |
  |---|---|---|
  | success | chart-1 (green) | `checkCircle` |
  | info | chart-2 (blue) | `info` *(new icon, see below)* |
  | warning | chart-4 (amber) | `alertCircle` |
  | danger | chart-5 (red) | `xCircle` |
- **Card shell**: `--card` background, `1px solid var(--border)`, `var(--radius-xl)`,
  `box-shadow: var(--lift)` (the one shadow token the theme reserves for exactly this —
  "reactions to interaction, nothing floats at rest"), a tone-tinted icon chip (38px
  rounded square, same recipe as `.action-icon`), message text, and a borderless close
  button styled like `plan-banner.css`'s `.dismiss` (muted → `--foreground-strong` on
  hover). Text uses `white-space: pre-line` — `ApiErrorService` joins multi-line danger
  messages with `\n` and that must still wrap legibly, not get flattened.
- **Auto-dismiss + pause-on-hover**: `ToastService` gains `durationMs` per message
  (success/info `4500`, warning `6000`, danger `null` — errors persist until dismissed,
  the common "professional toast" convention), plus `dismiss(id)`, `pause(id)`,
  `resume(id)`. A thin progress bar along the card's bottom edge is a pure CSS animation
  whose `animation-duration` is bound to `durationMs`; `ToastHost` calls
  `pause()`/`resume()` on `(mouseenter)`/`(mouseleave)`, which the service implements by
  clearing/rescheduling its own `setTimeout` against the remaining time — so the visual bar
  and the actual removal timer never drift apart.
- **Animation**: reuse the codebase's one established enter/leave idiom (seen in
  `busy-overlay.ts`/`.css` and `date-range-picker.html`/`.css`): `animate.enter="toast-in"
  animate.leave="toast-out"` on each card, a single `@keyframes toast-in` (fade +
  slide from the inline-end, mirrored under `[dir='rtl']` via a custom property, not a bare
  `translateX`), and `.toast-out` running the same keyframes `reverse` at a shorter
  duration. Add the same `@media (prefers-reduced-motion: reduce)` fallback
  `busy-overlay.css` uses (drop motion, keep the outcome legible).
- **New icon**: `icon-paths.ts` has `checkCircle`/`alertCircle`/`xCircle` but no distinct
  "info" glyph (reusing `alertCircle` would make warning and info look identical). Add one
  `info` entry in the same 24×24 stroke idiom.
- **i18n**: add `shared.toast.dismiss` ("Dismiss notification") for the close button's
  `aria-label`, following the exact precedent of `shell.planBanner.dismiss`, plus a
  `shared.toast.regionLabel` for the stack's own `aria-label` (paired with the existing
  `aria-live="polite" aria-relevant="additions"`), and one sr-only tone-prefix key per tone
  (`shared.toast.tone.success/info/warning/danger`) so assistive tech gets the outcome
  even though color/icon carry it visually — the same "tone is never the only signal" rule
  `tone.ts` documents for `Badge`. Add both keys to `en.json` and `ar.json`.

## Files to change

- **`src/app/shared/ui/toast/toast.ts`** — add `id`, `durationMs` to `ToastMessage`;
  implement `dismiss(id)`, `pause(id)`, `resume(id)` with an internal
  `Map<id, {handle, remaining, startedAt}>` timer registry; keep the existing
  `success/info/warning/danger(text: string)` public method signatures unchanged (~15
  call sites across `orders.ts`, `copy-field.ts`, store-management sections, and
  `ApiErrorService` via `NOTIFICATION_PORT` all call these as-is today and need no changes).
- **`src/app/shared/ui/toast/toast-host.ts`** — rewrite the template: icon chip, message
  (`pre-line`), close button, progress bar; `track message.id`; move styles to a new
  `toast-host.css` (growing past what's comfortable inline); wire
  `*transloco="let t"` / `TranslocoDirective` for the two new ARIA strings.
- **`src/app/shared/ui/icon/icon-paths.ts`** — add the `info` path.
- **`src/locale/en.json`, `src/locale/ar.json`** — add the `shared.toast.*` keys described
  above, alongside the existing `shared.*` block.
- **Optional, matching the codebase's own bar for what gets a spec** (only
  `date-range-picker` and `export-button` — components with real timing/interaction logic —
  have `.spec.ts` files today; `Badge`/`NoticeBar`/`PlanBanner` don't): add
  `toast.spec.ts` covering `dismiss`, auto-expiry via `fakeAsync`/`tick`, and
  `pause`/`resume` not double-firing removal.

## Verification

1. `npm test -- --watch=false` — full suite green, plus the new toast spec if added.
2. `npm run lint` — clean, including the i18n literal-text rule already enforced.
3. Start the dev server, trigger each tone from the running app (e.g. store-management's
   "not available yet" actions for info, copy-field's copy button for success/warning,
   an invalid form submit for danger) and confirm in-browser: icon + color per tone,
   entrance/exit animation, close button dismisses immediately, progress bar pauses on
   hover and resumes with the correct remaining time, danger toasts do not auto-dismiss,
   multi-line danger text wraps without truncation, and the stack still anchors correctly
   with `dir="rtl"` (Arabic) — slide direction mirrors, `inset-inline-end` still reads as
   the right place.
4. `npm run build` — production build succeeds.
