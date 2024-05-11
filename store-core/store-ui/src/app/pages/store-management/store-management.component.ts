import {Component, DoCheck, OnInit, Input} from '@angular/core';

import {TranslateService} from '@ngx-translate/core';
import {StorageService} from '../shared/services/storage.service';

@Component({
  selector: 'ngx-store-management',
  templateUrl: './store-management.component.html',
  styleUrls: ['./store-management.component.scss']
})
export class StoreManagementComponent implements OnInit, DoCheck {
  path = 'Store';
  showSide = true;

  constructor() {

  }

  ngOnInit() {

  }


  ngDoCheck() {
    this.showSide = window.location.hash.indexOf('stores-list') === -1 &&
      window.location.hash.indexOf('retailer') === -1;
  }

}
