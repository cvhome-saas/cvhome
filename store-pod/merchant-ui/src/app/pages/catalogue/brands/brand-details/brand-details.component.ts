import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

import {BrandService} from '../services/brand.service';
import {ErrorService} from "../../../../shared/services/error.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {mergeMap, zip} from "rxjs";
import {map} from "rxjs/operators";

@Component({
  selector: 'ngx-brand-details',
  standalone:false,
  templateUrl: './brand-details.component.html',
  styleUrls: ['./brand-details.component.scss']
})
export class BrandDetailsComponent implements OnInit {
  brand: any = {};
  loadingInfo = false;
  store: string

  constructor(
    private brandService: BrandService,
    private errorService: ErrorService,
    private activatedRoute: ActivatedRoute,
    private selectedStoreService:SelectedStoreService
    ) {
  }

  ngOnInit() {
    this.loadingInfo = true;
    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .pipe(mergeMap(([selectedStore, params]) => {
        this.store = selectedStore;
        return this.brandService.getBrandById(this.store, params["id"]);
      })).subscribe({
      next: (it) => {
        this.brand = it;
        this.loadingInfo = false;
      },
      error: (err) => {
        this.loadingInfo = false;
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      },
      complete: () => {
        this.loadingInfo = false;
      },
    })

  }


}
