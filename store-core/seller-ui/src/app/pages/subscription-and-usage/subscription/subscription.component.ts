import {Component, OnInit, inject} from '@angular/core';
import {NbAlertModule, NbButtonModule, NbCardModule, NbSpinnerModule, NbToggleModule} from '@nebular/theme';
import {TranslateModule} from '@ngx-translate/core';
import {SubscriptionFacade} from '../facades/subscription.facade';

@Component({
  selector: 'ngx-subscription',
  standalone: true,
  imports: [NbCardModule, NbToggleModule, NbAlertModule, NbSpinnerModule, NbButtonModule, TranslateModule],
  templateUrl: './subscription.component.html',
  styleUrls: ['./subscription.component.scss'],
  providers: [SubscriptionFacade]
})
export class SubscriptionComponent implements OnInit {
  protected readonly facade = inject(SubscriptionFacade);

  ngOnInit(): void {
    this.facade.init();
  }
}
