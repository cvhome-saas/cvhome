# Todos

Work that is known, agreed and not yet done. One heading per item, newest first. Something that is
*done* belongs in `lessons.md` (what we learned) or in the code; this file is only for what is still
owed.

---

## Spacing has no tokens, so every screen re-invents it

**Status:** open · **Cost:** ~half a day · **Blast radius:** every stylesheet in `src/app`

### The problem

`theme.css` tokenises colour, radius (`--radius-xs` … `--radius-3xl`, eight steps) and type
(`--text-2xs` … `--text-lg`). **Spacing is the one scale that was never tokenised.** Every padding
and every gap in the console is a raw literal, so the same measurement is retyped per component and
drifts. Today, in `src/app`:

- `padding: 1rem` × 14, `1.25rem` × 12, `1.35rem` × 11, `1.5rem` × 10, `1.75rem` × 4 — five values
  doing one job with nothing to say which is correct.
- `gap: 1rem` × 55, `0.75rem` × 47, `1.25rem` × 36, `1.5rem` × 35, `0.85rem` × 29.
- The panel interior, `1.25rem 1.5rem`, is declared in **six** places: twice in `panel.css` (head and
  padded body) and hand-rolled again in `order-details.css`, `create-store.css` and `first-run.css`
  (twice). `Panel.padded` was introduced to end exactly this and consolidated five of them; four have
  since grown back.

### Why it keeps coming back

Because there is nothing to reach for. A developer writing a new panel has no `var(--space-…)` to
type, so they type a number, and the number they pick is whatever the component beside them used —
or, if they are reading the design mock rather than the code, whatever the mock happened to render
at. Two failures in the store-appearance tab came from this in one sitting (see the fix in
`ebd9395cd`):

1. Its three panels never passed `padded`, so the body sat flush at x=589 while every heading was
   inset to x=613. Nothing failed; the tab simply looked wrong, and only against a sibling screen.
2. The hub's `gap: 1.25rem` lives on `.hub-section` and reaches only a tab's *direct* children. The
   tab wraps its panels in one `app-busy-overlay`, so the gap silently did not apply and the panels
   stacked with their borders touching.

Neither is catchable by `ng build`, `ng test` or stylelint as configured. Both are the same root
cause: the rhythm is implicit.

### What to do

1. Add a spacing scale to `styles/theme.css` beside the radius block, on `:root` so component
   stylesheets read it through `var()` the same way. Derive the steps from what the console already
   uses rather than inventing a new scale — the four that carry real weight are the field gap
   (`0.75rem`), the group gap (`1rem`), the section gap (`1.25rem`) and the card interior
   (`1.25rem 1.5rem`).
2. Express the panel interior and the section rhythm as tokens, and have `Panel.padded` and
   `.hub-section` read them, so the two numbers that define the page rhythm exist once.
3. Sweep the four hand-rolled copies of the panel interior onto `Panel.padded`.
4. Add a stylelint rule rejecting raw `rem` in `padding`/`gap` outside `theme.css`. Without step 4
   this regrows — it already has once.

### Also worth fixing while in here

`--surface-sunken` and `--text-muted` were used in `branding-tab.css` and are defined by no theme,
so they silently resolved to nothing. stylelint does not currently reject an undefined custom
property. Whatever rule lands for step 4 should cover unknown `--*` names too; that class of bug is
invisible until someone looks at the screen.
