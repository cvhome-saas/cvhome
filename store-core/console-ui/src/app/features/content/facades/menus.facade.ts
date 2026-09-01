import {Injectable, computed, effect, inject, signal, untracked} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';
import {forkJoin, map} from 'rxjs';

import {ContentCache} from '@api/content/content-cache';
import {ContentItemsService} from '@api/content/content-items.service';
import {MenusService} from '@api/content/menus.service';
import {ApiErrorService} from '@cvhome-saas/ui-kit';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {ContentRow, Menu, MenuHandle, MenuItem, MenuTargetKind} from '@models/content';
import type {TreeMove} from '@cvhome-saas/ui-kit/ui';
import {ToastService} from '@cvhome-saas/ui-kit/ui';

/** A menu node as the editor holds it: a stable local key, mutable fields, one level of children. */
export interface MenuDraftItem {
  readonly key: string;
  id?: number;
  labels: Record<string, string>;
  kind: MenuTargetKind;
  value: string;
  openInNewTab: boolean;
  visible: boolean;
  broken: boolean;
  children: MenuDraftItem[];
}

let nextKey = 1;

/**
 * How deep a menu may go, in levels.
 *
 * The content service refuses a third level outright
 * (`ContentPlatformIntegrationTest.menusBootstrapFromLegacyPagesAndRefuseDepth`), so the editor
 * refuses it too — in the tree's affordances, and again here, because a drag is not the only way a
 * move can arrive.
 */
export const MENU_MAX_DEPTH = 2;

function toDraft(item: MenuItem): MenuDraftItem {
  return {
    key: `k${nextKey++}`,
    id: item.id,
    labels: {...item.labels},
    kind: item.target.kind,
    value: item.target.value ?? '',
    openInNewTab: !!item.openInNewTab,
    visible: item.visible !== false,
    broken: !!item.target.broken,
    children: item.children.map(toDraft),
  };
}

function toWire(item: MenuDraftItem, position: number): MenuItem {
  return {
    id: item.id,
    position,
    labels: item.labels,
    target: {kind: item.kind, value: item.value.trim() || null},
    openInNewTab: item.openInNewTab,
    visible: item.visible,
    children: item.children.map((child, index) => toWire(child, index)),
  };
}

/** Whether a target kind carries a value at all. The two index kinds resolve to fixed paths. */
export function targetNeedsValue(kind: MenuTargetKind): boolean {
  return kind !== 'BLOG_INDEX' && kind !== 'FAQ_INDEX';
}

/**
 * A deep copy of a whole menu, to any depth.
 *
 * Every edit clones before it mutates, so a half-applied change can never be observed and the
 * signal's identity always tells the truth about whether anything moved. The old component-side
 * `clone()` copied exactly two levels; this one recurses, so it stays correct if the server ever
 * allows a third.
 */
function cloneAll(items: readonly MenuDraftItem[]): MenuDraftItem[] {
  return items.map((item) => ({...item, labels: {...item.labels}, children: cloneAll(item.children)}));
}

function findItem(items: readonly MenuDraftItem[], key: string): MenuDraftItem | undefined {
  for (const item of items) {
    if (item.key === key) {
      return item;
    }
    const found = findItem(item.children, key);
    if (found) {
      return found;
    }
  }
  return undefined;
}

/** The array `key` sits in, so a caller can splice beside it. */
function listHolding(items: MenuDraftItem[], key: string): MenuDraftItem[] | null {
  if (items.some((item) => item.key === key)) {
    return items;
  }
  for (const item of items) {
    const found = listHolding(item.children, key);
    if (found) {
      return found;
    }
  }
  return null;
}

/** How far down `key` sits, counting the top level as 0. `null` when it is not in the tree. */
function depthOf(items: readonly MenuDraftItem[], key: string, depth = 0): number | null {
  for (const item of items) {
    if (item.key === key) {
      return depth;
    }
    const found = depthOf(item.children, key, depth + 1);
    if (found !== null) {
      return found;
    }
  }
  return null;
}

