import {Component, OnInit, inject} from '@angular/core';
import {StoreDetailInfoFacade} from './facades/store-detail-info.facade';
import {StoreFormComponent} from '../store-form/store-form.component';

@Component({
  selector: 'ngx-store-detail-info',
  standalone: true,
  imports: [StoreFormComponent],
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
