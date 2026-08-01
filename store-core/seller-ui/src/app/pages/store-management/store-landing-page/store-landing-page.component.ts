import {Component, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbCardModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule
} from '@nebular/theme';
import {QuillModule} from 'ngx-quill';
import {StoreLandingPageFacade} from './facades/store-landing-page.facade';
import {StoreLandingPageFormService} from './services/store-landing-page.form.service';

@Component({
  selector: 'ngx-store-landing-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule,
    QuillModule
  ],
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
