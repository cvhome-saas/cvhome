import {Component, OnInit, inject} from '@angular/core';
import {UserDetailsFacade} from './facades/user-details.facade';

@Component({
  selector: 'ngx-user-details',
  standalone: false,
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
