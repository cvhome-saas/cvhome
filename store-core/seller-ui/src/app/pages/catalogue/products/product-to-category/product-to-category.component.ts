import {AfterViewInit, Component, DestroyRef, OnInit, inject} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbCardModule, NbOptionModule, NbSelectModule, NbSpinnerModule} from '@nebular/theme';
import {ProductToCategoryFacade} from '../facades/product-to-category.facade';

@Component({
  selector: 'ngx-product-to-category',
  standalone: true,
  imports: [TranslateModule, NbCardModule, NbOptionModule, NbSelectModule, NbSpinnerModule],
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
