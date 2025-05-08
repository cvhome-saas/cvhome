import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

import {CategoryService} from '../services/category.service';
import {ErrorService} from "../../../../shared/services/error.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {mergeMap, zip} from "rxjs";
import {map} from "rxjs/operators";

@Component({
  selector: 'ngx-category-detail',
  standalone:false,
  templateUrl: './category-detail.component.html',
  styleUrls: ['./category-detail.component.scss']
})
export class CategoryDetailComponent implements OnInit {
  category: any = {};
  loadingInfo = false;
  loading: boolean = false;

  constructor(
    private categoryService: CategoryService,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService,
    private selectedStoreService: SelectedStoreService
  ) {
  }

  ngOnInit() {
    this.loadingInfo = true;
    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .pipe(mergeMap(([selectedStore, params]) => {
        return this.categoryService.getCategoryById(params['id'], selectedStore);
      }))
      .subscribe({
        next: (data) => {
          this.loadingInfo = false;
          this.category = data;
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
