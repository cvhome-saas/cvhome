import {Component} from '@angular/core';
import {StoreFormComponent} from '../store-form/store-form.component';
import {ReadableMerchantStoreWithPod} from '../models/store-service.model';

@Component({
  selector: 'ngx-store-creation',
  standalone: true,
  imports: [StoreFormComponent],
  templateUrl: './store-creation.component.html',
  styleUrls: ['./store-creation.component.scss']
})
export class StoreCreationComponent {
  store: ReadableMerchantStoreWithPod;
}
