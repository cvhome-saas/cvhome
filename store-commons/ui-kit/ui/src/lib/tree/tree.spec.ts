import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';

import {kitTranslocoTesting} from '@cvhome-saas/ui-kit/i18n';
import {Tree, type TreeMove, type TreeNode} from './tree';

function node(id: number, label: string, children: TreeNode[] = [], visible = true): TreeNode {
  return {id, label, meta: String(id), visible, children};
}

/**
 * Electronics
 *   Audio
 *   Displays
 * Furniture
 */
const TREE: readonly TreeNode[] = [
  node(1, 'Electronics', [node(11, 'Audio'), node(12, 'Displays')]),
  node(2, 'Furniture'),
];

/**
 * A host, because the tree is fully controlled: it renders what it is given and reports what the
 * operator did. Half these cases are about what it does *not* do on its own.
 */
@Component({
  imports: [Tree],
  template: `
    <app-tree
      [nodes]="nodes()"
      [selectedId]="selectedId()"
      [collapsed]="collapsed()"
      [busy]="busy()"
      label="Category tree"
      itemNoun="category"
      (selectedIdChange)="selected.push($event)"
      (toggleCollapsed)="toggled.push($event)"
      (visibilityToggled)="visibility.push($event.id)"
      (moved)="moves.push($event)"
      (addChild)="added.push($event.id)"
    />
  `,
})
class Host {
  readonly nodes = signal<readonly TreeNode[]>(TREE);
  readonly selectedId = signal<number | null>(null);
  readonly collapsed = signal<ReadonlySet<number>>(new Set<number>());
  readonly busy = signal(false);

  readonly selected: number[] = [];
  readonly toggled: number[] = [];
  readonly visibility: number[] = [];
  readonly moves: TreeMove[] = [];
  readonly added: number[] = [];
}

