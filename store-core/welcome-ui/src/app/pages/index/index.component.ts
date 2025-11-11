import {Component} from '@angular/core';
import {ContactComponent} from "../../sections/contact/contact.component";
import {FeaturesComponent} from "../../sections/features/features.component";
import {PricingComponent} from "../../sections/pricing/pricing.component";
import {SubscribeComponent} from "../../sections/subscribe/subscribe.component";
import {WelcomeComponent} from "../../sections/welcome/welcome.component";

@Component({
  selector: 'app-index',
  standalone: true,
  imports: [
    ContactComponent,
    FeaturesComponent,
    PricingComponent,
    SubscribeComponent,
    WelcomeComponent
  ],
  templateUrl: './index.component.html',
  styleUrl: './index.component.css'
})
export class IndexComponent {

}
