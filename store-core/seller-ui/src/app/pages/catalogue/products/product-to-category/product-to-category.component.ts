import {AfterViewInit, Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ProductToCategoryFacade} from '../facades/product-to-category.facade';

@Component({
  selector: 'ngx-product-to-category',
  standalone: false,
  templateUrl: './product-to-category.component.html',
  styleUrls: ['./product-to-category.component.scss'],
  providers: [ProductToCategoryFacade]
})
export class ProductToCategoryComponent implements OnInit, AfterViewInit {
  protected readonly facade = inject(ProductToCategoryFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }

  ngAfterViewInit(): void {
    document.getElementById('tabs')?.scrollIntoView();
  }
}
