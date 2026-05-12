import {Component,} from '@angular/core';
import {NbDialogRef} from '@nebular/theme';

@Component({
  selector: 'ngx-order-history',
  standalone: false,
  templateUrl: 'order-history.html',
  styleUrls: ['order-history.scss'],
})
export class OrderHistoryComponent {
  historyData: Array<any>;

  constructor(protected ref: NbDialogRef<OrderHistoryComponent>) {
    // console.log(this.historyData)
  }

  cancel() {
    this.ref.close();
  }

  submit(name) {
    this.ref.close(name);
  }
}
