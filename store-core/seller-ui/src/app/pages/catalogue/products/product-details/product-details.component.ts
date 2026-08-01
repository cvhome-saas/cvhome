import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ProductDetailsFacade} from '../facades/product-details.facade';
import {ProductFormComponent} from '../product-form/product-form.component';

@Component({
  selector: 'ngx-product-details',
  standalone: true,
  imports: [ProductFormComponent],
  templateUrl: './product-details.component.html',
  styleUrls: ['./product-details.component.scss'],
  providers: [ProductDetailsFacade]
})
export class ProductDetailsComponent implements OnInit {
  protected readonly facade = inject(ProductDetailsFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
