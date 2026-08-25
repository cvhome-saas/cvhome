import {
  Component,
  ElementRef,
  Injector,
  afterNextRender,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
  viewChildren,
} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';

/** One node, as the consumer supplies it. Children are nested; the component flattens for display. */
export interface TreeNode {
  readonly id: number;
  readonly label: string;
  /** A quiet figure at the end of the row — a product count, a member count. */
  readonly meta?: string;
  /** Drawn as an eye / eye-off toggle. Omit the field to leave the column out for this row. */
  readonly visible?: boolean;
  readonly children: readonly TreeNode[];
}

let nextTreeId = 0;

/** How long a type-ahead buffer survives a pause before the next character starts a new search. */
const TYPEAHEAD_RESET_MS = 500;

/**
 * Where a drop lands.
 *
 * `'inside'` nests the node into the target. `'out'` makes it the target's *sibling*, which is how
 * a node is promoted one level — the target is then its current parent.
 *
 * There is deliberately no before/after. Ordering siblings needs `sortOrder`, and on this platform
 * that is unwritable (`PUT /private/category/{id}` fails for every caller) *and* unreadable (the
 * hierarchy does not return children in `sortOrder` order). A drop zone for it would be a gesture
 * that cannot change anything. See lessons.md, "Catalogue — sibling order is not expressible, twice
 * over".
 */
export type DropPosition = 'inside' | 'out';

export interface TreeMove {
  readonly nodeId: number;
  readonly targetId: number;
  readonly position: DropPosition;
}

/** A node in the flattened, expansion-aware order the rows are drawn in. */
interface FlatNode {
  readonly node: TreeNode;
  readonly depth: number;
  readonly parentId: number | null;
  readonly hasChildren: boolean;
  readonly expanded: boolean;
  /** Position among its siblings, and how many siblings there are — for the ARIA contract. */
  readonly index: number;
  readonly size: number;
}

/**
 * A hierarchy the operator can read, navigate and rearrange.
 *
 * Nothing in `shared/ui/` did hierarchy before this: `data-table` is flat by construction and
 * `action-list` has no depth. The category tree is the first consumer and the shape is general —
 * a labelled node, an optional figure, an optional visibility flag, and children.
 *
 * **Drag is the fast path, not the only path.** A tree that can only be rearranged by dragging is
 * a tree a keyboard cannot rearrange, and re-parenting a category is not an optional nicety. Every
 * move is therefore reachable three ways: by drag, by the row's own buttons, and from the keyboard —
 * `Alt` with the arrows to move, and `Shift+F10` for the row's action menu, which is also where the
 * shortcuts are advertised.
 *
 * `Alt+Arrow` rather than `Shift+Arrow`: shift-arrow is selection extension in the tree pattern and
 * in screen readers, and taking it would collide with anything that later wants multi-select.
 *
 * **Focus and selection are separate**, as the tree pattern requires. Arrowing moves focus and
 * nothing else; Enter or Space selects. They used to be the same thing here, which meant arrowing
 * past a category loaded it into the editor beside the tree and discarded whatever was half-typed
 * there — and it made moving a node without opening it impossible.
 *
 * The whole tree is **one tab stop**. Up and down walk the visible rows, right expands then
 * descends, left collapses then ascends, Home and End jump to the ends, and a printable character
 * type-aheads.
 *
 * **It owns no state.** Expansion, selection and the node list all come in as inputs and every
 * change goes out as an output. That is what lets the consumer keep the tree in step with a server
 * that recomputes `lineage` and `depth` on every move: the page reloads and re-renders, rather than
 * this component holding a copy that has quietly become wrong.
 */
@Component({
  selector: 'app-tree',
  imports: [Icon, TranslocoDirective],
  templateUrl: './tree.html',
  styleUrl: './tree.css',
  host: {
    '(document:click)': 'closeMenu()',
  },
})
export class Tree {
  readonly nodes = input.required<readonly TreeNode[]>();
  /** Which node is being edited beside the tree. */
  readonly selectedId = input<number | null>(null);
  /** Ids whose children are hidden. Collapsed rather than expanded, so a new tree opens fully open. */
  readonly collapsed = input<ReadonlySet<number>>(new Set<number>());
  /** Names the tree for assistive tech, e.g. "Category tree". */
  readonly label = input.required<string>();
  /** Turns off every control that writes, while a move is in flight. */
  readonly busy = input(false);
  /** Whether rows offer the eye toggle. Off for a tree whose nodes have no visibility. */
  readonly showVisibility = input(true);

  readonly selectedIdChange = output<number>();
  readonly toggleCollapsed = output<number>();
  readonly visibilityToggled = output<TreeNode>();
  readonly moved = output<TreeMove>();
  readonly addChild = output<TreeNode>();

