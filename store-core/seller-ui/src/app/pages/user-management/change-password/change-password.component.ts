import {Component, OnInit, inject} from '@angular/core';
import {ChangePasswordFacade} from './facades/change-password.facade';
import {ChangePasswordFormService} from './services/change-password-form.service';

@Component({
  selector: 'ngx-change-password',
  standalone: false,
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
