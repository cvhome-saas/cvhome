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
import {ChangePasswordFacade} from './facades/change-password.facade';
import {ChangePasswordFormService} from './services/change-password-form.service';

@Component({
  selector: 'ngx-change-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule
  ],
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss'],
  providers: [ChangePasswordFacade, ChangePasswordFormService]
})
export class ChangePasswordComponent implements OnInit {
  protected readonly facade = inject(ChangePasswordFacade);

  ngOnInit() {
    this.facade.init();
  }
}