  private readonly injector = inject(Injector);
  private readonly transloco = inject(TranslocoService);

  /**
   * Which row holds the tab stop.
   *
   * Distinct from `selectedId`, which is the record open in the editor. Falls back to the selection
   * and then to the first row, so a freshly loaded tree always has somewhere for Tab to land.
   */
  private readonly focusedId = signal<number | null>(null);

  /** Which row's action menu is open, if any. */
  protected readonly openMenuFor = signal<number | null>(null);

  /**
   * The node whose move is in flight.
   *
   * The page reloads the whole tree from the server after every move, so focus would otherwise be
   * lost on each one. This is the marker the restoring effect below watches for.
   */
  private readonly pendingFocusId = signal<number | null>(null);

  /**
   * The node list as it stood when the action fired.
   *
   * The settle check cannot key on `busy()`: this effect runs in the same tick as the emit, before
   * the page has even started the request, so it would announce "settled" against the tree still on
   * screen — and announce the node's *old* position, which is the one thing the move changed. The
   * list's identity is the honest signal, because the page replaces it wholesale when the server
   * answers.
   */
  private pendingSince: readonly TreeNode[] | null = null;

  /** What just happened, for a reader who cannot see the tree redraw. */
  protected readonly announcement = signal('');

  private typeahead = '';
  private typeaheadAt = 0;

  /** Unique per instance, so two trees on one page do not both own the same menu ids. */
  protected readonly treeId = `tree-${nextTreeId++}`;
  protected readonly hintId = `${this.treeId}-hint`;

  constructor() {
    /*
     * Puts focus back on the node that was just moved.
     *
     * The page reloads the whole tree from the server after every write, so the element that had
     * focus is gone by the time the answer lands. `@for` tracks by id, so Angular usually reuses
     * the row and focus is never actually lost — hence the `activeElement` check, which turns this
     * into a no-op in the common case rather than a redundant `focus()` that scrolls the panel.
     *
     * Gated on `busy()` so it cannot fire against the pre-move tree that is still on screen.
     */
    effect(() => {
      const id = this.pendingFocusId();
      const nodes = this.nodes();
      const rows = this.flat();
      if (id === null || nodes === this.pendingSince) {
        // The server has not answered yet; what is on screen is still the pre-move tree.
        return;
      }

      const index = rows.findIndex((row) => row.node.id === id);
      if (index === -1) {
        /*
         * Nesting a node into a collapsed parent hides it. Open the ancestors and let the changed
         * `collapsed` input re-run this. If it is genuinely gone — deleted, or the move was
         * refused — drop the marker rather than waiting forever.
         */
        const hidden = this.collapsedAncestorsOf(id);
        if (hidden.length > 0) {
          hidden.forEach((ancestorId) => this.toggleCollapsed.emit(ancestorId));
          return;
        }
        this.pendingFocusId.set(null);
        this.pendingSince = null;
        return;
      }

      this.pendingFocusId.set(null);
      this.pendingSince = null;
      this.focusedId.set(id);
      const settled = rows[index];
      afterNextRender(
        () => {
          const element = this.rows()[index]?.nativeElement;
          if (element && element !== document.activeElement) {
            element.focus();
          }
          // The result, not the intent. Position-in-set is what a move changed and what focus
          // staying put would otherwise never re-announce.
          this.announcement.set(
            this.transloco.translate('shared.tree.announce.settled', {
              name: settled.node.label,
              level: settled.depth + 1,
              position: settled.index,
              size: settled.size,
            }),
          );
        },
        {injector: this.injector},
      );
    });
  }

  /** The id being dragged, and the row it is currently over. Pointer state only — never a source of truth. */
  protected readonly dragging = signal<number | null>(null);
  protected readonly dropTarget = signal<{id: number; position: DropPosition} | null>(null);

  private readonly rows = viewChildren<ElementRef<HTMLElement>>('row');
  private readonly menuItems = viewChildren<ElementRef<HTMLButtonElement>>('menuItem');

  /**
   * The visible rows, in order.
   *
   * Recomputed from the inputs on every change rather than maintained: a move reorders and
   * re-parents at once, and a tree that patches itself drifts from the server's answer within two
   * operations.
   */
  protected readonly flat = computed<readonly FlatNode[]>(() => {
    const collapsed = this.collapsed();
    const out: FlatNode[] = [];

    const walk = (nodes: readonly TreeNode[], depth: number, parentId: number | null): void => {
      nodes.forEach((node, index) => {
        const hasChildren = node.children.length > 0;
        const expanded = hasChildren && !collapsed.has(node.id);
        out.push({node, depth, parentId, hasChildren, expanded, index: index + 1, size: nodes.length});
        if (expanded) {
          walk(node.children, depth + 1, node.id);
        }
      });
    };

    walk(this.nodes(), 0, null);
    return out;
  });

