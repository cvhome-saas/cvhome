import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule} from '@nebular/theme';
import {ColumnMode, NgxDatatableModule} from '@swimlane/ngx-datatable';
import {TypesListFacade} from '../facades/types-list.facade';
import {TableStateService} from 'seller-core';

@Component({
  selector: 'ngx-types-list',
  standalone: true,
  imports: [TranslateModule, NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule, NgxDatatableModule],
  templateUrl: './types-list.component.html',
  styleUrls: ['./types-list.component.scss'],
  providers: [TypesListFacade, TableStateService]
})
export class TypesListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(TypesListFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
