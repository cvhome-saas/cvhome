import { Component } from '@angular/core';
import {SharedModule} from '../../shared/shared.module';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-users',
  imports: [
    SharedModule,
    RouterOutlet,
  ],
  template:`
    <div class="page-content">
      <router-outlet/>
    </div>
  `,
})
export class UsersComponent {

}
