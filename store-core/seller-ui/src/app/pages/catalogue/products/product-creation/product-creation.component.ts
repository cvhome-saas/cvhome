import {Component} from '@angular/core';
import {ProductFormComponent} from '../product-form/product-form.component';

@Component({
  selector: 'ngx-product-creation',
  standalone: true,
  imports: [ProductFormComponent],
  templateUrl: './product-creation.component.html',
  styleUrls: ['./product-creation.component.scss']
})
export class ProductCreationComponent {
  product = {};
}
