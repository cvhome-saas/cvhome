import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

import {BrandService} from '../services/brand.service';

@Component({
  selector: 'ngx-brand-details',
  templateUrl: './brand-details.component.html',
  styleUrls: ['./brand-details.component.scss']
})
export class BrandDetailsComponent implements OnInit {
  brand: any = {};
  loadingInfo = false;
  store: string

  constructor(
    private brandService: BrandService,
    private activatedRoute: ActivatedRoute) {
  }

  ngOnInit() {
    this.loadingInfo = true;
    this.activatedRoute.params.subscribe(it => {
      let split: string[] = it["id"].split("-");
      let store = split[0];
      let id = split[1];
      this.store=store;
      this.brandService.getBrandById(store, id)
        .subscribe(brand => {
          this.brand = brand;
          this.loadingInfo = false;
        })
    });
  }


}
