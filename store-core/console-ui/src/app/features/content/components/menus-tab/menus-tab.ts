import {Component, computed, inject, input, signal} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {ReferenceOption} from '@core/reference/reference-data.service';
import {MENU_TARGET_KINDS, type MenuHandle, type MenuTargetKind} from '@models/content';
import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {Icon} from '@shared/ui/icon/icon';
import {LoadError} from '@shared/ui/load-error/load-error';
import {LocaleSwitcher} from '@shared/ui/locale-switcher/locale-switcher';
import {Panel} from '@shared/ui/panel/panel';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {TextField} from '@shared/ui/text-field/text-field';
import {MenusFacade, blankItem, type MenuDraftItem} from '../../facades/menus.facade';

const HANDLES: readonly MenuHandle[] = ['MAIN', 'FOOTER'];

/**
 * "Storefront navigation": the Main and Footer menus as editable lists — label in the chosen
 * language, target kind and value, up/down, indent under the previous root, visibility, remove — and
 * Save per menu. Arrow buttons rather than drag-and-drop (see lessons.md); one level of nesting, as
 * the server enforces.
 */
@Component({
  selector: 'app-menus-tab',
  imports: [Badge, BusyOverlay, Icon, LoadError, LocaleSwitcher, Panel, Select, TextField, TranslocoDirective],
  templateUrl: './menus-tab.html',
  styleUrl: './menus-tab.css',
})
export class MenusTab {
  private readonly transloco = inject(TranslocoService);
  protected readonly facade = inject(MenusFacade);

  readonly locales = input.required<readonly ReferenceOption[]>();
  readonly defaultLocale = input.required<string>();
  readonly canManage = input(true);

  protected readonly handles = HANDLES;
  protected readonly language = signal<string>('');

  protected readonly activeLanguage = computed(() => this.language() || this.defaultLocale());

