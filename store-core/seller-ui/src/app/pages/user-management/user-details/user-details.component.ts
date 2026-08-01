import {Component, OnInit, inject} from '@angular/core';
import {UserDetailsFacade} from './facades/user-details.facade';
import {UserFormComponent} from '../user-form/user-form.component';

@Component({
  selector: 'ngx-user-details',
  standalone: true,
  imports: [UserFormComponent],
  templateUrl: './user-details.component.html',
  styleUrls: ['./user-details.component.scss'],
  providers: [UserDetailsFacade]
})
export class UserDetailsComponent implements OnInit {
  protected readonly facade = inject(UserDetailsFacade);

  ngOnInit() {
    this.facade.init();
  }
}
