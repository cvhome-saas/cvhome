import {Injectable, inject, signal, DestroyRef} from '@angular/core';
import {CategoryService} from 'seller-core/catalog';
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {SelectedStoreService} from 'seller-core';
import {NgcxTreeNode} from '@cluetec/ngcx-tree';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {CategoryTreeMoveEvent, ReadableCategory} from 'seller-core/catalog';

@Injectable()
export class CategoriesHierarchyFacade {
  private readonly categoryService = inject(CategoryService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  readonly nodes = signal<NgcxTreeNode[]>([]);
  readonly loader = signal<boolean>(false);

  private store = '';

  init(destroyRef:DestroyRef,onComplete?: () => void): void {
    this.selectedStoreService.current()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (store) => {
          this.store = store;
          if (store) {
            this.loadHierarchy(onComplete);
          }
        },
        error: (err) => this.apiErrors.notify(err)
      });
  }

  loadHierarchy(onComplete?: () => void): void {
    if (!this.store) return;
    this.loader.set(true);

    this.categoryService.getHierarchyOfCategories({store: this.store, page: 0}).subscribe({
      next: (res) => {
        this.nodes.set(this.toTreeRoot(res.content || []));
        this.loader.set(false);
        if (onComplete) {
          onComplete();
        }
      },
      error: (err) => {
        this.loader.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  onMoveNode(event: CategoryTreeMoveEvent): void {
    const parentId = event.parent === undefined ? -1 : event.parent.id;
    this.categoryService.updateHierarchy(event.node.id, parentId).subscribe({
      next: () => {
        this.notify.success('CATEGORY.HIERARCHY_UPDATED');
      },
      error: (err) => this.apiErrors.notify(err)
    });
  }

  private toTreeRoot(categories: ReadableCategory[]): NgcxTreeNode[] {
    const root = this.toNode(undefined, '-1');
    this.toTreeNode(root, categories);
    return root.children;
  }

  private toTreeNode(root: NgcxTreeNode, categories: ReadableCategory[]): void {
    categories.forEach((it) => {
      const node: NgcxTreeNode = this.toNode(it.code, `${it.id}`);
      root.children.push(node);
      if (it.children) {
        this.toTreeNode(node, it.children);
      }
    });
  }

  private toNode(title?: string, id?: string): NgcxTreeNode {
    return {
      id: `${id}`,
      title,
      children: []
    };
  }
}
