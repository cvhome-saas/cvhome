import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

import {BrandService} from '../services/brand.service';
import {ErrorService} from "../../../../shared/services/error.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {mergeMap, of, zip} from "rxjs";
import {StoreService} from "../../../store-management/services/store.service";
import {Store} from "../../../store-management/models/store";

@Component({
  selector: 'ngx-brand-details',
  standalone: false,
  templateUrl: './brand-details.component.html',
  styleUrls: ['./brand-details.component.scss']
})
export class BrandDetailsComponent implements OnInit {
  brand: any = {};
  loadingInfo = false;
  store: Store

  constructor(
    private brandService: BrandService,
    private errorService: ErrorService,
    private activatedRoute: ActivatedRoute,
    private storeService: StoreService,
    private selectedStoreService: SelectedStoreService
  ) {
  }

  ngOnInit() {
    this.loadingInfo = true;
    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .pipe(mergeMap(([selectedStore, params]) => {
        return zip(of(selectedStore), of(params), this.storeService.getStore(selectedStore), this.brandService.getBrandById(selectedStore, params["id"]));
      }))
      .subscribe({
        next: ([selectedStore, params, store, brand]) => {
          this.store = store;
          this.brand = brand;
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
