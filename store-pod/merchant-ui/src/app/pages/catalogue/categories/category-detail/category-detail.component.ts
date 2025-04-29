import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

import {CategoryService} from '../services/category.service';
import {ErrorService} from "../../../../shared/services/error.service";

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
    private errorService: ErrorService
  ) {
  }

  ngOnInit() {
    this.loadingInfo = true;
    this.activatedRoute.params.subscribe(it => {
      let split: string[] = it["id"].split("-");
      let store = split[0];
      let id = split[1];
      this.categoryService.getCategoryById(id, store)
        .subscribe(res => {
          this.category = res;
          this.loadingInfo = false;
        }, err => {
          this.loadingInfo = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        })
    });

  }

}
