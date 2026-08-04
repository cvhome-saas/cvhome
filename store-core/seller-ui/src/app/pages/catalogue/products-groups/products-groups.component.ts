import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'ngx-products-groups',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './products-groups.component.html',
  styleUrls: ['./products-groups.component.scss']
})
export class ProductsGroupsComponent {
}
