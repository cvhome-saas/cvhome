import {AfterViewInit, Component, DestroyRef, OnInit, inject} from '@angular/core';
import {Location} from '@angular/common';
import {NbSpinnerModule} from '@nebular/theme';
import {ProductsImagesFacade} from '../facades/products-images.facade';
import {ImageUploadingComponent} from '../../../shared/components/image-uploading/image-uploading.component';

@Component({
  selector: 'ngx-products-images',
  standalone: true,
  imports: [NbSpinnerModule, ImageUploadingComponent],
  templateUrl: './products-images.component.html',
  styleUrls: ['./products-images.component.css'],
  providers: [ProductsImagesFacade]
})
export class ProductsImagesComponent implements OnInit, AfterViewInit {
  private readonly location = inject(Location);
  protected readonly facade = inject(ProductsImagesFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }

  ngAfterViewInit(): void {
    if (this.location.path().includes('images')) {
      document.getElementById('tabs')?.scrollIntoView();
    }
  }
}