  /**
   * Which row holds the tab stop. A treeview is one stop, not one per row.
   *
   * Focus first, then the selection, then the first row — so Tab lands somewhere sensible whether
   * the operator has been navigating, has a record open, or has just arrived.
   */
  protected readonly focusedIndex = computed(() => {
    const rows = this.flat();
    const target = this.focusedId() ?? this.selectedId();
    const index = rows.findIndex((row) => row.node.id === target);
    return index === -1 ? 0 : index;
  });

  protected select(node: TreeNode): void {
    this.selectedIdChange.emit(node.id);
  }

  /* ------------------------------------------------------------------- keyboard ---- */

  protected onKeydown(event: KeyboardEvent, index: number): void {
    const rows = this.flat();
    const row = rows[index];
    if (!row || this.openMenuFor() !== null) {
      // The menu owns the keyboard while it is open.
      return;
    }

    /*
     * The write shortcuts. `stopPropagation` as well as `preventDefault`: Windows Firefox raises
     * its menu bar on a bare `Alt+letter`, and the row menu is the guaranteed path where that
     * still slips through.
     */
    if (event.altKey && !event.ctrlKey && !event.metaKey) {
      if (this.handleAction(event, row)) {
        event.preventDefault();
        event.stopPropagation();
      }
      return;
    }

    if ((event.shiftKey && event.key === 'F10') || event.key === 'ContextMenu') {
      this.openMenu(row);
      event.preventDefault();
      return;
    }

    switch (event.key) {
      case 'ArrowDown':
        this.focusRow(Math.min(index + 1, rows.length - 1));
        break;
      case 'ArrowUp':
        this.focusRow(Math.max(index - 1, 0));
        break;
      case 'Home':
        this.focusRow(0);
        break;
      case 'End':
        this.focusRow(rows.length - 1);
        break;
      /*
       * Expand-then-descend and collapse-then-ascend, which is what makes a treeview navigable
       * without ever reaching for a chevron. Swapped under RTL, where the arrows point the other
       * way through the hierarchy.
       */
      case 'ArrowRight':
        this.stepIn(event, row, index);
        break;
      case 'ArrowLeft':
        this.stepOut(event, row, index);
        break;
      case 'Enter':
      case ' ':
        this.select(row.node);
        break;
      /* Expand every sibling of the focused row — the tree pattern's one punctuation shortcut. */
      case '*':
        this.expandSiblings(row);
        break;
      default:
        // Type-ahead last, so it can never shadow a named key.
        if (event.key.length === 1 && !event.ctrlKey && !event.metaKey && event.key !== ' ') {
          this.typeAhead(event.key, index);
          break;
        }
        return;
    }
    event.preventDefault();
  }

  /**
   * The `Alt`-modified write shortcuts. Returns whether the key was one of them.
   *
   * Every one goes through the same guarded method the row's button and the menu item call, so the
   * three paths cannot drift apart.
   */
  private handleAction(event: KeyboardEvent, row: FlatNode): boolean {
    const rtl = this.isRtl(event);
    switch (event.key) {
      // No Alt+Up / Alt+Down: sibling order cannot be changed on this platform. See `DropPosition`.
      case 'ArrowRight':
        if (rtl) {
          this.unnest(row);
        } else {
          this.nest(row);
        }
        return true;
      case 'ArrowLeft':
        if (rtl) {
          this.nest(row);
        } else {
          this.unnest(row);
        }
        return true;
      default:
        break;
    }
    switch (event.key.toLowerCase()) {
      case 'v':
        this.toggleVisibility(row);
        return true;
      case 'n':
        this.requestChild(row);
        return true;
      default:
        return false;
    }
  }

  /**
   * Focuses the next row whose label starts with what has been typed.
   *
   * The buffer resets after a pause, so "sh" finds "Shoes" but a later "s" starts again. Wraps, so
   * the search has no dead end.
   */
  private typeAhead(character: string, from: number): void {
    const now = Date.now();
    this.typeahead = now - this.typeaheadAt > TYPEAHEAD_RESET_MS ? character : this.typeahead + character;
    this.typeaheadAt = now;

    const rows = this.flat();
    const needle = this.typeahead.toLowerCase();
    for (let step = 1; step <= rows.length; step += 1) {
      const index = (from + step) % rows.length;
      if (rows[index].node.label.toLowerCase().startsWith(needle)) {
        this.focusRow(index);
        return;
      }
    }
  }

