import {Component, OnInit, inject} from '@angular/core';
import {RouterLink} from "@angular/router";
import {PricingFacade} from './facades/pricing.facade';

@Component({
  selector: 'app-pricing',
  standalone: true,
  imports: [
    RouterLink
  ],
  providers: [PricingFacade],
  templateUrl: './pricing.component.html',
  styleUrl: './pricing.component.css'
})
export class PricingComponent implements OnInit {
  protected readonly facade = inject(PricingFacade);

  title = 'Unlock Full Power Of Cvhome';
  desc = 'Choose your Plan that fit your business.';

  ngOnInit(): void {
    this.facade.init();
  }

  toggle(): void {
    this.facade.toggle();
  }
}
