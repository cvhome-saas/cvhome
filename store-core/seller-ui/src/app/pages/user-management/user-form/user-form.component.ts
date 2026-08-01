import {Component, Input, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule
} from '@nebular/theme';
import {User} from '../../shared/models/user';
import {UserFormFacade} from './facades/user-form.facade';
import {UserFormFormService} from './services/user-form-form.service';

@Component({
  selector: 'ngx-user-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbCheckboxModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule
  ],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss'],
  providers: [UserFormFacade, UserFormFormService]
})
export class UserFormComponent implements OnInit {
  @Input() title: string;
  @Input() action = 'CREATE';
  @Input() store = '';
  @Input() user: User;

  protected readonly facade = inject(UserFormFacade);

  ngOnInit() {
    this.facade.init(this.action, this.store, this.user);
  }
}
