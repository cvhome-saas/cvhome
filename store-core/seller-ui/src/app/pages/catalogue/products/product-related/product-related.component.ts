import {AfterViewInit, Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ColumnMode} from '@swimlane/ngx-datatable';
import {ProductRelatedFacade} from '../facades/product-related.facade';

@Component({
  selector: 'ngx-product-related',
  standalone: false,
  templateUrl: './product-related.component.html',
  styleUrls: ['./product-related.component.scss'],
  providers: [ProductRelatedFacade]
})
export class ProductRelatedComponent implements OnInit, AfterViewInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(ProductRelatedFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.facade.init(this.destroyRef);
  }
}
