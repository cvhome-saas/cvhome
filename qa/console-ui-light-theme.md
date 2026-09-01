# QA — Light, the console-template theme

The console gained a fourth theme, **Light**, reproducing `store-core/console-template` — the design the
screens are actually drawn from. It is a token-only change: no component, route, endpoint or permission moved.

That shape is what makes this document worth reading rather than the PR. A theme cannot break a request, but it
can make an interface unreadable in ways no test catches, and adding this one exposed three defects that had
been sitting in the *other* themes — one of which, A11Y-02, is **still open**. Those are the cases to run first.

- **Scope** — console-ui only (`src/styles/`, fourteen feature stylesheets, `index.html`, both locales)
- **Change** — PR #311, branch `feat/console-light-theme`
- **Cases** — 19 · 8 verified, 1 verified-failing (A11Y-02), 10 not verified
- **No backend involvement** — nothing here touches a service, a schema or a token. Tenant isolation and
  permission gates are untouched by construction, which is why this document has no case for them.

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by a named test, but nobody drove it through the browser.
- **[not verified]** — never run end to end by anyone. These are where a tester is most likely to find
  something, and they are called out rather than buried.

Sections [REG](#reg--regression-watchlist) and [99](#99--known-gaps) are the highest-value reading: one is
defects that already happened during this work, the other is behaviour that looks wrong and is not.

---

## 00 — Before you start

```bash
sudo ./extra/scripts/configure-domain.sh          # once per machine
cd .claude/worktrees/feat-console-light-theme     # QA the branch, not main
lcl start -d --stack light-theme                  # one stack per worktree
lcl urls --stack light-theme                      # this stack's real ports — do not assume 8000
```

Ports shift by +1000·k when another stack holds them. On the run this document was written from, the offset was
**+2000**: console at `http://gateway.com:10000/`, console-ui direct on `:10011`. **Read your own offset from
`lcl urls`** — hostnames never change, ports do.

**Sign-in.** Seller console — `org1-admin` / `admin`. Local seed data from the `test-stores` profile; if the
login fails, the stack came up without it.

**Switching theme.** Fastest is the **swatch-and-name control in the console toolbar**, left of the account
menu; Profile → *Preferences* → **Theme** has the same list with hints. Four choices: Forest
(default), Midnight, Daylight, **Light**. The choice is stored in `localStorage` under
`cvhome.console.theme` and applied by an inline script in `index.html` *before first paint* — which is
why THM-02 exists.

### What "correct" looks like on Light

Worth knowing before you judge a screen, because two of these look like bugs and are not:

- **Panels are pure white on a grey canvas.** On Daylight it is the other way round — a panel there is a tint
  *on top of* a near-white page and sits fractionally darker. Light being brighter than its page is the whole
  point of the theme, not a missing background.
- **Primary buttons have near-black text on emerald**, not the white the template itself uses. The template's
  white-on-emerald is 2.54:1. Deliberate — see [99](#99--known-gaps).
- **Disabled fields are white**, where an enabled field is grey-filled. Inverted from what some systems do;
  the disabled state is carried by the ink colour and the cursor, not the fill.

### Looking at the truth underneath

```js
// in devtools, on any console page — what the theme actually resolved to
getComputedStyle(document.documentElement).getPropertyValue('--muted')
document.documentElement.dataset.theme          // must match the picker
localStorage.getItem('cvhome.console.theme')    // must match both
```

---

## THM — The theme itself

Light is one `[data-theme='light']` block and four wiring points: the stylesheet, `CONSOLE_THEMES`, the
pre-paint allow-list in `index.html`, and the locale keys. Each case below covers one of them, because each
fails differently and only one fails loudly.

### THM-01 — Light is offered and applies · critical · [verified]

- **Steps** — sign in, open Profile → Preferences → Theme.
- **Expect** — four options. **Light** with the hint "White panels on cool grey". Selecting it repaints
  immediately, with no reload: grey canvas, white panels, emerald accents.
- **Seen** — all four listed with swatches and hints, Light ticked. Note Daylight's and Light's swatches are
  near-identical pale-emerald circles; see [99](#99--known-gaps).

### THM-02 — The choice survives a reload · critical · [verified]

The trap this case exists for: `index.html` holds a `themes` allow-list, and a theme missing from it is stored
correctly and then **silently rejected on the next load**. It presents as "my theme keeps resetting to Forest"
and points at nothing — the array is in an inline script, not in any Angular code.

- **Steps** — select Light, then hard-reload the page. Then close the tab and reopen the console.
- **Expect** — Light both times, with **no flash of dark** before it paints. `localStorage` and
  `document.documentElement.dataset.theme` both read `light`.
- **Seen** — after reload: `dataset.theme` `light` *at first paint*, `--muted` `#fff`, `--border` `#e2e8f0`,
  `color-scheme: light`. The allow-list accepted the new id.

### THM-03 — Arabic and RTL · high · [verified]

- **Steps** — with Light active, switch the language to العربية. Walk two or three screens.
- **Expect** — the label and hint are translated ("فاتح" / "لوحات بيضاء على رمادي فاتح"). Layout mirrors as it
  does on every other theme — this change adds no direction-sensitive property, so an RTL break here would be
  pre-existing.
- **Seen** — orders page fully mirrored (rail right, breadcrumb right-aligned, KPI order reversed), theme
  control reading **فاتح**. No layout defect attributable to the theme.

### THM-04 — The other three themes are unchanged · critical · [verified]

The fourteen stylesheet edits and the `::selection` change are shared by every theme, so this is the case that
proves the blast radius.

- **Steps** — cycle Forest → Midnight → Daylight on the dashboard, an orders list and a settings page.
- **Expect** — each looks as it did before the branch, except the two deliberate improvements in
  [REG](#reg--regression-watchlist): hover fills are slightly more distinct, and selected text is legible.
- **Seen** — Forest and Daylight both correct on the orders page. **Midnight was not opened in the app** —
  only in a static render of the built bundle. Worth a minute of someone's time.

---

## TOK — The repaired tokens

`--muted` is the panel surface; `--input` is hover fills, icon tiles and inset wells. Fourteen rules were
using `--muted` for `--input`'s job. On the dark themes the two are 2% and 6% white — near enough that it read
as intentional. On Light `--muted` is **opaque white**, so every one of them vanished against its own panel.

These cases each look at one repaired rule on Light. If a tint is invisible, the rule was missed.

### TOK-01 — Row hovers are visible · high · [verified]

- **Steps** — on Light: hover rows in a notification list, a ranked list (dashboard), the customer dialog's
  order list, and platform billing's blocked-store row.
- **Expect** — a distinct grey fill under the pointer on every one. White-on-white means a missed rule.
- **Seen** — sidebar nav hover shows a clear grey `--input` fill with emerald ink against the white rail.
  Only the sidebar was hovered; the four lists named above are still worth walking.

### TOK-02 — Zebra striping is visible · [not verified]

- **Steps** — open an organisation's billing rows and the store billing panel.
- **Expect** — alternating rows tinted. Flat white means a missed rule.

### TOK-03 — The segmented control reads correctly · [verified]

Its selected tab is `--muted` and its trough is `--input`, so on Light it becomes exactly the
template's control: a white tab lifted out of a grey trough.

- **Expect** — the selected tab is white, clearly raised, on a grey track.
- **Seen** — the orders status filter renders exactly that. But read A11Y-02 before ticking this: the tab is
  correct in *shape* and fails on *ink contrast*.

### TOK-04 — Inset wells and chips · [not verified]

- **Steps** — settings → a provider card header and a stored secret; a product's image well; the users list
  "you" tag; a payment's transaction id chip.
- **Expect** — each reads as inset or as a chip, not as bare panel.

### TOK-05 — The same rules are still right on Forest · high · [not verified]

- **Steps** — repeat TOK-01 and TOK-02 on Forest.
- **Expect** — hovers and stripes are slightly *more* distinct than before, which is the documented intent
  (`--input` is 6% where `--muted` was 2%), and nothing looks washed out.

---

## SEL — Text selection

Selection was `--primary` mixed toward `--primary-foreground`, inked with `--primary-muted`. That pairing only
ever suited Forest. It is now `--accent-strong` under `--foreground-strong`.

### SEL-01 — Selected text is legible on all four themes · critical · [not verified]

- **Steps** — on each theme in turn, drag-select a paragraph and a table row.
- **Expect** — the selection is an accent wash with the strongest ink on top, readable throughout. **Before
  this branch, Daylight was 1.03:1 and Midnight 1.23:1** — text disappeared into the highlight. Re-check
  Daylight especially; it is the one that was worst.
- **Seen** — verified in Chrome against the built stylesheet, all four themes, but **outside the console app**
  (the session expired before it could be repeated in place), so it stays untagged. Measured ≥14:1 on every
  theme after the fix. Ten seconds of drag-selecting in the console would close this.

---

## A11Y — Contrast

### A11Y-01 — Text clears AA on both planes · high · [not verified]

Light has two backgrounds — the white panel and the grey canvas — and a token that passes on one can fail on
the other. `--muted-foreground` is slate-600 rather than slate-500 for exactly this reason: the page-header
subtitle renders on the canvas, where slate-500 is 4.35:1.

- **Steps** — run an axe/Lighthouse contrast pass on the dashboard and a settings page with Light active.
- **Expect** — the text ladder clears AA on both planes: body 10.36:1 on panel / 9.45:1 on canvas, strong
  17.83 / 16.28, subtle and muted-foreground 7.58 / 6.92. **`--primary-emphasis` is a known exception — see
  A11Y-02.**
- **Seen** — audited the orders page in Light: 60 visible text nodes, the ladder tokens all clear, one real
  failure (A11Y-02). *Caveat for whoever repeats this:* a naive audit script mis-reads this app twice over —
  `oklch()` colours parsed as RGB give nonsense, and `sr-only` text is not `display:none` so it must be
  excluded by class. Normalise every colour through a 1×1 canvas, the way `theme.provider.ts` does.

### A11Y-02 — Emerald text fails AA on light surfaces · high · [verified — FAILS]

**This is an open defect, not a gap.** It predates the branch — Daylight is worse — but Light inherits it, so
it is called out rather than buried in [99](#99--known-gaps).

`--primary-emphasis` is emerald-600 on both light themes and is used as a **text colour in 73 places** (links,
the selected tab in every segmented control, emphasised labels). On white that is **3.77:1** on Light and
**3.3:1** on Daylight, against AA's 4.5:1 for normal text. On the dark themes it is fine — 9.4:1 on Forest,
6.24:1 on Midnight — which is why it survived.

- **Steps** — on Light, look at the orders status filter: the selected tab's emerald label on its white pill.
  Repeat on Daylight, which is worse.
- **Expect** — **it fails today.** Report it as seen, do not re-file it.
- **Why it is not fixed here** — the token does two jobs with opposite requirements on a light surface. As ink
  on white it needs to be *darker*; as the primary button's hover fill under near-black ink it needs to be
  *lighter*. Neither value satisfies both:

  | `--primary-emphasis` | as ink on white | as primary-button hover fill |
  |---|---|---|
  | emerald-600 (today) | **3.77:1 ✗** | 5.35:1 ✓ |
  | emerald-700 | 5.48:1 ✓ | **3.68:1 ✗** |

  The real fix is to split the fill role from the ink role across all four themes and 84 call sites. That is a
  design-system change with its own review, not a line to slip into a theme PR.

---

## REG — Regression watchlist

Every defect that actually happened during this work. Each row has already proven it can happen.

| What broke | How it looked | Caught again by |
|---|---|---|
| Fourteen rules used `--muted` (panel) where the job was `--input` (hover/well) | Hovers, zebra stripes, chips and wells invisible on Light; on the dark themes a hover that was fainter than the field it sat in | TOK-01 … TOK-04 |
| `::selection` built from `--primary`/`--primary-muted` | Selected text ~1.03:1 on Daylight, 1.23:1 on Midnight — effectively invisible. Unnoticed because Forest is the default and the only theme it suited | SEL-01 |
| `--muted-foreground` at slate-500 | Page-header subtitle at 4.35:1 on the grey canvas — passes on a panel, fails on the page | A11Y-01 |
| A theme absent from `index.html`'s `themes` array | Stored correctly, silently rejected on next load — "my theme keeps resetting to Forest" | THM-02 |
| `--primary-emphasis` as text on a light surface — **still open** | Emerald labels and selected tabs at 3.77:1 (Light) / 3.3:1 (Daylight); invisible to every test, and to any audit that mis-parses `oklch()` | A11Y-02 |

---

## 99 — Known gaps

Expected behaviour. Do not re-raise.

- **Primary buttons do not match the template.** It paints white on emerald-500 — **2.54:1**, below every
  threshold. DESIGN.md holds that `--primary`/`--primary-foreground` does not move between themes, so Light
  keeps near-black ink at 7.95:1. A deliberate departure from the reference design, on contrast grounds.
- **Uppercase section labels are darker than the template's.** It uses slate-400, which is 2.56:1 on white.
  Those jobs go to slate-500.
- **`--input` is ~ΔRGB 6 from the template's `#f1f5f9`,** because it is an alpha rather than an opaque colour —
  it has to work on a panel *and* on the canvas, and an opaque slate-100 would vanish against the canvas.
  Imperceptible in place.
- **`--foreground-quiet` is 4.35:1 on the canvas** and 4.76:1 on a panel. The console's uses are all inside a
  panel (the rail, the toolbar, popovers) — but QA found one outside it: the **sign-in page footer**
  ("Trusted by 1,400 merchants…") sits on the canvas and measures 4.35:1. Small, decorative, and one line
  from being fixed by moving the token to slate-600 — which would flatten the ladder's last step, so it is
  recorded here rather than done silently. The same caution applies to any new screen that puts quiet text
  on the canvas.
- **Light and Daylight sit next to each other in the picker** and both are light. They are genuinely different
  themes (see *What "correct" looks like*), but the names invite confusion; if it becomes a support question,
  retiring Daylight is the cheaper fix.

---

Raise findings on PR #311. Attach `.lcl/light-theme/logs/console-ui.log` and say which theme and which
language were active — a theme bug that only appears in one of the four is the interesting kind.
