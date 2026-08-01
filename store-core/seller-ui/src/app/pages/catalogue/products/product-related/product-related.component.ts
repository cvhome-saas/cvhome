import {AfterViewInit, Component, DestroyRef, inject} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule} from '@nebular/theme';
import {ColumnMode, NgxDatatableModule} from '@swimlane/ngx-datatable';
import {ProductRelatedFacade} from '../facades/product-related.facade';
import {ProductAutoCompleteComponent} from '../../../shared/components/product-autocomplete/product-auto-complete.component';

@Component({
  selector: 'ngx-product-related',
  standalone: true,
  imports: [TranslateModule, NbButtonModule, NbCardModule, NbIconModule, NgxDatatableModule, ProductAutoCompleteComponent],
  templateUrl: './product-related.component.html',
  styleUrls: ['./product-related.component.scss'],
  providers: [ProductRelatedFacade]
})
export class ProductRelatedComponent implements AfterViewInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(ProductRelatedFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngAfterViewInit(): void {
    this.facade.init(this.destroyRef);
  }
}
