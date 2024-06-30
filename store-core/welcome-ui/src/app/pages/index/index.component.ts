import { Component } from '@angular/core';
import {AskedQuestionsComponent} from "../../sections/asked-questions/asked-questions.component";
import {ContactComponent} from "../../sections/contact/contact.component";
import {CounterComponent} from "../../sections/counter/counter.component";
import {DiscoverComponent} from "../../sections/discover/discover.component";
import {DownloadComponent} from "../../sections/download/download.component";
import {FeaturesComponent} from "../../sections/features/features.component";
import {FooterComponent} from "../../components/footer/footer.component";
import {HeaderComponent} from "../../components/header/header.component";
import {PricingComponent} from "../../sections/pricing/pricing.component";
import {ReviewComponent} from "../../sections/review/review.component";
import {ScreenshotsComponent} from "../../sections/screenshots/screenshots.component";
import {ServiceComponent} from "../../sections/service/service.component";
import {SubscribeComponent} from "../../sections/subscribe/subscribe.component";
import {TeamComponent} from "../../sections/team/team.component";
import {WelcomeComponent} from "../../sections/welcome/welcome.component";
import {WorkComponent} from "../../sections/work/work.component";

@Component({
  selector: 'app-index',
  standalone: true,
  imports: [
    AskedQuestionsComponent,
    ContactComponent,
    CounterComponent,
    DiscoverComponent,
    DownloadComponent,
    FeaturesComponent,
    FooterComponent,
    HeaderComponent,
    PricingComponent,
    ReviewComponent,
    ScreenshotsComponent,
    ServiceComponent,
    SubscribeComponent,
    TeamComponent,
    WelcomeComponent,
    WorkComponent
  ],
  templateUrl: './index.component.html',
  styleUrl: './index.component.css'
})
export class IndexComponent {

}
