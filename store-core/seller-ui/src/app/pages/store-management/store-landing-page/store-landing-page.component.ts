import {Component, OnInit, inject} from '@angular/core';
import {StoreLandingPageFacade} from './facades/store-landing-page.facade';
import {StoreLandingPageFormService} from './services/store-landing-page.form.service';

@Component({
  selector: 'ngx-store-landing-page',
  standalone: false,
  templateUrl: './store-landing-page.component.html',
  styleUrls: ['./store-landing-page.component.scss'],
  providers: [StoreLandingPageFacade, StoreLandingPageFormService]
})
export class StoreLandingPageComponent implements OnInit {
  protected readonly facade = inject(StoreLandingPageFacade);

  ngOnInit() {
    this.facade.init();
  }
}
