import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {OrderListFacade} from '../facades/order-list.facade';
import {TableStateService} from '../../shared/table/table-state.service';
import {ColumnMode, NgxDatatableModule} from "@swimlane/ngx-datatable";
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule,
  NbCardModule
} from '@nebular/theme';

@Component({
  selector: 'ngx-order-list',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
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
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss'],
  providers: [OrderListFacade, TableStateService]
})
export class OrderListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(OrderListFacade);
  private readonly destroyRef = inject(DestroyRef);

  filter = {...this.facade.filter()};

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }

  onPageChange(event: any): void {
    this.facade.onPageChange(event);
  }

  onFilterChange(): void {
    this.facade.onFilterChange(this.filter);
  }

  resetFilters(): void {
    this.filter = {phone: '', email: '', name: '', status: ''};
    this.facade.onFilterChange(this.filter);
  }
}
