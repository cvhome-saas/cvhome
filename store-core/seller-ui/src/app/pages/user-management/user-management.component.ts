import {Component, DoCheck, inject} from '@angular/core';
import {UserManagementFacade} from './facades/user-management.facade';

@Component({
  selector: 'ngx-user-management',
  standalone: false,
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss'],
  providers: [UserManagementFacade]
})
export class UserManagementComponent implements DoCheck {
  protected readonly facade = inject(UserManagementFacade);

  ngDoCheck() {
    this.facade.checkHash();
  }
}
