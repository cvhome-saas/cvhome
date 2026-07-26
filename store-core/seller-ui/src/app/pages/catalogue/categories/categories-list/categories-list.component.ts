import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {ColumnMode} from '@swimlane/ngx-datatable';
import {CategoriesListFacade} from '../facades/categories-list.facade';
import {TableStateService} from '../../../shared/table/table-state.service';

@Component({
  selector: 'ngx-categories-list',
  standalone: false,
  templateUrl: './categories-list.component.html',
  styleUrls: ['./categories-list.component.scss'],
  providers: [CategoriesListFacade, TableStateService]
})
export class CategoriesListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(CategoriesListFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
