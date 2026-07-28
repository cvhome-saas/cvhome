import {Component, OnInit, inject} from '@angular/core';
import {SubscriptionFacade} from '../facades/subscription.facade';

@Component({
  selector: 'ngx-subscription',
  standalone: false,
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
