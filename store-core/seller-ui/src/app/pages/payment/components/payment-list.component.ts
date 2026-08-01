import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbCardModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule
} from '@nebular/theme';
import {ColumnMode, NgxDatatableModule, PageEvent} from '@swimlane/ngx-datatable';
import {PaymentListFacade, PaymentFilter} from '../facades/payment-list.facade';
import {TableStateService} from '../../shared/table/table-state.service';
import {PaymentTransaction} from '../models/payment-transaction.model';

@Component({
  selector: 'ngx-payment-list',
  standalone: true,
  imports: [
    FormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbFormFieldModule,
    NbIconModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule,
    NgxDatatableModule
  ],
  templateUrl: './payment-list.component.html',
  styleUrls: ['./payment-list.component.scss'],
  providers: [PaymentListFacade, TableStateService]
})
export class PaymentListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(PaymentListFacade);
  private readonly destroyRef = inject(DestroyRef);

  filter: PaymentFilter = {...this.facade.filter()};

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }

  onPageChange(event: PageEvent): void {
    this.facade.onPageChange(event);
  }

  onFilterChange(): void {
    this.facade.onFilterChange(this.filter);
  }

  resetFilters(): void {
    this.filter = this.facade.filter();
    this.facade.onFilterChange(this.filter);
  }

  canApproveOrReject(row: PaymentTransaction): boolean {
    return this.facade.canApproveOrReject(row);
  }

  approve(row: PaymentTransaction): void {
    this.facade.approve(row);
  }

  reject(row: PaymentTransaction): void {
    this.facade.reject(row);
  }
}
