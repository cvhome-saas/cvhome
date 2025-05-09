import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

import {CategoryService} from '../services/category.service';
import {ErrorService} from "../../../../shared/services/error.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {mergeMap, of, zip} from "rxjs";
import {StoreService} from "../../../store-management/services/store.service";
import {Store} from "../../../store-management/models/store";

@Component({
  selector: 'ngx-category-detail',
  standalone: false,
  templateUrl: './category-detail.component.html',
  styleUrls: ['./category-detail.component.scss']
})
export class CategoryDetailComponent implements OnInit {
  category: any = {};
  loadingInfo = false;
  loading: boolean = false;
  store: Store

  constructor(
    private categoryService: CategoryService,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService,
    private selectedStoreService: SelectedStoreService,
    private storeService: StoreService
  ) {
  }

  ngOnInit() {
    this.loadingInfo = true;
    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .pipe(mergeMap(([selectedStore, params]) => {
        return zip(of(selectedStore), of(params), this.storeService.getStore(selectedStore), this.categoryService.getCategoryById(params['id'], selectedStore));
      }))
      .subscribe({
        next: ([selectedStore, params, store, category]) => {
          this.store = store;
          this.loadingInfo = false;
          this.category = category;
        },
        error: (err) => {
          this.loadingInfo = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
        complete: () => {
          this.loadingInfo = false;
        },
      });

  }

}
