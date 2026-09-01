import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, of} from 'rxjs';

import {ContentCache} from '@api/content/content-cache';
import {ContentItemsService} from '@api/content/content-items.service';
import {MenusService} from '@api/content/menus.service';
import {NOTIFICATION_PORT} from '@cvhome-saas/ui-kit';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import type {Menu, MenuHandle, MenuItem} from '@models/content';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {MENU_MAX_DEPTH, MenusFacade, type MenuDraftItem} from './menus.facade';

function item(label: string, children: MenuItem[] = []): MenuItem {
  return {
    labels: {en: label},
    target: {kind: 'URL', value: `/${label.toLowerCase()}`},
    visible: true,
    children,
  };
}

/**
 * Shop
 *   New in
 *   Sale
 * About
 */
const MAIN: readonly MenuItem[] = [
  item('Shop', [item('New in'), item('Sale')]),
  item('About'),
];

class FakeMenusService {
  saved: Menu | null = null;

  get(handle: MenuHandle): Observable<Menu> {
    return of({handle, items: handle === 'MAIN' ? MAIN : []});
  }

  put(handle: MenuHandle, body: Menu): Observable<Menu> {
    this.saved = body;
    return of({handle, items: body.items});
  }
}

describe('MenusFacade', () => {
  let facade: MenusFacade;
  let api: FakeMenusService;

  beforeEach(() => {
    localStorage.removeItem('cvhome.console.store');
    api = new FakeMenusService();

    TestBed.configureTestingModule({
      imports: [...translocoTesting().imports],
      providers: [
        provideRouter([]),
        MenusFacade,
        {provide: MenusService, useValue: api},
        {provide: ContentItemsService, useValue: {list: () => of({content: []})}},
        {provide: ContentCache, useValue: {stamp: () => 0, invalidate: () => undefined}},
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        {provide: NOTIFICATION_PORT, useValue: {danger: () => undefined, success: () => undefined}},
        ...translocoTesting().providers,
      ],
    });
    facade = TestBed.inject(MenusFacade);
  });

  /** Settles the store directory and the menus resource. */
  function settle(): void {
    TestBed.tick();
    tick();
    TestBed.tick();
  }

  /** The whole menu as `label@depth` strings, depth-first — the shape, without the noise. */
  function shape(): string[] {
    const out: string[] = [];
    const walk = (items: readonly MenuDraftItem[], depth: number): void => {
      for (const link of items) {
        out.push(`${link.labels['en']}@${depth}`);
        walk(link.children, depth + 1);
      }
    };
    walk(facade.currentItems(), 0);
    return out;
  }

  /** One link, by key — the editor's view of it, without going through `selectedKey`. */
  function at(key: string): MenuDraftItem {
    const find = (items: readonly MenuDraftItem[]): MenuDraftItem | undefined => {
      for (const link of items) {
        if (link.key === key) {
          return link;
        }
        const deeper = find(link.children);
        if (deeper) {
          return deeper;
        }
      }
      return undefined;
    };
    return find(facade.currentItems())!;
  }

  function keyOf(label: string): string {
    const find = (items: readonly MenuDraftItem[]): MenuDraftItem | undefined => {
      for (const link of items) {
        if (link.labels['en'] === label) {
          return link;
        }
        const deeper = find(link.children);
        if (deeper) {
          return deeper;
        }
      }
      return undefined;
    };
    return find(facade.currentItems())!.key;
  }

  /* ------------------------------------------------------------------- applyMove ---- */

  it('reorders siblings with before and after', fakeAsync(() => {
    settle();

    facade.applyMove('MAIN', {nodeId: keyOf('Sale'), targetId: keyOf('New in'), position: 'before'});
    expect(shape()).toEqual(['Shop@0', 'Sale@1', 'New in@1', 'About@0']);

    facade.applyMove('MAIN', {nodeId: keyOf('Sale'), targetId: keyOf('New in'), position: 'after'});
    expect(shape()).toEqual(['Shop@0', 'New in@1', 'Sale@1', 'About@0']);
  }));

  it('nests a root into the one before it, and promotes a child back out', fakeAsync(() => {
    settle();

    facade.applyMove('MAIN', {nodeId: keyOf('About'), targetId: keyOf('Shop'), position: 'inside'});
    expect(shape()).toEqual(['Shop@0', 'New in@1', 'Sale@1', 'About@1']);

    // `out` names the *parent* as the target — the node lands among the parent's siblings.
    facade.applyMove('MAIN', {nodeId: keyOf('About'), targetId: keyOf('Shop'), position: 'out'});
    expect(shape()).toEqual(['Shop@0', 'New in@1', 'Sale@1', 'About@0']);
  }));

  it('reorders a root across the whole subtree of the one before it', fakeAsync(() => {
    settle();

    facade.applyMove('MAIN', {nodeId: keyOf('About'), targetId: keyOf('Shop'), position: 'before'});
    expect(shape()).toEqual(['About@0', 'Shop@0', 'New in@1', 'Sale@1']);
  }));

  it(`refuses a nest that would reach level ${MENU_MAX_DEPTH + 1}`, fakeAsync(() => {
    settle();

    // Shop has children, so nesting it under About would put "New in" on a third level.
    facade.applyMove('MAIN', {nodeId: keyOf('Shop'), targetId: keyOf('About'), position: 'inside'});
    expect(shape()).toEqual(['Shop@0', 'New in@1', 'Sale@1', 'About@0']);

    // And a childless root into an existing child is the same refusal from the other side.
    facade.applyMove('MAIN', {nodeId: keyOf('About'), targetId: keyOf('Sale'), position: 'inside'});
    expect(shape()).toEqual(['Shop@0', 'New in@1', 'Sale@1', 'About@0']);
  }));

  it('refuses to drop a link inside its own subtree', fakeAsync(() => {
    settle();

    facade.applyMove('MAIN', {nodeId: keyOf('Shop'), targetId: keyOf('Sale'), position: 'inside'});
    expect(shape()).toEqual(['Shop@0', 'New in@1', 'Sale@1', 'About@0']);
  }));

  it('marks the menu dirty only when a move actually landed', fakeAsync(() => {
    settle();
    expect(facade.dirty()['MAIN']).toBe(false);

    facade.applyMove('MAIN', {nodeId: keyOf('Shop'), targetId: keyOf('About'), position: 'inside'});
    expect(facade.dirty()['MAIN']).toBe(false);

    facade.applyMove('MAIN', {nodeId: keyOf('About'), targetId: keyOf('Shop'), position: 'before'});
    expect(facade.dirty()['MAIN']).toBe(true);
  }));

  /* ------------------------------------------------------------- add and remove ---- */

  it('adds inside a link, and refuses to add below the depth the server takes', fakeAsync(() => {
    settle();

    expect(facade.addItem('MAIN', keyOf('About'))).not.toBeNull();
    expect(shape()).toContain('undefined@1');

    // "Sale" is already a child, so a link inside it would be a third level.
    expect(facade.addItem('MAIN', keyOf('Sale'))).toBeNull();
  }));

  it('removes a link and everything under it', fakeAsync(() => {
    settle();

    facade.removeItem('MAIN', keyOf('Shop'));
    expect(shape()).toEqual(['About@0']);
  }));

  it('closes the editor when the open link is the one removed', fakeAsync(() => {
    settle();
    const key = keyOf('Sale');
    facade.selectedKey.set(key);

    facade.removeItem('MAIN', keyOf('Shop'));
    expect(facade.selectedKey()).toBeNull();
  }));

  /* --------------------------------------------------------------------- fields ---- */

  it('re-seeds the target when the kind changes, and clears the broken flag', fakeAsync(() => {
    settle();
    const key = keyOf('About');

    facade.setField('MAIN', key, {kind: 'CATEGORY'});
    expect(at(key).value).toBe('');

    facade.setField('MAIN', key, {kind: 'URL'});
    expect(at(key).value).toBe('/');

    facade.setField('MAIN', key, {kind: 'BLOG_INDEX'});
    expect(at(key).value).toBe('');
  }));

  it('writes one label without touching the other languages', fakeAsync(() => {
    settle();
    const key = keyOf('About');

    facade.setLabel('MAIN', key, 'ar', 'من نحن');
    expect(at(key).labels).toEqual({en: 'About', ar: 'من نحن'});
  }));

  /* ------------------------------------------------------------------ selection ---- */

  it('keeps the editor open on the same link across a save', fakeAsync(() => {
    settle();
    facade.selectedKey.set(keyOf('Sale'));
    const before = facade.selectedKey();

    facade.applyMove('MAIN', {nodeId: keyOf('About'), targetId: keyOf('Shop'), position: 'before'});
    facade.save('MAIN');
    TestBed.tick();

    // `toDraft` mints new keys, so the key must have changed — and still point at "Sale".
    expect(facade.selectedKey()).not.toBe(before);
    expect(facade.selected()?.labels['en']).toBe('Sale');
  }));
});
