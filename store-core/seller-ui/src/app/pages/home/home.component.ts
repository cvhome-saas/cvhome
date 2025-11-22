import {Component} from '@angular/core';
import {AuthService} from "../shared/services/auth.service";

@Component({
  selector: 'ngx-home',
  standalone:false,
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  userType: string | undefined;

  constructor(private authService: AuthService) {
    this.authService.getAuthUser().subscribe(it => {
      this.userType = it.user_type;
    })
  }
}