  protected readonly kindOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return MENU_TARGET_KINDS.map((kind) => ({value: kind, label: this.transloco.translate(`content.menus.kind.${kind}`)}));
  });

  protected readonly pageOptions = computed<readonly SelectOption[]>(() =>
    this.facade.pages().map((page) => ({value: page.slug, label: page.title})),
  );

  /** Languages that have at least one label filled, for the switcher's dots. */
  protected readonly filled = computed<ReadonlySet<string>>(() => {
    const set = new Set<string>();
    for (const items of Object.values(this.facade.drafts())) {
      for (const item of items) {
        for (const code of Object.keys(item.labels)) {
          if (item.labels[code]?.trim()) {
            set.add(code);
          }
        }
      }
    }
    return set;
  });

  protected items(handle: MenuHandle): MenuDraftItem[] {
    return this.facade.drafts()[handle];
  }

  protected label(item: MenuDraftItem): string {
    return item.labels[this.activeLanguage()] ?? '';
  }

  protected needsValue(kind: MenuTargetKind): boolean {
    return kind !== 'BLOG_INDEX' && kind !== 'FAQ_INDEX';
  }

  private commit(handle: MenuHandle, items: MenuDraftItem[]): void {
    this.facade.update(handle, items);
  }

  private clone(handle: MenuHandle): MenuDraftItem[] {
    return this.items(handle).map((item) => ({...item, labels: {...item.labels}, children: item.children.map((c) => ({...c, labels: {...c.labels}, children: []}))}));
  }

  protected setLabel(handle: MenuHandle, item: MenuDraftItem, value: string): void {
    const items = this.clone(handle);
    const target = this.find(items, item.key);
    if (target) {
      target.labels[this.activeLanguage()] = value;
      this.commit(handle, items);
    }
  }

  protected setKind(handle: MenuHandle, item: MenuDraftItem, kind: string): void {
    const items = this.clone(handle);
    const target = this.find(items, item.key);
    if (target) {
      target.kind = kind as MenuTargetKind;
      if (!this.needsValue(target.kind)) {
        target.value = '';
      } else if (target.kind === 'URL' && !target.value) {
        target.value = '/';
      } else if (target.kind === 'PAGE') {
        target.value = this.pageOptions()[0]?.value ?? '';
      }
      target.broken = false;
      this.commit(handle, items);
    }
  }

  protected setValue(handle: MenuHandle, item: MenuDraftItem, value: string): void {
    const items = this.clone(handle);
    const target = this.find(items, item.key);
    if (target) {
      target.value = value;
      target.broken = false;
      this.commit(handle, items);
    }
  }

  protected toggleVisible(handle: MenuHandle, item: MenuDraftItem): void {
    const items = this.clone(handle);
    const target = this.find(items, item.key);
    if (target) {
      target.visible = !target.visible;
      this.commit(handle, items);
    }
  }

  protected remove(handle: MenuHandle, item: MenuDraftItem): void {
    const items = this.clone(handle).filter((root) => root.key !== item.key);
    for (const root of items) {
      root.children = root.children.filter((child) => child.key !== item.key);
    }
    this.commit(handle, items);
  }

  protected add(handle: MenuHandle): void {
    const item = blankItem();
    this.commit(handle, [...this.clone(handle), item]);
  }

  /** Moves a root or a child one slot within its own list. */
  protected move(handle: MenuHandle, item: MenuDraftItem, delta: -1 | 1): void {
    const items = this.clone(handle);
    const list = this.listOf(items, item.key);
    if (!list) {
      return;
    }
    const index = list.findIndex((i) => i.key === item.key);
    const next = index + delta;
    if (next < 0 || next >= list.length) {
      return;
    }
    [list[index], list[next]] = [list[next], list[index]];
    this.commit(handle, items);
  }

  /** A root becomes the last child of the root above it; a child becomes a root after its parent. */
  protected indent(handle: MenuHandle, item: MenuDraftItem): void {
    const items = this.clone(handle);
    const index = items.findIndex((root) => root.key === item.key);
    if (index <= 0) {
      return;
    }
    const [moved] = items.splice(index, 1);
    const parent = items[index - 1];
    parent.children = [...parent.children, ...moved.children, {...moved, children: []}];
    this.commit(handle, items);
  }

  protected outdent(handle: MenuHandle, item: MenuDraftItem): void {
    const items = this.clone(handle);
    const parentIndex = items.findIndex((root) => root.children.some((child) => child.key === item.key));
    if (parentIndex < 0) {
      return;
    }
    const parent = items[parentIndex];
    const child = parent.children.find((c) => c.key === item.key) as MenuDraftItem;
    parent.children = parent.children.filter((c) => c.key !== item.key);
    items.splice(parentIndex + 1, 0, {...child, children: []});
    this.commit(handle, items);
  }

  protected isChild(handle: MenuHandle, item: MenuDraftItem): boolean {
    return this.items(handle).some((root) => root.children.some((child) => child.key === item.key));
  }

  /** Roots and children flattened for rendering, each tagged with its depth. */
  protected rows(handle: MenuHandle): readonly {item: MenuDraftItem; depth: number}[] {
    const out: {item: MenuDraftItem; depth: number}[] = [];
    for (const root of this.items(handle)) {
      out.push({item: root, depth: 0});
      for (const child of root.children) {
        out.push({item: child, depth: 1});
      }
    }
    return out;
  }

  protected handleLabel(handle: MenuHandle): string {
    return this.transloco.translate(`content.menus.handle.${handle}`);
  }

  private find(items: MenuDraftItem[], key: string): MenuDraftItem | undefined {
    for (const root of items) {
      if (root.key === key) {
        return root;
      }
      const child = root.children.find((c) => c.key === key);
      if (child) {
        return child;
      }
    }
    return undefined;
  }

  private listOf(items: MenuDraftItem[], key: string): MenuDraftItem[] | undefined {
    if (items.some((root) => root.key === key)) {
      return items;
    }
    return items.find((root) => root.children.some((c) => c.key === key))?.children;
  }
}
