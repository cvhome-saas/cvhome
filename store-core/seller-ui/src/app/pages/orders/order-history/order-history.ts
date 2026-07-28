import {Component, inject} from '@angular/core';
import {NbDialogRef} from '@nebular/theme';

@Component({
  selector: 'ngx-order-history',
  standalone: false,
  templateUrl: 'order-history.html',
  styleUrls: ['order-history.scss'],
})
export class OrderHistoryComponent {
  protected readonly ref = inject(NbDialogRef<OrderHistoryComponent>);

  historyData: Array<any>;

  cancel() {
    this.ref.close();
  }

  submit(name?: any) {
    this.ref.close(name);
  }
}
