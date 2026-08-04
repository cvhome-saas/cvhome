import {Component, OnInit, inject} from '@angular/core';
import {LowerCasePipe} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule
} from '@nebular/theme';
import {StoreSocialLoginFacade} from './facades/store-social-login.facade';
import {StoreSocialLoginFormService} from './services/store-social-login.form.service';

@Component({
  selector: 'ngx-store-social-login',
  standalone: true,
  imports: [
    LowerCasePipe,
    ReactiveFormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbCheckboxModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule
  ],
  templateUrl: './store-social-login.component.html',
  styleUrls: ['./store-social-login.component.scss'],
  providers: [StoreSocialLoginFacade, StoreSocialLoginFormService]
})
export class StoreSocialLoginComponent implements OnInit {
  protected readonly facade = inject(StoreSocialLoginFacade);

  ngOnInit() {
    this.facade.init();
  }
}