/**
 * Where `key` sits, as a trail of sibling indices.
 *
 * `toDraft` mints fresh keys every time the server answers, so the key an operator had open is
 * gone the instant a save lands. Position survives, and position is what they were looking at.
 */
function pathOf(items: readonly MenuDraftItem[], key: string): number[] | null {
  for (const [index, item] of items.entries()) {
    if (item.key === key) {
      return [index];
    }
    const deeper = pathOf(item.children, key);
    if (deeper) {
      return [index, ...deeper];
    }
  }
  return null;
}

/** The link at a trail of sibling indices, if the tree still has one there. */
function atPath(items: readonly MenuDraftItem[], path: readonly number[]): MenuDraftItem | null {
  let list = items;
  let found: MenuDraftItem | undefined;
  for (const index of path) {
    found = list[index];
    if (!found) {
      return null;
    }
    list = found.children;
  }
  return found ?? null;
}

/** How many levels hang below a link. A childless one is 0. */
function heightOf(item: MenuDraftItem): number {
  return item.children.length === 0 ? 0 : 1 + Math.max(...item.children.map(heightOf));
}

/** Whether `key` is anywhere inside `item`'s subtree. */
function contains(item: MenuDraftItem, key: string): boolean {
  return findItem(item.children, key) !== undefined;
}

/** Lifts `key` out of wherever it is, leaving the rest of the tree intact. */
function detach(items: MenuDraftItem[], key: string): void {
  const list = listHolding(items, key);
  if (!list) {
    return;
  }
  list.splice(
    list.findIndex((item) => item.key === key),
    1,
  );
}

export function blankItem(): MenuDraftItem {
  return {
    key: `k${nextKey++}`,
    labels: {},
    kind: 'URL',
    value: '/',
    openInNewTab: false,
    visible: true,
    broken: false,
    children: [],
  };
}

/**
 * The two storefront menus as editable trees. Each menu is saved whole (`PUT /menus/{handle}`), which
 * is what the server expects; the draft lives here so switching tabs does not lose an unsaved order.
 */
