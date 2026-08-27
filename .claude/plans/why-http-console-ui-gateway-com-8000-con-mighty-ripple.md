# Align Storefront navigation with the category tree

## Context

`/content/menus` and `/catalogue/categories` both edit a hierarchy, but they were built at
different times and look nothing alike.

**Why menus feels harder.** `menus-tab.html` renders every link as a fully-expanded form row —
label field + target-kind select + target-value field + up + down + indent/outdent + eye + trash —
and does it for **both** menus at once in a two-column grid. A store with 8 main links and 6 footer
links puts ~14 rows × ~4 controls × 2 columns on screen at once. Nothing is progressive: every
field of every link competes for attention even when you only wanted to reorder two items. The
hierarchy itself is the weakest signal in the row — depth 1 is a CSS indent + tint on a flat `<ol>`,
with no chevron, no collapse, no `role="tree"`, and a purely decorative `aria-hidden` grip that
suggests a drag gesture that does not exist.

**Why categories feels easy.** `category-tab.html` splits it in two: a tree that shows *only*
shape (label, count, eye) and an editor panel that shows *only* the open record. The tree is the
shared `app-tree` primitive in `shared/ui/tree/` — flattened `role="tree"`, one tab stop, collapse,
drag, `Alt+Arrow` moves, a `Shift+F10` row menu that advertises the shortcuts, and an `aria-live`
announcement after each move. Menus reimplements a worse version of all of that locally.

The outcome: menus adopts the same split, and `app-tree` grows the two things menus needs that
categories genuinely cannot have.

## Decisions taken

- **Layout** — a Main / Footer tab strip above **one** split (tree left, item editor right), so
  each menu gets full width. Not two trees side by side.
- **Reordering** — added to `app-tree` behind a `reorderable` input, off by default. Menus turn it
  on; catalogue stays exactly as it is today, and the reason it must
  (`lessons.md` → *"Catalogue — sibling order is not expressible, twice over"*) stays true.
- **No Apply button.** Editor fields commit into the local draft on change, as they do today; the
  only commit is **Save menu** (the API is a whole-tree `PUT /menus/{handle}`). The mock's `[Apply]`
  is dropped as a second, misleading save.

---

## Part 1 — generalise `app-tree`

Files: `src/app/shared/ui/tree/tree.ts`, `tree.html`, `tree.css`, `tree.spec.ts`

### 1a. Node identity widens to `string | number`

Menu draft items are keyed by a local `key: string` (`MenuDraftItem.key`, `menus.facade.ts:16`)
because an unsaved link has no server `id` yet.

```ts
export type TreeNodeId = string | number;

export interface TreeNode {
  readonly id: TreeNodeId;
  …
  /** An amber badge at the row end — a broken target, a validation warning. */
  readonly warn?: string;
}
```

Widen alongside it: `TreeMove.nodeId` / `.targetId`, the `collapsed` input, and the
`selectedIdChange` / `toggleCollapsed` outputs. Everything internal already treats ids as opaque.

Consumer fallout is two narrowing casts:
- `category-tab.ts:96` `onSelect(id: TreeNodeId)` → `this.facade.select('categories', Number(id))`
- `catalogue.facade.ts` `toggleCollapsed` / `moveCategory` — accept `TreeNodeId`, coerce with
  `Number()` at the boundary. `ReadonlySet<number>` stays assignable to `ReadonlySet<TreeNodeId>`.

### 1b. `reorderable` — sibling order

```ts
export type DropPosition = 'inside' | 'out' | 'before' | 'after';

/** Whether siblings can be reordered. Off where the platform cannot express order. */
readonly reorderable = input(false);
```

Rewrite `DropPosition`'s doc comment: `'before'`/`'after'` exist but are only reachable when the
consumer opts in, and the catalogue's two blockers are why it does not.

When `reorderable()`:
- **Buttons** — `arrowUp` / `arrowDown` join `.row-moves`, before nest/unnest. `canMoveUp(row)` is
  `row.index > 1`; `canMoveDown(row)` is `row.index < row.size`. They emit
  `{nodeId, targetId: <sibling>.id, position: 'before' | 'after'}` through `beginAction()`, so
  focus restoration and the announcement work unchanged.
- **Keyboard** — `Alt+↑` / `Alt+↓` in `handleAction()`. Not RTL-swapped (vertical order does not
  mirror). When `reorderable()` is false they fall through unhandled, exactly as today.
- **Row menu** — two more `role="menuitem"`s with `<kbd>Alt+↑</kbd>` / `<kbd>Alt+↓</kbd>`, then the
  existing `<hr />`.
