import {Component, computed, effect, inject, input, signal} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {ReferenceOption} from '@core/reference/reference-data.service';
import {MENU_TARGET_KINDS, type MenuHandle, type MenuTargetKind} from '@models/content';
import {BusyOverlay, EmptyState, FormField, Icon, LoadError, LocaleSwitcher, NoticeBar, Panel, Select, type SelectOption, TabSwitcher, type TabItem, TextField, Toggle, Tree, type TreeMove, type TreeNode, type TreeNodeId} from '@cvhome-saas/ui-kit/ui';
import {
  MENU_MAX_DEPTH,
  MenusFacade,
  targetNeedsValue,
  type MenuDraftItem,
} from '../../facades/menus.facade';

const HANDLES: readonly MenuHandle[] = ['MAIN', 'FOOTER'];

/** The fixed paths the two index kinds resolve to. They have no target to pick. */
const INDEX_PATHS: Partial<Record<MenuTargetKind, string>> = {
  BLOG_INDEX: '/blog',
  FAQ_INDEX: '/help',
};

/**
 * "Storefront navigation": one menu at a time as a tree, with the open link edited beside it.
 *
 * It used to be both menus at once, each link a fully expanded form row — label field, kind select,
 * value field and six buttons — in a two-column grid. Fourteen links put roughly sixty controls on
 * screen for an operator who had come to move one of them, and the hierarchy, which is the thing a
 * menu *is*, was the faintest signal in the row: an indent on a flat list, with no chevron, no
 * collapse and a grip icon for a drag that did not exist.
 *
 * So it borrows the shape the catalogue's categories tab already proved, down to the primitive:
 * `app-tree` on the left showing only shape, an editor on the right showing only the open link.
 * Two screens that do the same job now look and drive the same way, and the accessible tree — one
 * tab stop, `Alt+Arrow` moves, a row menu that advertises the shortcuts — comes with it rather than
 * being reimplemented badly here.
 *
 * **The draft is still the model.** `PUT /menus/{handle}` replaces a menu whole, so edits land in
 * the local draft as they are typed and *Save menu* is the only commit. There is no per-link save
 * button, because there is no per-link endpoint to hang one on.
 */
@Component({
  selector: 'app-menus-tab',
  imports: [
    BusyOverlay,
    EmptyState,
    FormField,
    Icon,
    LoadError,
    LocaleSwitcher,
    NoticeBar,
    Panel,
    Select,
    TabSwitcher,
    TextField,
    Toggle,
    TranslocoDirective,
    Tree,
  ],
  templateUrl: './menus-tab.html',
  styleUrls: ['../../../../shared/styles/field.css', './menus-tab.css'],
})
export class MenusTab {
  private readonly transloco = inject(TranslocoService);
  protected readonly facade = inject(MenusFacade);

  readonly locales = input.required<readonly ReferenceOption[]>();
  readonly defaultLocale = input.required<string>();
  readonly canManage = input(true);

  protected readonly maxDepth = MENU_MAX_DEPTH;
  protected readonly language = signal<string>('');
  /** Collapsed rather than expanded, so a menu opens fully open — the tree's own convention. */
  protected readonly collapsed = signal<ReadonlySet<TreeNodeId>>(new Set<TreeNodeId>());

  protected readonly activeLanguage = computed(() => this.language() || this.defaultLocale());

  /** "Arabic", not "ar" — the hint beside the label field is prose, not a wire value. */
  protected readonly activeLanguageName = computed(() => {
    const code = this.activeLanguage();
    return this.locales().find((locale) => locale.code === code)?.label ?? code;
  });

  constructor() {
    /*
     * A link opened in one menu means nothing in the other, and leaving the key set would leave the
     * tree with a selection it cannot draw. Cleared on the switch rather than tolerated, so the
     * editor shows its empty state and says so.
     */
    let previous = this.facade.handle();
    effect(() => {
      const handle = this.facade.handle();
      if (handle !== previous) {
        previous = handle;
        this.facade.selectedKey.set(null);
      }
    });
  }

