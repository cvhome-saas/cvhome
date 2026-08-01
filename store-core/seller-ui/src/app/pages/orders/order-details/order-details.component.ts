import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbCardModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule
} from '@nebular/theme';
import {OrderDetailsFacade} from './facades/order-details.facade';

@Component({
  selector: 'ngx-order-details',
  standalone: true,
  imports: [
    FormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule
  ],
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.scss'],
  providers: [OrderDetailsFacade]
})
export class OrderDetailsComponent implements OnInit {
  protected readonly facade = inject(OrderDetailsFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }

  onBillingChange(value: any, flag: number): void {
    this.facade.onBillingChange(value, flag);
  }

  onShippingChange(value: any, flag: number): void {
    this.facade.onShippingChange(value, flag);
  }

  goToBack(): void {
    this.facade.goBack();
  }

  showDialog(value: any): void {
    this.facade.showDialog(value);
  }

  onPhoneChange(phone: string): void {
    this.facade.onPhoneChange(phone);
  }

  onShippingPhoneChange(phone: string): void {
    this.facade.onShippingPhoneChange(phone);
  }

  updateHistory(): void {
    this.facade.updateHistory();
  }

  save(): void {
    this.facade.save();
  }

  onClickRefund(): void {
    this.facade.onClickRefund();
  }

  onClickCapture(): void {
    this.facade.onClickCapture();
  }
}
