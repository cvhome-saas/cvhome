import {Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {FieldError} from '@shared/ui/form-field/field-error';
import {Icon} from '@shared/ui/icon/icon';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import {Toggle} from '@shared/ui/toggle/toggle';
import {Tree, type TreeMove, type TreeNode} from '@shared/ui/tree/tree';
import {NumberField} from '@shared/ui/number-field/number-field';
import type {CategoryNode} from '@models/taxonomy';
import {CopyFields} from '../copy-fields/copy-fields';
import {LocaleChips} from '../locale-chips/locale-chips';
import {CatalogueFacade} from '../../facades/catalogue.facade';

/**
 * The category tree, and the editor for whichever node is open.
 *
 * The tree *is* the list: seller-ui had a categories table and a separate hierarchy screen, and an
 * operator restructuring a catalogue had to hold the shape of it in their head while looking at a
 * flat list. One screen, one shape.
 *
 * **What the design draws and the record does not carry.** A category banner image — there is no
 * image field on `ReadableCategory`, no upload endpoint, and nothing on the storefront reads one.
 * The block is removed rather than disabled, because a permanently dead upload well is worse than
 * an honest absence. See lessons.md, "Catalogue — a category has no banner image and a brand has no
 * logo or publish flag".
 */
@Component({
  selector: 'app-category-tab',
  imports: [
    CopyFields,
    FieldError,
    Icon,
    LocaleChips,
    NoticeBar,
    Panel,
    ReactiveFormsModule,
    Toggle,
    TranslocoDirective,
    Tree,
    NumberField,
  ],
  templateUrl: './category-tab.html',
  styleUrls: ['../editor-card.css', './category-tab.css'],
})
export class CategoryTab {
  protected readonly facade = inject(CatalogueFacade);

  protected readonly form = this.facade.categoryForm;
  protected readonly nodes = this.facade.treeNodes;
  protected readonly selected = this.facade.selectedCategory;
  protected readonly creating = computed(() => this.facade.editorMode() === 'create');

  /** How many categories there are, how many are top level, and how many the storefront nav shows. */
  protected readonly counts = computed(() => {
    const roots = this.facade.categories();
    const flat = flattenNodes(this.nodes());
    return {
      total: flat.length,
      roots: roots.length,
      visible: flat.filter((node) => node.visible).length,
    };
  });

  /**
   * The path to the open category, as a trail of names.
   *
   * The tree already shows depth by indent, but the editor is a separate panel and by the time an
   * operator has scrolled to it the indent is off screen — so the panel restates where they are.
   */
  protected readonly path = computed(() => {
    const node = this.selected();
    if (!node) {
      return [];
    }
    const trail: string[] = [];
    let current = node;
    const all = this.facade.categories();
    for (;;) {
      trail.unshift(current.name);
      const parent = current.parentId === null ? null : findNode(all, current.parentId);
      if (!parent) {
        break;
      }
      current = parent;
    }
    return trail;
  });

  protected onSelect(id: number): void {
    this.facade.select('categories', id);
  }

  protected onMove(move: TreeMove): void {
    this.facade.moveCategory(move);
  }

  protected onNameInput(value: string): void {
    this.facade.suggestCode('categories', value);
  }
}

function flattenNodes(nodes: readonly TreeNode[]): readonly TreeNode[] {
  return nodes.flatMap((node) => [node, ...flattenNodes(node.children)]);
}

function findNode(nodes: readonly CategoryNode[], id: number): CategoryNode | null {
  for (const node of nodes) {
    if (node.id === id) {
      return node;
    }
    const found = findNode(node.children, id);
    if (found) {
      return found;
    }
  }
  return null;
}
