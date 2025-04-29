import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {NgOptimizedImage} from "@angular/common";
import {HeaderComponent} from "./components/header/header.component";
import {ContactComponent} from "./sections/contact/contact.component";
import {PricingComponent} from "./sections/pricing/pricing.component";
import {SubscribeComponent} from "./sections/subscribe/subscribe.component";
import {FeaturesComponent} from "./sections/features/features.component";
import {WelcomeComponent} from "./sections/welcome/welcome.component";
import {FooterComponent} from "./components/footer/footer.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NgOptimizedImage, HeaderComponent, ContactComponent, PricingComponent, SubscribeComponent, FeaturesComponent, WelcomeComponent, FooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'welcome-ui';
}
