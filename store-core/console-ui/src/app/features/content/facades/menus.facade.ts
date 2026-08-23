import {Injectable, computed, effect, inject, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';
import {forkJoin, map} from 'rxjs';

import {ContentCache} from '@api/content/content-cache';
import {ContentItemsService} from '@api/content/content-items.service';
import {MenusService} from '@api/content/menus.service';
import {ApiErrorService} from '@core/errors/api-error.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {ContentRow, Menu, MenuHandle, MenuItem, MenuTargetKind} from '@models/content';
import {ToastService} from '@shared/ui/toast/toast';

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
@Injectable({providedIn: 'root'})
export class MenusFacade {
  private readonly api = inject(MenusService);
  private readonly items = inject(ContentItemsService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly cache = inject(ContentCache);

  private readonly stamp = signal(0);

  private readonly resource = rxResource({
    params: () => {
      this.stamp();
      return this.shell.currentStoreId() ?? undefined;
    },
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
  readonly saving = signal(false);

  /** The editable trees. Reset from the server whenever it answers; edits mark `dirty`. */
  readonly drafts = signal<Record<MenuHandle, MenuDraftItem[]>>({MAIN: [], FOOTER: []});
  readonly dirty = signal<Record<MenuHandle, boolean>>({MAIN: false, FOOTER: false});

  constructor() {
    // pour the server's trees into the drafts whenever a load completes
    effect(() => {
      const value = this.resource.hasValue() ? this.resource.value() : null;
      if (value) {
        this.drafts.set({
          MAIN: value.main.items.map(toDraft),
          FOOTER: value.footer.items.map(toDraft),
        });
        this.dirty.set({MAIN: false, FOOTER: false});
      }
    });
  }

  update(handle: MenuHandle, items: MenuDraftItem[]): void {
    this.drafts.update((current) => ({...current, [handle]: items}));
    this.dirty.update((current) => ({...current, [handle]: true}));
  }

  save(handle: MenuHandle): void {
    this.saving.set(true);
    const body: Menu = {
      handle,
      items: this.drafts()[handle].map((item, index) => toWire(item, index)),
    };
    this.api.put(handle, body).subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.drafts.update((current) => ({...current, [handle]: saved.items.map(toDraft)}));
        this.dirty.update((current) => ({...current, [handle]: false}));
        this.cache.invalidate();
        this.toast.success(
          this.transloco.translate('content.menus.saved', {
            menu: this.transloco.translate(`content.menus.handle.${handle}`),
          }),
        );
      },
      error: (failure: unknown) => {
        this.saving.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  discard(handle: MenuHandle): void {
    this.stamp.update((v) => v + 1);
  }

  retry(): void {
    this.resource.reload();
  }
}
