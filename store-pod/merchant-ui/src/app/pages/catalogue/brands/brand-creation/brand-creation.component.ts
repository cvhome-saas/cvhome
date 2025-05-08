import {Component, OnInit} from '@angular/core';
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";

@Component({
  selector: 'ngx-brand-creation',
  standalone: false,
  templateUrl: './brand-creation.component.html',
  styleUrls: ['./brand-creation.component.scss']
})
export class BrandCreationComponent implements OnInit {
  brand = {};
  store: string;

  constructor(private selectedStoreService: SelectedStoreService) {

  }


  ngOnInit() {
    this.selectedStoreService.current().subscribe(it => {
      this.store = it;
    })
  }

}
