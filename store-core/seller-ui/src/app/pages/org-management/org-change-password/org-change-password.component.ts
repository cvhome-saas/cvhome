import {Component, OnInit, inject} from '@angular/core';
import {OrgChangePasswordFacade} from './facades/org-change-password.facade';
import {OrgChangePasswordFormService} from './services/org-change-password-form.service';

@Component({
  selector: 'ngx-org-change-password',
  standalone: false,
  templateUrl: './org-change-password.component.html',
  styleUrls: ['./org-change-password.component.scss'],
  providers: [OrgChangePasswordFacade, OrgChangePasswordFormService]
})
export class OrgChangePasswordComponent implements OnInit {
  protected readonly facade = inject(OrgChangePasswordFacade);

  ngOnInit() {
    this.facade.init();
  }
}
