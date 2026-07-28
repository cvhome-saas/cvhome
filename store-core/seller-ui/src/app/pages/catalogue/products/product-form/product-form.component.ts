import {Component, Input, OnInit, inject} from '@angular/core';
import {ProductFormFacade} from '../facades/product-form.facade';
import {ProductFormService} from '../services/product-form.service';

@Component({
  selector: 'ngx-product-form',
  standalone: false,
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss'],
  providers: [ProductFormFacade, ProductFormService]
})
export class ProductFormComponent implements OnInit {
  @Input() product: any;
  @Input() _title: string;

  protected readonly facade = inject(ProductFormFacade);

  ngOnInit(): void {
    this.facade.init(this.product);
  }
}
