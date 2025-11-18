import {Component, OnInit} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {UserService} from '../../../shared/services/user.service';
import {User} from '../../../shared/models/user';
import {ErrorService} from "../../../shared/services/error.service";

@Component({
  selector: 'ngx-user-profile',
  standalone:false,
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {
  form: FormGroup;
  user: User;
  loading = false;

  constructor(
    private userService: UserService,
    private errorService: ErrorService
  ) {
  }

  ngOnInit() {
    this.loading = true;
    this.userService.getCurrentAccount().subscribe({
      next: (user) => {
        this.user = user;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

}
