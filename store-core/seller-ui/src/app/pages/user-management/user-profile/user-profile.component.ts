import {Component, OnInit, inject} from '@angular/core';
import {UserProfileFacade} from './facades/user-profile.facade';
import {UserFormComponent} from '../user-form/user-form.component';

@Component({
  selector: 'ngx-user-profile',
  standalone: true,
  imports: [UserFormComponent],
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss'],
  providers: [UserProfileFacade]
})
export class UserProfileComponent implements OnInit {
  protected readonly facade = inject(UserProfileFacade);

  ngOnInit() {
    this.facade.init();
  }
}
