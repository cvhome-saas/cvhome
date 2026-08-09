import {Component, OnInit, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
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
import {ColumnMode, NgxDatatableModule} from '@swimlane/ngx-datatable';
import {ProductsListFacade} from '../facades/products-list.facade';
import {TableStateService} from 'seller-core';

@Component({
  selector: 'ngx-products-list',
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
  templateUrl: './products-list.component.html',
  styleUrls: ['./products-list.component.scss'],
  providers: [ProductsListFacade, TableStateService]
})
export class ProductsListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(ProductsListFacade);

  editing: Record<string, boolean> = {};
  filter = {...this.facade.filter()};

  ngOnInit(): void {
    this.facade.init();
  }

  onFilterChange(): void {
    this.facade.onFilterChange(this.filter);
  }

  resetFilters(): void {
    this.filter = {sku: '', available: '', categoryIds: '', manufacturerId: ''};
    this.facade.onFilterChange(this.filter);
  }

  updateValue(event: Event, cell: 'quantity' | 'price' | 'available', rowIndex: number): void {
    const target = event.target as HTMLInputElement;
    const value = target.type === 'checkbox' ? target.checked : target.value;
    this.facade.updateCell(rowIndex, cell, value);
    this.editing[rowIndex + '-' + cell] = false;
  }
}
