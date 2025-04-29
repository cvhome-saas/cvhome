import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'ngx-brand-creation',
  standalone:false,
  templateUrl: './brand-creation.component.html',
  styleUrls: ['./brand-creation.component.scss']
})
export class BrandCreationComponent implements OnInit {
  brand = {};
  store: string;

  constructor(private activatedRoute: ActivatedRoute) {

  }


  ngOnInit() {
    this.activatedRoute.params.subscribe(it => {
      let split: string[] = it["id"].split("-");
      this.store = split[0];
    });
  }

}
