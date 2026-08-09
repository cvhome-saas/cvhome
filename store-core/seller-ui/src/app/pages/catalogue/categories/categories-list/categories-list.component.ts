import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule} from '@nebular/theme';
import {ColumnMode, NgxDatatableModule} from '@swimlane/ngx-datatable';
import {CategoriesListFacade} from '../facades/categories-list.facade';
import {TableStateService} from 'seller-core';

@Component({
  selector: 'ngx-categories-list',
  standalone: true,
  imports: [TranslateModule, NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule, NgxDatatableModule],
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
