import {Component, OnInit, inject} from '@angular/core';
import {CreateNewUserFacade} from './facades/create-new-user.facade';
import {UserFormComponent} from '../user-form/user-form.component';

@Component({
  selector: 'ngx-create-new-user',
  standalone: true,
  imports: [UserFormComponent],
  templateUrl: './create-new-user.component.html',
  styleUrls: ['./create-new-user.component.scss'],
  providers: [CreateNewUserFacade]
})
export class CreateNewUserComponent implements OnInit {
  protected readonly facade = inject(CreateNewUserFacade);

  ngOnInit() {
    this.facade.init();
  }
}
