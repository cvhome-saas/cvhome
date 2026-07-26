import {Component, OnInit, inject} from '@angular/core';
import {StoreDetailInfoFacade} from './facades/store-detail-info.facade';

@Component({
  selector: 'ngx-store-detail-info',
  standalone: false,
  templateUrl: './store-detail-info.component.html',
  styleUrls: ['./store-detail-info.component.scss'],
  providers: [StoreDetailInfoFacade]
})
export class StoreDetailInfoComponent implements OnInit {
  protected readonly facade = inject(StoreDetailInfoFacade);

  ngOnInit() {
    this.facade.init();
  }
}
