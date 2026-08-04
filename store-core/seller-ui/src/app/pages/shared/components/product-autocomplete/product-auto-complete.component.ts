import {AfterViewInit, Component, DestroyRef, ElementRef, EventEmitter, Input, Output, ViewChild, inject} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbAutocompleteModule, NbIconModule, NbInputModule, NbOptionModule} from '@nebular/theme';
import {ProductAutocompleteFacade} from './facades/product-autocomplete.facade';
import {PRODUCT_AUTOCOMPLETE_MIN_SEARCH_LENGTH} from './constants/product-autocomplete.constants';
import {ReadableProduct} from '../../../catalogue/products/models/product.model';

@Component({
  selector: 'ngx-product-auto-complete',
  standalone: true,
  imports: [TranslateModule, NbAutocompleteModule, NbIconModule, NbInputModule, NbOptionModule],
  template: `<input #autoInput
                    nbInput fullWidth
                    type="text"
                    [disabled]="disabled"
                    (input)="onChange()"
                    [placeholder]="'COMMON.SEARCH_BY_NAME'|translate"
                    [nbAutocomplete]="auto"/>

  <nb-autocomplete #auto (selectedChange)="onSelectionChange($event)">
    @for (option of facade.options(); track option) {
      <nb-option [value]="option">
        {{ option.sku }}
        <nb-icon icon="heart-outline" status="success"></nb-icon>
      </nb-option>
    }
  </nb-autocomplete>
  `,
  providers: [ProductAutocompleteFacade]
})
export class ProductAutoCompleteComponent implements AfterViewInit {
  @Input() store: string;
  @Input() disabled = false;
  @Output() productSelected: EventEmitter<ReadableProduct> = new EventEmitter<ReadableProduct>();
  @ViewChild('autoInput', {static: false}) autoInput: ElementRef;

  protected readonly facade = inject(ProductAutocompleteFacade);
  private readonly destroyRef = inject(DestroyRef);
  private lastSearchedName = '';

  ngAfterViewInit(): void {
    this.facade.init(this.store, this.destroyRef);
  }

  onChange(): void {
    const value = this.autoInput.nativeElement.value.trim();
    if (value && value.length > PRODUCT_AUTOCOMPLETE_MIN_SEARCH_LENGTH && value !== this.lastSearchedName) {
      this.lastSearchedName = value;
      this.facade.search(value);
    }
    if (value.length === 0) {
      this.facade.resetToFirst();
    }
  }

  onSelectionChange(value: ReadableProduct): void {
    this.productSelected.emit(value);
    this.autoInput.nativeElement.value = '';
    this.facade.resetToFirst();
  }
}
