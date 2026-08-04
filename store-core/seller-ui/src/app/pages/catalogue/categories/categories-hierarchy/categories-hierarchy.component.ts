import {Component, DestroyRef, inject, OnInit, ViewChild} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbCardModule, NbSpinnerModule} from '@nebular/theme';
import {NgcxTreeComponent, NgcxTreeNode} from '@cluetec/ngcx-tree';
import {CategoriesHierarchyFacade} from '../facades/categories-hierarchy.facade';

@Component({
  selector: 'ngx-categories-hierarchy',
  standalone: true,
  imports: [TranslateModule, NbCardModule, NbSpinnerModule, NgcxTreeComponent],
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
