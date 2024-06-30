import {Component, DoCheck, OnInit} from '@angular/core';

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
