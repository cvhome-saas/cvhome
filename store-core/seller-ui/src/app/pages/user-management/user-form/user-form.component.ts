import {Component, Input, OnInit, inject} from '@angular/core';
import {User} from '../../shared/models/user';
import {UserFormFacade} from './facades/user-form.facade';
import {UserFormFormService} from './services/user-form-form.service';

@Component({
  selector: 'ngx-user-form',
  standalone: false,
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss'],
  providers: [UserFormFacade, UserFormFormService]
})
export class UserFormComponent implements OnInit {
  @Input() title: string;
  @Input() action: string = 'CREATE';
  @Input() store: string = '';
  @Input() user: User;

  protected readonly facade = inject(UserFormFacade);

  ngOnInit() {
    this.facade.init(this.action, this.store, this.user);
  }
}