describe('Tree', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Host, ...kitTranslocoTesting().imports],
      providers: [...kitTranslocoTesting().providers],
    }).compileComponents();

    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  function rows(): HTMLElement[] {
    return [...element.querySelectorAll<HTMLElement>('.tree-row')];
  }

  function labelOf(row: Element | null): string | null {
    return row?.querySelector('.node-label')?.textContent?.trim() ?? null;
  }

  function press(row: Element, key: string, options: KeyboardEventInit = {}): void {
    row.dispatchEvent(new KeyboardEvent('keydown', {key, bubbles: true, cancelable: true, ...options}));
    fixture.detectChanges();
  }

  it('renders the visible rows depth-first with the hierarchy in ARIA', () => {
    expect(rows().map(labelOf)).toEqual(['Electronics', 'Audio', 'Displays', 'Furniture']);
    expect(rows().map((row) => row.getAttribute('aria-level'))).toEqual(['1', '2', '2', '1']);
    expect(rows()[0].getAttribute('aria-posinset')).toBe('1');
    expect(rows()[0].getAttribute('aria-setsize')).toBe('2');
  });

  it('is one tab stop however many rows it has', () => {
    expect(rows().filter((row) => row.getAttribute('tabindex') === '0').length).toBe(1);
  });

  it('hides a collapsed branch and says it is collapsed', () => {
    host.collapsed.set(new Set([1]));
    fixture.detectChanges();

    expect(rows().map(labelOf)).toEqual(['Electronics', 'Furniture']);
    expect(rows()[0].getAttribute('aria-expanded')).toBe('false');
  });

  it('does not claim a leaf can expand', () => {
    expect(rows()[3].getAttribute('aria-expanded')).toBeNull();
  });

  /* ------------------------------------------------------------------- navigation ---- */

  it('moves focus with the arrows without selecting anything', fakeAsync(() => {
    /*
     * The bug this guards: `focusRow` used to emit `selectedIdChange`, so arrowing past a category
     * loaded it into the editor beside the tree and discarded whatever was half-typed there.
     */
    press(rows()[0], 'ArrowDown');
    tick();
    fixture.detectChanges();

    expect(host.selected).toEqual([]);
    expect(rows().filter((row) => row.getAttribute('tabindex') === '0').map(labelOf)).toEqual(['Audio']);
  }));

  it('selects on Enter and on Space, and only then', fakeAsync(() => {
    press(rows()[1], 'Enter');
    press(rows()[1], ' ');
    tick();

    expect(host.selected).toEqual([11, 11]);
  }));

  it('walks to the ends with Home and End', fakeAsync(() => {
    press(rows()[0], 'End');
    tick();
    fixture.detectChanges();
    expect(rows().filter((row) => row.getAttribute('tabindex') === '0').map(labelOf)).toEqual(['Furniture']);
  }));

  it('expands then descends on Right, collapses then ascends on Left', fakeAsync(() => {
    host.collapsed.set(new Set([1]));
    fixture.detectChanges();

    // Collapsed: the first Right opens it rather than moving.
    press(rows()[0], 'ArrowRight');
    tick();
    expect(host.toggled).toEqual([1]);

    host.collapsed.set(new Set());
    fixture.detectChanges();

    // Expanded: now it descends.
    press(rows()[0], 'ArrowRight');
    tick();
    fixture.detectChanges();
    expect(rows().filter((row) => row.getAttribute('tabindex') === '0').map(labelOf)).toEqual(['Audio']);

    // On a leaf, Left goes back up to the parent rather than collapsing anything.
    press(rows()[1], 'ArrowLeft');
    tick();
    fixture.detectChanges();
    expect(rows().filter((row) => row.getAttribute('tabindex') === '0').map(labelOf)).toEqual(['Electronics']);
  }));

  it('type-aheads to the next row that starts with what was typed', fakeAsync(() => {
    press(rows()[0], 'f');
    tick();
    fixture.detectChanges();

    expect(rows().filter((row) => row.getAttribute('tabindex') === '0').map(labelOf)).toEqual(['Furniture']);
  }));

  /* ---------------------------------------------------------------------- actions ---- */

  it('gives every mouse action a keyboard path', fakeAsync(() => {
    // Displays has Audio before it, so it can be nested.
    press(rows()[2], 'ArrowRight', {altKey: true});
    tick();
    expect(host.moves).toEqual([{nodeId: 12, targetId: 11, position: 'inside'}]);

    press(rows()[1], 'ArrowLeft', {altKey: true});
    tick();
    expect(host.moves[1]).toEqual({nodeId: 11, targetId: 1, position: 'out'});

    press(rows()[1], 'v', {altKey: true});
    tick();
    expect(host.visibility).toEqual([11]);

    press(rows()[1], 'n', {altKey: true});
    tick();
    expect(host.added).toEqual([11]);
  }));

  it('swaps the nesting arrows under RTL', fakeAsync(() => {
    element.querySelector('.tree')!.setAttribute('dir', 'rtl');
    fixture.detectChanges();

    press(rows()[2], 'ArrowLeft', {altKey: true});
    tick();

    // Left nests in a right-to-left tree, because that is the direction "further in" points.
    expect(host.moves).toEqual([{nodeId: 12, targetId: 11, position: 'inside'}]);
  }));

  it('offers no sibling reordering unless the consumer asks for it', fakeAsync(() => {
    press(rows()[2], 'ArrowUp', {altKey: true});
    press(rows()[1], 'ArrowDown', {altKey: true});
    tick();
    fixture.detectChanges();

    expect(host.moves).toEqual([]);
    // Nor are the buttons drawn, so nothing advertises a move that cannot happen.
    expect(element.querySelectorAll('[title="Move up"]').length).toBe(0);
  }));

  it('refuses to nest a first child, which has nothing before it', fakeAsync(() => {
    press(rows()[1], 'ArrowRight', {altKey: true});
    tick();

    expect(host.moves).toEqual([]);
  }));

  it('opens the row menu on Shift+F10 and lists every action with its shortcut', fakeAsync(() => {
    press(rows()[1], 'F10', {shiftKey: true});
    tick();
    fixture.detectChanges();

    const menu = element.querySelector('.tree-row-menu');
    expect(menu).not.toBeNull();
    expect(menu!.getAttribute('role')).toBe('menu');
    // Disabled items stay focusable and countable, so an unavailable action does not vanish.
    expect(menu!.querySelectorAll('[role^="menuitem"]').length).toBeGreaterThan(0);
    expect(menu!.querySelectorAll('kbd').length).toBeGreaterThan(0);
    expect(menu!.querySelector('[disabled]')).toBeNull();
  }));

  it('does nothing at all while a write is in flight', fakeAsync(() => {
    host.busy.set(true);
    fixture.detectChanges();

    press(rows()[2], 'ArrowRight', {altKey: true});
    press(rows()[1], 'v', {altKey: true});
    tick();

    expect(host.moves).toEqual([]);
    expect(host.visibility).toEqual([]);
    expect(element.querySelector('.tree')?.getAttribute('aria-busy')).toBe('true');
  }));

  /* ------------------------------------------------------------ focus restoration ---- */

  it('puts focus back on the node it moved once the server answers', fakeAsync(() => {
    press(rows()[2], 'ArrowRight', {altKey: true});
    tick();

    // The page reloads and hands back a new list — Displays is now inside Audio.
    host.nodes.set([
      node(1, 'Electronics', [node(11, 'Audio', [node(12, 'Displays')])]),
      node(2, 'Furniture'),
    ]);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(rows().filter((row) => row.getAttribute('tabindex') === '0').map(labelOf)).toEqual(['Displays']);
  }));

  it('announces what settled, including where the node ended up', fakeAsync(() => {
    press(rows()[2], 'ArrowRight', {altKey: true});
    tick();

    host.nodes.set([
      node(1, 'Electronics', [node(11, 'Audio', [node(12, 'Displays')])]),
      node(2, 'Furniture'),
    ]);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    const live = element.querySelector('p[role="status"]');
    expect(live?.getAttribute('aria-live')).toBe('polite');
    // Position-in-set is exactly what the move changed and what focus staying put never re-announces.
    expect(live?.textContent).toContain('Displays');
    expect(live?.textContent).toContain('3');
  }));

  it('shows a warning badge only on the rows that carry one', () => {
    host.nodes.set([
      {...node(1, 'Electronics'), warn: 'Broken link'},
      node(2, 'Furniture'),
    ]);
    fixture.detectChanges();

    const badges = Array.from(element.querySelectorAll('app-badge'));
    expect(badges.length).toBe(1);
    expect(badges[0].textContent).toContain('Broken link');
  });

  /* ------------------------------------------------------------------------- drag ---- */

  it('refuses a drop onto a node inside the one being dragged', () => {
    const dragged = rows()[0];
    dragged.dispatchEvent(new DragEvent('dragstart', {bubbles: true}));
    fixture.detectChanges();

    const descendant = rows()[1];
    const over = new DragEvent('dragover', {bubbles: true, cancelable: true});
    descendant.dispatchEvent(over);
    fixture.detectChanges();

    // Not prevented means not a drop target: a category cannot be moved inside itself.
    expect(over.defaultPrevented).toBe(false);
    expect(descendant.className).not.toContain('drop-inside');
  });
});

