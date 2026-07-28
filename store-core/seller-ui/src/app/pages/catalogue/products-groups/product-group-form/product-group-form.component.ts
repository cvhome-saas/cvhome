import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ColumnMode} from '@swimlane/ngx-datatable';
import {ProductGroupFormFacade} from '../facades/product-group-form.facade';
import {ProductGroupFormService} from '../services/product-group-form.service';

@Component({
  selector: 'ngx-product-group-form',
  standalone: false,
  templateUrl: './product-group-form.component.html',
  styleUrls: ['./product-group-form.component.scss'],
  providers: [ProductGroupFormFacade, ProductGroupFormService]
})
export class ProductGroupFormComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(ProductGroupFormFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