  protected readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    const drafts = this.facade.drafts();
    const dirty = this.facade.dirty();
    return HANDLES.map((handle) => ({
      key: handle,
      label: this.transloco.translate(`content.menus.handle.${handle}`),
      // The count, or a dot when the menu has edits the server has not seen — a menu can be dirty
      // without its count changing, and that is exactly the state worth flagging.
      badge: dirty[handle] ? '•' : String(countOf(drafts[handle])),
      badgeTone: dirty[handle] ? ('amber' as const) : ('slate' as const),
    }));
  });

  protected readonly linkCount = computed(() => countOf(this.facade.currentItems()));

  protected readonly kindOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return MENU_TARGET_KINDS.map((kind) => ({
      value: kind,
      label: this.transloco.translate(`content.menus.kind.${kind}`),
    }));
  });

  protected readonly pageOptions = computed<readonly SelectOption[]>(() =>
    this.facade.pages().map((page) => ({value: page.slug, label: page.title})),
  );

  /** Languages that have at least one label filled anywhere, for the switcher's dots. */
  protected readonly filled = computed<ReadonlySet<string>>(() => {
    const set = new Set<string>();
    const walk = (items: readonly MenuDraftItem[]): void => {
      for (const item of items) {
        for (const [code, label] of Object.entries(item.labels)) {
          if (label?.trim()) {
            set.add(code);
          }
        }
        walk(item.children);
      }
    };
    Object.values(this.facade.drafts()).forEach(walk);
    return set;
  });

  /**
   * The menu as the tree wants it: shape, name, visibility — and nothing else.
   *
   * **No `meta`.** The target used to ride along there, and in this panel's width a full URL won
   * the row: the label track collapsed and every row showed where a link went rather than what it
   * was called. The target is one click away in the editor, which is the point of splitting them.
   * `warn` stays, because a broken link is a state you have to be able to *find*, not one you go
   * looking for.
   *
   * A link with no label in the language being edited says so rather than borrowing its target as
   * a name — a missing translation is worth seeing, and disguising it as a label hides it.
   */
  protected readonly treeNodes = computed<readonly TreeNode[]>(() => {
    const language = this.activeLanguage();
    const broken = this.transloco.translate('content.menus.broken');
    const untitled = this.transloco.translate('content.menus.untitled');

    const toNode = (item: MenuDraftItem): TreeNode => ({
      id: item.key,
      label: item.labels[language]?.trim() || untitled,
      warn: item.broken ? broken : undefined,
      visible: item.visible,
      children: item.children.map(toNode),
    });

    return this.facade.currentItems().map(toNode);
  });

  protected readonly selectedPath = computed(() => {
    const key = this.facade.selectedKey();
    if (key === null) {
      return '';
    }
    const trail = (items: readonly MenuDraftItem[], above: readonly string[]): string[] | null => {
      for (const item of items) {
        const here = [...above, this.labelOf(item)];
        if (item.key === key) {
          return here;
        }
        const deeper = trail(item.children, here);
        if (deeper) {
          return deeper;
        }
      }
      return null;
    };
    return (trail(this.facade.currentItems(), []) ?? []).join(' / ');
  });

  protected labelOf(item: MenuDraftItem): string {
    return (
      item.labels[this.activeLanguage()]?.trim() ||
      this.transloco.translate('content.menus.untitled')
    );
  }

  protected needsValue(kind: MenuTargetKind): boolean {
    return targetNeedsValue(kind);
  }

  protected fixedPath(kind: MenuTargetKind): string {
    return INDEX_PATHS[kind] ?? '';
  }

  /* ------------------------------------------------------------------------ the tree ---- */

  protected onSelect(id: TreeNodeId): void {
    this.facade.selectedKey.set(String(id));
  }

  protected onToggleCollapsed(id: TreeNodeId): void {
    const next = new Set(this.collapsed());
    if (!next.delete(id)) {
      next.add(id);
    }
    this.collapsed.set(next);
  }

  protected onToggleVisible(node: TreeNode): void {
    this.facade.setField(this.facade.handle(), String(node.id), {visible: !node.visible});
  }

  protected onMove(move: TreeMove): void {
    this.facade.applyMove(this.facade.handle(), move);
  }

  /** Adding opens the new link, because an operator who adds one is about to label it. */
  protected onAdd(parentKey?: string): void {
    const key = this.facade.addItem(this.facade.handle(), parentKey);
    if (key !== null) {
      this.facade.selectedKey.set(key);
    }
  }

  protected expandAll(): void {
    this.collapsed.set(new Set());
  }

  protected collapseAll(): void {
    const keys = new Set<TreeNodeId>();
    const walk = (items: readonly MenuDraftItem[]): void => {
      for (const item of items) {
        if (item.children.length) {
          keys.add(item.key);
        }
        walk(item.children);
      }
    };
    walk(this.facade.currentItems());
    this.collapsed.set(keys);
  }

  /* ---------------------------------------------------------------------- the editor ---- */

  protected setLabel(item: MenuDraftItem, value: string): void {
    this.facade.setLabel(this.facade.handle(), item.key, this.activeLanguage(), value);
  }

  protected patch(item: MenuDraftItem, patch: Partial<MenuDraftItem>): void {
    this.facade.setField(this.facade.handle(), item.key, patch);
  }

  protected remove(item: MenuDraftItem): void {
    this.facade.removeItem(this.facade.handle(), item.key);
  }
}

/** Every link in a menu, at any depth — what the tab badge counts. */
function countOf(items: readonly MenuDraftItem[]): number {
  return items.reduce((total, item) => total + 1 + countOf(item.children), 0);
}
