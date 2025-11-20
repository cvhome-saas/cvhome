import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'ngx-usage',
  standalone:false,
  templateUrl: './usage.component.html',
  styleUrls: ['./usage.component.scss']
})
export class UsageComponent implements OnInit {
  loadingList = false;

  constructor() {
  }

  ngOnInit() {
  }

}