@Injectable()
export class MenusFacade {
  private readonly api = inject(MenusService);
  private readonly items = inject(ContentItemsService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly cache = inject(ContentCache);

  private readonly resource = rxResource({
    params: () => this.shell.currentStoreId() ?? undefined,
    stream: () => forkJoin({main: this.api.get('MAIN'), footer: this.api.get('FOOTER')}),
  });

  /** Published pages, for the PAGE target picker. */
  private readonly pagesResource = rxResource({
    params: () => {
      this.cache.stamp();
      return this.shell.currentStoreId() ?? undefined;
    },
    stream: () =>
      this.items
        .list(
          'pages',
          {status: 'PUBLISHED', locale: null, state: null, q: ''},
          {page: 0, count: 100},
        )
        .pipe(map((page) => page.content)),
  });

  readonly pages = computed<readonly ContentRow[]>(() =>
    this.pagesResource.hasValue() ? this.pagesResource.value() : [],
  );

  readonly isLoading = this.resource.isLoading;
  readonly error = computed(() => this.resource.error() as Error | undefined);
  readonly busy = signal(false);

  /** The editable trees. Reset from the server whenever it answers; edits mark `dirty`. */
  readonly drafts = signal<Record<MenuHandle, MenuDraftItem[]>>({MAIN: [], FOOTER: []});
  readonly dirty = signal<Record<MenuHandle, boolean>>({MAIN: false, FOOTER: false});

  constructor() {
    // pour the server's trees into the drafts whenever a load completes
    effect(() => {
      const value = this.resource.hasValue() ? this.resource.value() : null;
      if (value) {
        const next = {
          MAIN: value.main.items.map(toDraft),
          FOOTER: value.footer.items.map(toDraft),
        };
        /*
         * `untracked`, or this effect re-runs itself forever: it reads the drafts, the handle and
         * the open key, and writes all three.
         */
        untracked(() => {
          this.carrySelection(this.drafts()[this.handle()], next[this.handle()]);
          this.drafts.set(next);
          this.dirty.set({MAIN: false, FOOTER: false});
        });
      }
    });
  }

  /**
   * Keeps the editor open on the same link across a re-pour.
   *
   * Draft keys are minted fresh by `toDraft`, so every one of them changes when the server answers
   * — and without this, saving a menu would close the editor on the link that had just been saved.
   * Matched by position, which is the thing that did not change.
   */
  private carrySelection(before: readonly MenuDraftItem[], after: readonly MenuDraftItem[]): void {
    const key = this.selectedKey();
    const path = key === null ? null : pathOf(before, key);
    this.selectedKey.set(path === null ? null : (atPath(after, path)?.key ?? null));
  }

  update(handle: MenuHandle, items: MenuDraftItem[]): void {
    this.drafts.update((current) => ({...current, [handle]: items}));
    this.dirty.update((current) => ({...current, [handle]: true}));
  }

  /* ------------------------------------------------------------------ the open link ---- */

  /**
   * Which link the editor beside the tree is showing.
   *
   * A draft key, not an id: a link that has never been saved has no id, and it is exactly the link
   * an operator is most likely to be editing.
   */
  readonly selectedKey = signal<string | null>(null);

  readonly selected = computed<MenuDraftItem | null>(() => {
    const key = this.selectedKey();
    return key === null ? null : (findItem(this.drafts()[this.handle()], key) ?? null);
  });

  /** Which menu is on screen. Owned here so a save, a discard and a reload all agree on it. */
  readonly handle = signal<MenuHandle>('MAIN');

  /** The menu on screen, as a tree. */
  readonly currentItems = computed<readonly MenuDraftItem[]>(() => this.drafts()[this.handle()]);

  /* --------------------------------------------------------------------- the edits ---- */

  /**
   * Writes fields onto one link.
   *
   * One commit path for every control in the editor. `kind` is the only field with consequences:
   * changing it invalidates whatever target the old kind meant, so the value is re-seeded here
   * rather than left as a leftover the operator has to notice and clear.
   */
  setField(handle: MenuHandle, key: string, patch: Partial<MenuDraftItem>): void {
    const items = cloneAll(this.drafts()[handle]);
    const target = findItem(items, key);
    if (!target) {
      return;
    }
    Object.assign(target, patch);

    if (patch.kind !== undefined) {
      if (!targetNeedsValue(target.kind)) {
        target.value = '';
      } else if (target.kind === 'URL') {
        target.value = '/';
      } else if (target.kind === 'PAGE') {
        // The picker only offers published pages; seeding the first beats a blank the operator has
        // to notice is a dead link.
        target.value = this.pages()[0]?.slug ?? '';
      } else {
        target.value = '';
      }
      target.broken = false;
    }
    if (patch.value !== undefined) {
      // The server sets `broken`; an operator who has just retyped the target deserves to stop
      // being told it is broken before the next save can say otherwise.
      target.broken = false;
    }
    this.update(handle, items);
  }

  /** Writes one label, in one language. The others are untouched. */
  setLabel(handle: MenuHandle, key: string, language: string, label: string): void {
    const items = cloneAll(this.drafts()[handle]);
    const target = findItem(items, key);
    if (!target) {
      return;
    }
    target.labels = {...target.labels, [language]: label};
    this.update(handle, items);
  }

  /** Appends a link, at the top level or inside `parentKey`, and returns its key so it can be opened. */
  addItem(handle: MenuHandle, parentKey?: string): string | null {
    const items = cloneAll(this.drafts()[handle]);
    const item = blankItem();

    if (parentKey === undefined) {
      items.push(item);
    } else {
      const parent = findItem(items, parentKey);
      if (!parent || (depthOf(items, parentKey) ?? 0) + 2 > MENU_MAX_DEPTH) {
        return null;
      }
      parent.children.push(item);
    }
    this.update(handle, items);
    return item.key;
  }

  /**
   * Removes a link and everything under it.
   *
   * The whole subtree, matching the catalogue's cascade — a sub-link whose parent is gone has
   * nowhere to hang. The editor says how many go with it before the button is pressed.
   */
  removeItem(handle: MenuHandle, key: string): void {
    const strip = (list: MenuDraftItem[]): MenuDraftItem[] =>
      list
        .filter((item) => item.key !== key)
        .map((item) => ({...item, children: strip(item.children)}));

    const items = strip(cloneAll(this.drafts()[handle]));
    this.update(handle, items);

    /*
     * Checked against the tree, not against `key`: removing a parent takes its sub-links with it,
     * and it is one of *those* that the editor is most likely to have open — the operator opened
     * the child, saw the "removes 2 sub-links" warning, and pressed the button.
     */
    const open = this.selectedKey();
    if (open !== null && handle === this.handle() && !findItem(items, open)) {
      this.selectedKey.set(null);
    }
  }

  /**
   * A move from the tree — by drag, by a row button or from the keyboard, all one event.
   *
   * Detach, then re-attach: every position is the same two steps over a fresh clone, which is what
   * lets one function replace the four the component used to carry (up, down, indent, outdent) and
   * makes the depth rule a single check rather than four.
   */
  applyMove(handle: MenuHandle, move: TreeMove): void {
    const nodeKey = String(move.nodeId);
    const targetKey = String(move.targetId);
    if (nodeKey === targetKey) {
      return;
    }

    const items = cloneAll(this.drafts()[handle]);
    const subject = findItem(items, nodeKey);
    if (!subject || contains(subject, targetKey)) {
      // Dropping a link into its own subtree would detach the branch from the tree entirely.
      return;
    }

    /*
     * The level the subject would land on, counted from 1. `inside` goes a level below the target;
     * every other position makes it the target's sibling, so it lands on the target's own level.
     */
    const targetDepth = depthOf(items, targetKey);
    if (targetDepth === null) {
      return;
    }
    const landing = move.position === 'inside' ? targetDepth + 2 : targetDepth + 1;
    if (landing + heightOf(subject) > MENU_MAX_DEPTH) {
      return;
    }

    detach(items, nodeKey);

    if (move.position === 'inside') {
      const parent = findItem(items, targetKey);
      if (!parent) {
        return;
      }
      parent.children.push(subject);
      this.update(handle, items);
      return;
    }

    /*
     * All three remaining positions insert into the list the *target* sits in — for `out` the
     * target is the subject's old parent, for `before` and `after` it is a sibling. The index is
     * read after the detach, so a reorder among siblings cannot be off by one.
     */
    const list = listHolding(items, targetKey);
    if (!list) {
      return;
    }
    const at = list.findIndex((item) => item.key === targetKey);
    list.splice(move.position === 'before' ? at : at + 1, 0, subject);
    this.update(handle, items);
  }

  save(handle: MenuHandle): void {
    this.busy.set(true);
    const body: Menu = {
      handle,
      items: this.drafts()[handle].map((item, index) => toWire(item, index)),
    };
    this.api.put(handle, body).subscribe({
      next: (saved) => {
        this.busy.set(false);
        const items = saved.items.map(toDraft);
        if (handle === this.handle()) {
          this.carrySelection(this.drafts()[handle], items);
        }
        this.drafts.update((current) => ({...current, [handle]: items}));
        this.dirty.update((current) => ({...current, [handle]: false}));
        this.cache.invalidate();
        this.toast.success(
          this.transloco.translate('content.menus.saved', {
            menu: this.transloco.translate(`content.menus.handle.${handle}`),
          }),
        );
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /**
   * Back to what the server last answered — for this menu only. Reloading the resource instead
   * would do the same job through the constructor effect, but that effect pours *both* trees,
   * so discarding one menu's draft would silently throw away the other's unsaved edits.
   */
  discard(handle: MenuHandle): void {
    if (!this.resource.hasValue()) {
      return;
    }
    const value = this.resource.value();
    const items = (handle === 'MAIN' ? value.main : value.footer).items.map(toDraft);
    if (handle === this.handle()) {
      this.carrySelection(this.drafts()[handle], items);
    }
    this.drafts.update((current) => ({...current, [handle]: items}));
    this.dirty.update((current) => ({...current, [handle]: false}));
  }

  retry(): void {
    this.resource.reload();
  }
}
