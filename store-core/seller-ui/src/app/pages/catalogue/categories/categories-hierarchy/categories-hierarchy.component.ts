import {Component, DestroyRef, inject, OnInit, ViewChild} from '@angular/core';
import {CategoriesHierarchyFacade} from '../facades/categories-hierarchy.facade';
import {NgcxTreeComponent, NgcxTreeNode} from '@cluetec/ngcx-tree';

@Component({
  selector: 'ngx-categories-hierarchy',
  standalone: false,
  templateUrl: './categories-hierarchy.component.html',
  styleUrls: ['./categories-hierarchy.component.scss'],
  providers: [CategoriesHierarchyFacade]
})
export class CategoriesHierarchyComponent implements OnInit {
  @ViewChild('tree', {static: false}) ngcxTree: NgcxTreeComponent<NgcxTreeNode>;

  protected readonly facade = inject(CategoriesHierarchyFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef, () => {
      if (this.ngcxTree?.treeControl) {
        this.ngcxTree.treeControl.expandAll();
      }
    });
  }
}
