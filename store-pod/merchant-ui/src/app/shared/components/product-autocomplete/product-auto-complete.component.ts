import {AfterViewInit, Component, ElementRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {Observable, of} from "rxjs";
import {ProductService} from "../../../pages/catalogue/products/services/product.service";
import {map} from "rxjs/operators";
import {ErrorService} from "../../services/error.service";


@Component({
  selector: 'ngx-product-auto-complete',
  standalone: false,
  template: `<input #autoInput
                    nbInput fullWidth
                    type="text"
                    [disabled]="disabled"
                    (input)="onChange($event)"
                    [placeholder]="'COMMON.SEARCH_BY_NAME'|translate"
                    [nbAutocomplete]="auto"/>

  <nb-autocomplete #auto (selectedChange)="onSelectionChange($event)">
    <nb-option *ngFor="let option of filteredOptions$ | async" [value]="option">
      {{ option.description.name }}
      <nb-icon icon="heart-outline" status="success"></nb-icon>
    </nb-option>
  </nb-autocomplete>
  `,
})
export class ProductAutoCompleteComponent implements AfterViewInit {
  @Input() store: string;
  @Input() disabled: boolean = false;
  filteredOptions$: Observable<any[]>;
  @Output()
  onProduct: EventEmitter<any> = new EventEmitter<any>()
  @ViewChild('autoInput', {static: false})
  autoInput: ElementRef;
  firstProducts: any[] = [];
  params;

  constructor(
    private productService: ProductService,
    private translate: TranslateService,
    private errorService: ErrorService,
  ) {
    this.params = {
      "store": "",
      "name": "",
      "lang": this.translate.currentLang
    }
  }

  onChange($event) {
    if (this.autoInput.nativeElement.value && this.autoInput.nativeElement.value.trim().length > 3 && this.autoInput.nativeElement.value.trim() != this.params.name)
      this.getProductList();
    if (this.autoInput.nativeElement.value.trim().length == 0) {
      this.resetResultToFirst();
    }
  }

  onSelectionChange($event) {
    this.onProduct.emit($event);
    this.autoInput.nativeElement.value = '';
    this.resetResultToFirst()
  }

  getProductList() {
    this.params.name = this.autoInput.nativeElement.value;
    this.filteredOptions$ = this.productService.getListOfProducts(this.params).pipe(map(it => {
      return it.content
    }));
  }

  ngAfterViewInit(): void {
    this.params.store = this.store;
    this.productService.getListOfProducts(this.params).pipe(map(it => {
      return it.content
    })).subscribe((it: any[]) => {
      this.firstProducts = it
      this.resetResultToFirst();
    }, err => {
      this.errorService.error('ERROR.SYSTEM_ERROR', err);
    });

  }

  resetResultToFirst() {
    this.filteredOptions$ = of(this.firstProducts);
  }
}
