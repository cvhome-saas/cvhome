import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {NgOptimizedImage} from "@angular/common";
import {HeaderComponent} from "./components/header/header.component";
import {ContactComponent} from "./sections/contact/contact.component";
import {PricingComponent} from "./sections/pricing/pricing.component";
import {AskedQuestionsComponent} from "./sections/asked-questions/asked-questions.component";
import {TeamComponent} from "./sections/team/team.component";
import {SubscribeComponent} from "./sections/subscribe/subscribe.component";
import {ReviewComponent} from "./sections/review/review.component";
import {ScreenshotsComponent} from "./sections/screenshots/screenshots.component";
import {WorkComponent} from "./sections/work/work.component";
import {DiscoverComponent} from "./sections/discover/discover.component";
import {ServiceComponent} from "./sections/service/service.component";
import {FeaturesComponent} from "./sections/features/features.component";
import {CounterComponent} from "./sections/counter/counter.component";
import {WelcomeComponent} from "./sections/welcome/welcome.component";
import {DownloadComponent} from "./sections/download/download.component";
import {FooterComponent} from "./components/footer/footer.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NgOptimizedImage, HeaderComponent, ContactComponent, PricingComponent, AskedQuestionsComponent, TeamComponent, SubscribeComponent, ReviewComponent, ScreenshotsComponent, WorkComponent, DiscoverComponent, ServiceComponent, FeaturesComponent, CounterComponent, WelcomeComponent, DownloadComponent, FooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'welcome-ui';
}
