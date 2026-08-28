# Todos

Work that is known, agreed and not yet done. One heading per item, newest first. Something that is
*done* belongs in `lessons.md` (what we learned) or in the code; this file is only for what is still
owed.

---

## Spacing off the scale, in 682 declarations

**Status:** open · **Cost:** ~2 days · **Blast radius:** every stylesheet in `src/app`

### Where this stands

The scale itself exists now. `styles/theme.css` carries six steps named for the job each one does —
`--spacing-inline` (0.25rem) through `--spacing-gutter` (1.5rem) — plus `--panel-interior`, the
console's card padding, as one value. 431 declarations that already sat on those steps were
converted, and `.stylelintrc.json` errors on any of the six typed as a literal, so *that* half
cannot regrow. `npm run lint` is finally wired into `gradle check`, which it never was.

What is left is the tail: **682 padding, gap and margin declarations on values the scale does not
have.** They are warnings, not errors — run `npm run lint:css:debt` to see them.

```
0.6rem  x82    0.85rem x75    0.35rem x73    0.7rem x56    0.4rem x54    0.15rem x52
```

Worst files: `marketing.css` (76), `order-details.css` (48), `create-store.css` (38),
`first-run.css` (23), `console-toolbar.css` (21).

### Why it was not finished in the same pass

Every one of these has to move to be tokenised — `0.85rem` is not `0.75rem`. Snapping the tail to
the nearest step moves at most 0.1rem (1.6px) per declaration, which is invisible in isolation, but
it is a visual change on every screen in the console and it cannot be proved safe the way the
first half was. The 431 already-on-scale conversions were verified by substituting the tokens back
and diffing against `HEAD`: 114 stylesheets, zero changed values. There is no equivalent proof for
a snap, only a review.

So it is a deliberate deferral, not an oversight. It needs someone with the screens in front of
them, probably a file or two at a time, in both Forest and Daylight.

### Two smaller things in the same territory

- **`.panel-pad` is still two different measurements.** `order-details.css` defines it as
  `1.25rem` and `create-store.css` as `1.5rem`, and neither is `--panel-interior`
  (`1.25rem 1.5rem`), which is what a panel body actually uses. 13 templates reference the class.
  This is the exact drift `Panel.padded` was introduced to end, and `panel.ts`'s doc comment still
  describes it in the past tense. Fixing it changes padding on those pages, which is why it is
  here and not in the last commit.
- **Three panel-lookalike cards.** `create-store.css` `.progress-head`, `first-run.css`
  `.progress-head` and `.help` build a bordered `--muted` card by hand at `--radius-2xl`, where
  `app-panel` is `--radius-xl`. They read the spacing tokens now, so they no longer *drift*, but
  they are still a fourth copy of a component that exists.
