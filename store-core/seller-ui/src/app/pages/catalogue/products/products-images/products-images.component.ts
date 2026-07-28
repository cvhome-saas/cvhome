import {AfterViewInit, Component, DestroyRef, OnInit, inject} from '@angular/core';
import {Location} from '@angular/common';
import {ProductsImagesFacade} from '../facades/products-images.facade';

@Component({
  selector: 'ngx-products-images',
  standalone: false,
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
