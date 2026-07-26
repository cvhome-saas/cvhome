import {Component, OnInit, inject} from '@angular/core';
import {CreateNewUserFacade} from './facades/create-new-user.facade';

@Component({
  selector: 'ngx-create-new-user',
  standalone: false,
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
