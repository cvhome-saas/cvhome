import {Component, inject} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbDialogRef} from '@nebular/theme';
import {ReadableOrderStatusHistory} from '../models/order.model';

@Component({
  selector: 'ngx-order-history',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: 'order-history.html',
  styleUrls: ['order-history.scss'],
})
export class OrderHistoryComponent {
  protected readonly ref = inject(NbDialogRef<OrderHistoryComponent>);

  historyData: ReadableOrderStatusHistory[];

  cancel() {
    this.ref.close();
  }

  submit(name?: string) {
    this.ref.close(name);
  }
}
