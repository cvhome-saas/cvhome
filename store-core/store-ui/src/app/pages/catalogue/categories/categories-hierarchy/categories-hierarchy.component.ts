import {Component, OnInit, ViewChild} from '@angular/core';
import {CategoryService} from '../services/category.service';
import {TranslateService} from '@ngx-translate/core';
import {NbToastrService} from "@nebular/theme";

@Component({
  selector: 'ngx-categories-hierarchy',
  templateUrl: './categories-hierarchy.component.html',
  styleUrls: ['./categories-hierarchy.component.scss']
})
export class CategoriesHierarchyComponent implements OnInit {
  @ViewChild('tree', {static: false}) tree;
  nodes: TreeNode[] = [];
  options = {
    allowDrag: true
  }
  loader = false;
  params = this.loadParams();

  constructor(
    private categoryService: CategoryService,
    private toastr: NbToastrService,
    private translate: TranslateService) {
  }

  loadParams() {
    return {
      lang: this.translate.currentLang,
      store: "",
      page: 0
    };
  }

  getList() {
    this.loader = true;
    this.categoryService.getListOfCategories(this.params)
      .subscribe(res => {
        this.nodes = this.toTreeRoot(res.categories)
        this.loader = false;
      });
  }

  toTreeRoot(categories: any[]): TreeNode[] {
    const root = this.toNode(undefined, "-1");
    this.toTreeNode(root, categories);
    return [root]
  }

  toTreeNode(root: TreeNode, categories: any[]): void {
    categories.forEach(it => {
      const node: TreeNode = this.toNode(it.description.name, it.id);
      root.children.push(node);
      if (it.children) {
        this.toTreeNode(node, it.children)
      }
    });
  }

  toNode(name?: string, id?: string): TreeNode {
    return {
      id: id,
      name: name ? name : "root",
      children: []
    };
  }


  onMoveNode(event) {
    let parentId = event.to.parent.id;

    if (event.to.parent.name === undefined) {
      parentId = -1;
    }

    this.categoryService.updateHierarchy(event.node.id, parentId, this.params.store)
      .subscribe(res => {
        this.toastr.success(this.translate.instant('CATEGORY.HIERARCHY_UPDATED'));
      });
  }

  onSelectStore($event) {
    this.params.store = $event.id
    this.getList();
  }

  ngOnInit(): void {
    this.translate.onLangChange.subscribe(lang => {
      this.params.lang = lang.lang;
    })
  }
}

interface TreeNode {
  id: string
  name: string
  children: TreeNode[];
}
