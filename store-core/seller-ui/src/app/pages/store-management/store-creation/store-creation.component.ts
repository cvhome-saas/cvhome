import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'ngx-store-creation',
  standalone:false,
  templateUrl: './store-creation.component.html',
  styleUrls: ['./store-creation.component.scss']
})
export class StoreCreationComponent implements OnInit {
  store: any;

  constructor() {
  }

  ngOnInit() {
  }

}
