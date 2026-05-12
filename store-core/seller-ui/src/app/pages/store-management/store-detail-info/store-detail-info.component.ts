import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {StoreService} from '../services/store.service';
import {ErrorService} from "../../shared/services/error.service";

@Component({
  selector: 'ngx-store-detail-info',
  standalone: false,
  templateUrl: './store-detail-info.component.html',
  styleUrls: ['./store-detail-info.component.scss']
})
export class StoreDetailInfoComponent implements OnInit {
  store;

  constructor(
    private storeService: StoreService,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService
  ) {
  }

  ngOnInit() {
    const store = this.activatedRoute.snapshot.paramMap.get('code');
    this.storeService.getStore(store)
      .subscribe(res => {
        this.store = res;
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }
}