  /** Opens every collapsed sibling of a row, including the row itself. */
  private expandSiblings(row: FlatNode): void {
    for (const candidate of this.flat()) {
      if (candidate.parentId === row.parentId && candidate.hasChildren && !candidate.expanded) {
        this.toggleCollapsed.emit(candidate.node.id);
      }
    }
  }

  private stepIn(event: KeyboardEvent, row: FlatNode, index: number): void {
    if (this.isRtl(event)) {
      this.collapseOrAscend(row);
      return;
    }
    this.expandOrDescend(row, index);
  }

  private stepOut(event: KeyboardEvent, row: FlatNode, index: number): void {
    if (this.isRtl(event)) {
      this.expandOrDescend(row, index);
      return;
    }
    this.collapseOrAscend(row);
  }

  private expandOrDescend(row: FlatNode, index: number): void {
    if (row.hasChildren && !row.expanded) {
      this.toggleCollapsed.emit(row.node.id);
      return;
    }
    if (row.hasChildren) {
      this.focusRow(index + 1);
    }
  }

  private collapseOrAscend(row: FlatNode): void {
    if (row.expanded) {
      this.toggleCollapsed.emit(row.node.id);
      return;
    }
    const parent = this.flat().findIndex((candidate) => candidate.node.id === row.parentId);
    if (parent !== -1) {
      this.focusRow(parent);
    }
  }

  private isRtl(event: KeyboardEvent): boolean {
    return getComputedStyle(event.currentTarget as Element).direction === 'rtl';
  }

  /**
   * Moves focus, and only focus.
   *
   * It used to emit `selectedIdChange` as well, which meant arrowing down the tree loaded each
   * category into the editor in turn and threw away anything unsaved in it. Selection is Enter and
   * Space now.
   *
   * `afterNextRender` rather than a microtask: this app runs zone change detection with
   * `eventCoalescing`, so a microtask can land *before* the tab stop has moved in the DOM and would
   * focus the row that used to hold it. `DatePicker` documents the same trap.
   */
  private focusRow(index: number): void {
    const row = this.flat()[index];
    if (!row) {
      return;
    }
    this.focusedId.set(row.node.id);
    afterNextRender(() => this.rows()[index]?.nativeElement.focus(), {injector: this.injector});
  }

  /* --------------------------------------------------------- the accessible moves ---- */

  /** Whether the row above can adopt this one. The row above is a sibling or an uncle, never a child. */
  protected canNest(row: FlatNode): boolean {
    return row.index > 1;
  }

  protected nest(row: FlatNode): void {
    const previous = this.siblingOf(row, -1);
    if (this.busy() || !previous) {
      return;
    }
    this.beginAction(row, 'nesting');
    this.moved.emit({nodeId: row.node.id, targetId: previous.id, position: 'inside'});
  }

  protected toggleVisibility(row: FlatNode): void {
    if (this.busy() || row.node.visible === undefined) {
      return;
    }
    this.beginAction(row, row.node.visible ? 'hiding' : 'showing');
    this.visibilityToggled.emit(row.node);
  }

  protected requestChild(row: FlatNode): void {
    if (this.busy()) {
      return;
    }
    this.closeMenu();
    this.addChild.emit(row.node);
  }

  /**
   * Records what is about to happen, so focus and the announcement can both survive the reload.
   *
   * The page replaces `nodes` wholesale after every write, so without the marker the operator's
   * focus lands back at the top of the tree and nothing tells a screen reader anything moved.
   */
  private beginAction(row: FlatNode, key: string): void {
    this.closeMenu();
    this.pendingSince = this.nodes();
    this.pendingFocusId.set(row.node.id);
    this.announcement.set(
      this.transloco.translate(`shared.tree.announce.${key}`, {name: row.node.label}),
    );
  }

  /** Promote out of the current parent, up to the grandparent's level. */
  protected canUnnest(row: FlatNode): boolean {
    return row.parentId !== null;
  }

  protected unnest(row: FlatNode): void {
    if (this.busy() || row.parentId === null) {
      return;
    }
    this.beginAction(row, 'unnesting');
    this.moved.emit({nodeId: row.node.id, targetId: row.parentId, position: 'out'});
  }

  /* -------------------------------------------------------------------- the row menu ---- */

  protected menuId(row: FlatNode): string {
    return `${this.treeId}-menu-${row.node.id}`;
  }

  protected toggleMenu(row: FlatNode): void {
    this.openMenuFor.update((open) => (open === row.node.id ? null : row.node.id));
  }