/**
 * The same primitive with the two inputs a menu sets. Its own host, because `reorderable` and
 * `maxDepth` change what is drawn rather than only what is emitted — and the point of both is that
 * the catalogue's tree above is untouched by them.
 */
@Component({
  imports: [Tree],
  template: `
    <app-tree
      [nodes]="nodes()"
      label="Menu tree"
      itemNoun="link"
      reorderable
      [maxDepth]="2"
      (moved)="moves.push($event)"
    />
  `,
})
class ReorderHost {
  readonly nodes = signal<readonly TreeNode[]>(TREE);
  readonly moves: TreeMove[] = [];
}

describe('Tree, reorderable', () => {
  let fixture: ComponentFixture<ReorderHost>;
  let host: ReorderHost;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReorderHost, ...kitTranslocoTesting().imports],
      providers: [...kitTranslocoTesting().providers],
    }).compileComponents();

    fixture = TestBed.createComponent(ReorderHost);
    host = fixture.componentInstance;
    element = fixture.nativeElement as HTMLElement;
    fixture.detectChanges();
  });

  const rows = (): HTMLElement[] => Array.from(element.querySelectorAll<HTMLElement>('.tree-row'));

  const press = (row: HTMLElement, key: string, init: KeyboardEventInit = {}): void => {
    row.dispatchEvent(new KeyboardEvent('keydown', {key, bubbles: true, ...init}));
  };

  it('moves a node among its siblings with Alt and the vertical arrows', fakeAsync(() => {
    // Displays sits after Audio, so it can go up; Audio is first, so it can go down.
    press(rows()[2], 'ArrowUp', {altKey: true});
    tick();
    expect(host.moves).toEqual([{nodeId: 12, targetId: 11, position: 'before'}]);

    press(rows()[1], 'ArrowDown', {altKey: true});
    tick();
    expect(host.moves[1]).toEqual({nodeId: 11, targetId: 12, position: 'after'});
  }));

  it('does not swap the vertical arrows under RTL — up is up either way', fakeAsync(() => {
    element.querySelector('.tree')!.setAttribute('dir', 'rtl');
    fixture.detectChanges();

    press(rows()[2], 'ArrowUp', {altKey: true});
    tick();

    expect(host.moves).toEqual([{nodeId: 12, targetId: 11, position: 'before'}]);
  }));

  it('refuses to move the first sibling up or the last one down', fakeAsync(() => {
    press(rows()[1], 'ArrowUp', {altKey: true});
    press(rows()[2], 'ArrowDown', {altKey: true});
    tick();

    expect(host.moves).toEqual([]);
  }));

  it('draws the vertical pair on the row and in its menu', fakeAsync(() => {
    press(rows()[2], 'F10', {shiftKey: true});
    tick();
    fixture.detectChanges();

    expect(element.querySelectorAll('[title="Move up"]').length).toBeGreaterThan(0);
    const shortcuts = Array.from(element.querySelectorAll('.tree-row-menu kbd')).map((k) => k.textContent);
    expect(shortcuts).toContain('Alt+\u2191');
    expect(shortcuts).toContain('Alt+\u2193');
  }));

  it('refuses a nest that would push the subtree past maxDepth', fakeAsync(() => {
    // Electronics has children, so nesting it under Furniture would make a third level. It is the
    // second root, so `canNest` would otherwise allow it.
    host.nodes.set([node(2, 'Furniture'), node(1, 'Electronics', [node(11, 'Audio')])]);
    fixture.detectChanges();

    press(rows()[1], 'ArrowRight', {altKey: true});
    tick();

    expect(host.moves).toEqual([]);
  }));

  it('still nests a childless node, which fits', fakeAsync(() => {
    host.nodes.set([node(2, 'Furniture'), node(1, 'Electronics')]);
    fixture.detectChanges();

    press(rows()[1], 'ArrowRight', {altKey: true});
    tick();

    expect(host.moves).toEqual([{nodeId: 1, targetId: 2, position: 'inside'}]);
  }));
});