- **Drag** — `onDragOver` splits the row by pointer Y: top 25% `before`, bottom 25% `after`, middle
  `inside`. `tree.css` gains `.drop-before` / `.drop-after` insertion lines (a 2px
  `--color-accent` rule on the block edge) beside the existing `.drop-inside`. When not
  reorderable the whole row stays one `inside` target, as now.
- **Announcements** — `shared.tree.announce.movingUp` / `movingDown`; `settled` already reports
  position-in-set, which is the thing a reorder changes.

### 1c. `maxDepth` — the server's one-level rule

```ts
/** Deepest level a node may sit at, 1-based. Blocks nests that would exceed it. */
readonly maxDepth = input(Number.POSITIVE_INFINITY);
```

`canNest(row)` additionally requires `row.depth + 2 + heightOf(row.node) <= maxDepth + 1` — i.e.
the moved subtree must still fit. With `maxDepth = 2` this reproduces today's menu rule (the
template's `[disabled]="row.item.children.length > 0"` on indent) without the consumer restating
it. `onDragOver` applies the same test before offering `inside`.

### 1d. The primitive stops saying "category"

`shared.tree.*` in `en.json` / `ar.json` is worded for its one consumer — *"Add a **category**
inside {name}"*, *"Shown in the **storefront navigation**"*. Add

```ts
/** The translated singular for a row, e.g. "category", "link". */
readonly itemNoun = input.required<string>();
```

and interpolate `{noun}` into `addChild`, `keyboardHint`, `toggleVisibility`, `visible`, `hidden`.
New keys `catalogue.categories.noun` ("category" / "تصنيف") and `content.menus.noun`
("link" / "رابط") feed it.

### 1e. `warn` badge

Render `@if (row.node.warn)` as `<app-badge tone="amber" shape="square">` after `.node-meta`
(`tree.ts` imports `Badge`). This is where the menus screen's existing "Broken link" badge lands.

---

## Part 2 — the menus screen

### `menus.facade.ts` — move tree logic out of the component

The component currently owns `move` / `indent` / `outdent` / `remove` / `add` / `clone` / `find` /
`listOf` (`menus-tab.ts:92-244`). Those become facade methods so they are unit-testable and so the
component is presentation only:

- `applyMove(handle, move: TreeMove)` — one function replacing `move`+`indent`+`outdent`. Clone,
  detach the node from its current list, then re-attach per `position`:
  `inside` → append to target's `children`; `out` → splice into the target's parent list right
  after the target; `before` / `after` → splice into the target's own list. Enforce depth 2 by
  refusing an `inside` whose subject has children. Then `update(handle, items)`.
