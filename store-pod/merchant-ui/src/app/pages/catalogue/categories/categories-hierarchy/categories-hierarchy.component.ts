import {Component, OnInit, ViewChild} from '@angular/core';
import {CategoryService} from '../services/category.service';
import {TranslateService} from '@ngx-translate/core';
import {NbToastrService} from "@nebular/theme";
import {ErrorService} from "../../../../shared/services/error.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {NgcxTreeComponent, NgcxTreeNode} from "@cluetec/ngcx-tree";

@Component({
  selector: 'ngx-categories-hierarchy',
  standalone: false,
  templateUrl: './categories-hierarchy.component.html',
  styleUrls: ['./categories-hierarchy.component.scss']
})
export class CategoriesHierarchyComponent implements OnInit {
  nodes: NgcxTreeNode[] = [];
  @ViewChild('tree', {static: false})
  ngcxTree: NgcxTreeComponent<NgcxTreeNode>;

  loader = false;
  params: any;

  constructor(
    private categoryService: CategoryService,
    private toastr: NbToastrService,
    private errorService: ErrorService,
    private selectedStoreService: SelectedStoreService,
    private translate: TranslateService) {
    this.params = this.loadParams();
  }

  loadParams() {
    return {
      store: "",
      page: 0
    };
  }

  getList() {
    if (this.params.store) {
      this.loader = true;
      this.categoryService.getHierarchyOfCategories(this.params)
        .subscribe({
          next: res => {
            this.nodes = this.toTreeRoot(res.content)
            this.loader = false;
          },
          error: err => {
            this.loader = false;
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          },
          complete: () => {
            this.loader = false;
            this.ngcxTree.treeControl.expandAll();
          }
        });

    }
  }

  toTreeRoot(categories: any[]): NgcxTreeNode[] {
    const root = this.toNode(undefined, "-1");
    this.toTreeNode(root, categories);
    return root.children
  }

  toTreeNode(root: NgcxTreeNode, categories: any[]): void {
    categories.forEach(it => {
      const node: NgcxTreeNode = this.toNode(it.code, it.id);
      root.children.push(node);
      if (it.children) {
        this.toTreeNode(node, it.children)
      }
    });
  }

  toNode(title?: string, id?: string): NgcxTreeNode {
    return {
      id: `${id}`,
      title: title,
      children: []
    };
  }

  onMoveNode(event) {
    let parentId;

    if (event.parent === undefined) {
      parentId = -1;
    } else {
      parentId = event.parent.id;
    }

    this.categoryService.updateHierarchy(event.node.id, parentId, this.params.store)
      .subscribe({
        next: (ignored) => {
          this.toastr.success(this.translate.instant('CATEGORY.HIERARCHY_UPDATED'));
        },
        error: err => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
        complete: () => {
        }
      });
  }


  ngOnInit(): void {
    this.selectedStoreService.current().subscribe({
      next: (it) => {
        this.params.store = it;
      },
      error: err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      },
      complete: () => {
        this.getList();
      }
    })
  }
}
