import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';

import {UserService} from '../../shared/services/user.service';
import {User} from '../../shared/models/user';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from "../../shared/services/error.service";
import {map} from "rxjs/operators";

@Component({
  selector: 'ngx-change-password',
  standalone: false,
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnInit {

  form: FormGroup;
  loader = false;
  pwdPattern = '^(?=[^A-Z]*[A-Z])(?=[^a-z]*[a-z])(?=[^0-9]*[0-9]).{6,12}$';
  errorMessage = '';
  selectedItem = '1';
  sidemenuLinks = [];

  constructor(
    private userService: UserService,
    private fb: FormBuilder,
    private router: Router,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService
  ) {
    this.createForm();
  }

  private _user: User;

  get user(): User {
    return this._user;
  }

  get password(): any {
    return this.form.get('password');
  }

  get newPassword(): any {
    return this.form.get('newPassword');
  }

  get confirmNewPassword(): any {
    return this.form.get('confirmNewPassword');
  }

  ngOnInit() {
    this.activatedRoute.params.pipe(map(it => it['id']))
      .subscribe({
        next: (res) => {
          this.sidemenuLinks = [
            {
              id: '0',
              title: 'COMPONENTS.USER_DETAILS',
              key: 'COMPONENTS.USER_DETAILS',
              link: `/pages/user-management/user/${res}`,
            },
            {
              id: '1',
              title: 'COMPONENTS.CHANGE_PASSWORD',
              key: 'COMPONENTS.CHANGE_PASSWORD',
              link: `/pages/user-management/change-password/${res}`,
            }
          ];
        },
      })
  }

  checkPasswords(group: FormGroup) {
    const pass = group.controls.newPassword.value;
    const confirmPass = group.controls.confirmNewPassword.value;

    return pass === confirmPass ? null : {notSame: true};
  }

  goToBack() {
    this.router.navigate(['pages/user-management/users']);
  }

  route(link) {
    this.router.navigate([link]);
  }

  save() {
    this.loader = true;
    const passwords = {
      changePassword: this.form.value.newPassword,
      password: this.form.value.password
    };
    this.userService.updatePassword(this.userService.getUserId(), passwords)
      .subscribe(res => {
        this.loader = false;
        this.toastr.success(this.translate.instant('USER.PASSWORD_CHANGED'));
      }, err => {
        this.loader = false;
        this.errorMessage = this.translate.instant('USER.PASSWORD_NOT_MATCH');
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  private createForm() {
    this.form = this.fb.group({
      password: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.pattern(this.pwdPattern)]],
      confirmNewPassword: ['', [Validators.required]],
    }, {validator: this.checkPasswords});
  }

}
