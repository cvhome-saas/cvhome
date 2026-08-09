import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbCardModule, NbSpinnerModule} from '@nebular/theme';
import {ColumnMode, NgxDatatableModule} from '@swimlane/ngx-datatable';
import {CustomerListFacade} from '../facades/customer-list.facade';
import {TableStateService} from 'seller-core';

@Component({
  selector: 'ngx-list',
  standalone: true,
  imports: [TranslateModule, NbCardModule, NbSpinnerModule, NgxDatatableModule],
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.scss'],
  providers: [CustomerListFacade, TableStateService]
})
export class CustomerListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(CustomerListFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