  protected openMenu(row: FlatNode): void {
    if (this.busy()) {
      return;
    }
    this.openMenuFor.set(row.node.id);
    afterNextRender(() => this.menuItems()[0]?.nativeElement.focus(), {injector: this.injector});
  }

  protected closeMenu(): void {
    if (this.openMenuFor() !== null) {
      this.openMenuFor.set(null);
    }
  }

  /** Closes the menu and puts focus back on the row it belongs to. */
  protected dismissMenu(): void {
    const id = this.openMenuFor();
    this.closeMenu();
    if (id === null) {
      return;
    }
    const index = this.flat().findIndex((row) => row.node.id === id);
    if (index !== -1) {
      this.focusRow(index);
    }
  }

  protected onMenuKeydown(event: KeyboardEvent): void {
    const items = this.menuItems();
    const current = items.findIndex((item) => item.nativeElement === document.activeElement);

    switch (event.key) {
      case 'ArrowDown':
      case 'ArrowUp': {
        const step = event.key === 'ArrowDown' ? 1 : -1;
        const next = ((current + step) % items.length + items.length) % items.length;
        items[next]?.nativeElement.focus();
        break;
      }
      case 'Home':
        items[0]?.nativeElement.focus();
        break;
      case 'End':
        items[items.length - 1]?.nativeElement.focus();
        break;
      case 'Escape':
      case 'Tab':
        this.dismissMenu();
        // Tab is allowed to continue: the tree is one tab stop and this leaves it.
        if (event.key === 'Escape') {
          break;
        }
        return;
      default:
        return;
    }
    event.preventDefault();
  }

  private siblingOf(row: FlatNode, offset: number): TreeNode | null {
    const siblings = row.parentId === null
      ? this.nodes()
      : (this.findNode(this.nodes(), row.parentId)?.children ?? []);
    return siblings[row.index - 1 + offset] ?? null;
  }

  /**
   * The collapsed ancestors hiding a node, outermost first.
   *
   * Walks `nodes()` rather than `flat()`, because a node inside a collapsed branch is by definition
   * not in the flattened list.
   */
  private collapsedAncestorsOf(id: number): readonly number[] {
    const trail: number[] = [];
    const walk = (nodes: readonly TreeNode[], ancestors: readonly number[]): boolean => {
      for (const node of nodes) {
        if (node.id === id) {
          trail.push(...ancestors.filter((ancestor) => this.collapsed().has(ancestor)));
          return true;
        }
        if (walk(node.children, [...ancestors, node.id])) {
          return true;
        }
      }
      return false;
    };
    walk(this.nodes(), []);
    return trail;
  }

  private findNode(nodes: readonly TreeNode[], id: number): TreeNode | null {
    for (const node of nodes) {
      if (node.id === id) {
        return node;
      }
      const found = this.findNode(node.children, id);
      if (found) {
        return found;
      }
    }
    return null;
  }

  /* ------------------------------------------------------------------------ drag ---- */

  protected onDragStart(node: TreeNode, event: DragEvent): void {
    if (this.busy()) {
      event.preventDefault();
      return;
    }
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
      // Firefox will not start a drag at all unless something is written to the transfer.
      event.dataTransfer.setData('text/plain', String(node.id));
    }
    this.dragging.set(node.id);
  }

  /** Whether the row under the pointer is a legal parent for what is being dragged. */
  protected onDragOver(node: TreeNode, event: DragEvent): void {
    const dragged = this.dragging();
    if (dragged === null || dragged === node.id || this.contains(dragged, node.id)) {
      return;
    }
    event.preventDefault();

    // The whole row is one target. There is no edge zone, because there is no reordering to
    // express — see `DropPosition`.
    if (this.dropTarget()?.id !== node.id) {
      this.dropTarget.set({id: node.id, position: 'inside'});
    }
  }

  protected onDrop(event: DragEvent): void {
    event.preventDefault();
    const dragged = this.dragging();
    const target = this.dropTarget();
    this.endDrag();
    if (dragged !== null && target) {
      this.moved.emit({nodeId: dragged, targetId: target.id, position: target.position});
    }
  }

  protected endDrag(): void {
    this.dragging.set(null);
    this.dropTarget.set(null);
  }

  protected dropMode(node: TreeNode): DropPosition | null {
    const target = this.dropTarget();
    return target && target.id === node.id ? target.position : null;
  }

  /** Whether `ancestorId`'s subtree holds `id` — a node cannot be dropped inside itself. */
  private contains(ancestorId: number, id: number): boolean {
    const ancestor = this.findNode(this.nodes(), ancestorId);
    return ancestor ? this.findNode(ancestor.children, id) !== null : false;
  }
}