- `addItem(handle): string` — appends `blankItem()`, returns its `key` so the caller can select it.
- `removeItem(handle, key)` — today's `remove`, and it must decide the fate of children. Delete the
  whole subtree (matching the catalogue's cascade), warned in the editor before the click.
- `setField(handle, key, patch: Partial<MenuDraftItem>)` — one commit path replacing `setLabel` /
  `setKind` / `setValue` / `toggleVisible`. `setKind`'s value normalisation
  (`menus-tab.ts:113-128`) moves here intact.
- `selectedKey = signal<string | null>(null)`, plus `selected = computed(...)`. Reset when the
  handle changes or the resource reloads with the key gone.

Untouched: `resource`, `pagesResource`, `save`, `discard`, `dirty`, `toDraft` / `toWire`.

### `menus-tab.ts` / `.html` / `.css` — the split

Mirrors `category-tab` structurally. `styleUrls` picks up
`shared/styles/field.css` for `.split`, and `../../../catalogue/components/editor-card.css` is the
existing editor vocabulary — if reaching across features is unwanted, lift `editor-card.css` to
`shared/styles/` in the same change rather than copying it.

**Header** — `app-tab-switcher` (already used by `catalogue.html`) for Main / Footer, bound to
`handle = signal<MenuHandle>('MAIN')`, each tab carrying the item count and an unsaved dot from
`facade.dirty()`.

**Left panel** — Expand all / Collapse all in `panelAction`, then:

```html
<app-tree
  [nodes]="treeNodes()" [selectedId]="facade.selectedKey()" [collapsed]="collapsed()"
  [label]="t('content.menus.treeLabel')" [itemNoun]="t('content.menus.noun')"
  [busy]="facade.busy()" reorderable [maxDepth]="2"
  (selectedIdChange)="facade.selectedKey.set($any($event))"
  (toggleCollapsed)="toggleCollapsed($event)"
  (visibilityToggled)="onToggleVisible($event)"
  (moved)="facade.applyMove(handle(), $event)"
  (addChild)="onAddChild($event)" />
<button class="secondary-action add-root" (click)="onAdd()">Add link</button>
<p class="tree-hint">{{ t('content.menus.dragHint') }}</p>
```

`treeNodes` is a `computed` over `facade.drafts()[handle()]`, mapping each `MenuDraftItem` to
`{id: key, label: labels[activeLanguage()] || t('content.menus.untitled'), meta: targetPreview(item),
visible, warn: broken ? t('content.menus.broken') : undefined, children}`. `targetPreview` renders
the resolved path (`/blog`, `/help`, the URL, the page slug) — the quiet figure the tree row already
has a slot for, so the target is still visible without opening the item.

**Right panel** — `app-empty-state icon="sitemap"` when nothing is selected; otherwise, in the
`category-tab` order:

1. `.editor-head` — `app-locale-switcher` (`[filled]` from today's `filled()` computed, which
   already scans every label in every draft) and a Delete `danger-action`.
2. `app-notice-bar tone="amber"` when the selected link has children, saying how many go with it.
3. Label field for the active language (`app-form-field` + `app-text-field`, `maxLength 80`).
4. `hr.divider`, then `.field-grid`: target kind `app-select`; target value — `app-select` of
   published pages for `PAGE`, the read-only `/blog` / `/help` for the index kinds, else
   `app-text-field latin mono` (the `needsValue` branch of `menus-tab.html:56-64`, unchanged);
   an **Open in new tab** `app-toggle`; a **Visible** `app-toggle`.
   `openInNewTab` is on `MenuItem` and round-trips through `toDraft`/`toWire` today but has **no
   control anywhere in the console** — the editor panel is where it finally gets one.
5. No editor-level action row.

**Footer** — one action bar under the split: Discard (when dirty) and Save menu, wired to
`facade.discard(handle())` / `facade.save(handle())`, `[disabled]="!facade.dirty()[handle()]"`.

`app-busy-overlay` + `app-load-error` wrap the whole thing as they do now.

### Locale — `en.json` / `ar.json`

Add `content.menus`: `noun`, `treeLabel`, `dragHint`, `expandAll`, `collapseAll`, `untitled`,
`noneTitle`, `newLabel`, `hasChildren` (ICU plural), `openInNewTab` + hint, `visibleOn`/`visibleOff`,
`delete`, `unsaved`.
Retire: `moveUp`, `moveDown`, `indent`, `outdent`, `hide`, `show`, `remove`, `labelPlaceholder`
(`npm run lint:i18n` fails on unused keys).
Add `shared.tree`: `moveUp`, `moveDown`, `moveUpShort`, `moveDownShort`, `keys.moveUp`,
`keys.moveDown`, `announce.movingUp`, `announce.movingDown`; reword the five keys in 1d for `{noun}`.

### `lessons.md`

- *"Content — drag-and-drop reorder is up/down buttons"* (line 3240) claims *"there is no
  drag-and-drop primitive in `shared/ui/`"*. `shared/ui/tree` is one. Narrow the entry to the FAQ
  editor, which still has no tree, and note menus now uses it.
- The class doc on `MenusTab` citing that lesson for *"Arrow buttons rather than drag-and-drop"*
  must be rewritten. `npm run lint:lessons` checks these citations, so a stale one fails the build.

---

## Verification

1. `npm run lint && npm run test:ci` in `store-core/console-ui`. Extend `tree.spec.ts` with the new
   surface: string ids, `reorderable` off ⇒ no up/down buttons and `Alt+↑` inert, on ⇒ correct
   `TreeMove` payloads for before/after; `maxDepth` blocking a nest. Add a `menus.facade.spec.ts`
   covering `applyMove` for all four positions and the depth-2 refusal.
2. `npm run lint:i18n` and `lint:i18n-missing` for the key add/retire, and `lint:lessons`.
3. Run the stack (`lcl`) and open `http://console-ui.gateway.com:8000/content/menus`:
   - nest / unnest / reorder by drag, by row button, and by `Alt+Arrow`; `Shift+F10` lists all six
     moves with shortcuts; a nest that would reach depth 3 is disabled.
   - edit a link in the right panel, switch Main ↔ Footer and back — the unsaved draft survives
     (`drafts` is per-handle), Save toasts, Discard restores only that menu.
   - arrow through the tree with a half-typed label in the editor: focus moves, the editor does not
     reload (focus ≠ selection).
   - switch the console to Arabic — RTL indent, `Alt+←/→` swap, `Alt+↑/↓` do not.
4. `/catalogue/categories` must be byte-for-byte unchanged in behaviour: no up/down buttons, no
   before/after drop zones, drag still always nests.
5. `qa/content-platform.md` § **MNU — Navigation menus** (line 457) describes the old row editor;
   update its steps to the split.
