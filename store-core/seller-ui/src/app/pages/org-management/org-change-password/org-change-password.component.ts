import {Component, DestroyRef, OnInit, inject} from '@angular/core';
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
import {OrgChangePasswordFacade} from './facades/org-change-password.facade';
import {OrgChangePasswordFormService} from './services/org-change-password-form.service';

@Component({
  selector: 'ngx-org-change-password',
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
  templateUrl: './org-change-password.component.html',
  styleUrls: ['./org-change-password.component.scss'],
  providers: [OrgChangePasswordFacade, OrgChangePasswordFormService]
})
export class OrgChangePasswordComponent implements OnInit {
  protected readonly facade = inject(OrgChangePasswordFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit() {
    this.facade.init(this.destroyRef);
  }
}
