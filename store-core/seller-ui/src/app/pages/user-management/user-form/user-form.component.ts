import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';

import {UserService} from '../../shared/services/user.service';
import {User} from '../../shared/models/user';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from "../../shared/services/error.service";

@Component({
  selector: 'ngx-user-form',
  standalone: false,
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit {
  form: FormGroup;
  @Input() title: string;
  @Input() action: string = 'CREATE';
  @Input() store: string = '';
  @Input() user: User;
  allGroups: string[] = [];
  groups = [];
  pwdPattern = '^(?=[^A-Z]*[A-Z])(?=[^a-z]*[a-z])(?=[^0-9]*[0-9]).{6,12}$';
  emailPattern = '^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$';
  selectedItem = '0';
  sidemenuLinks = []
  loader = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private errorService: ErrorService
  ) {
  }


  get userName() {
    return this.form.get('userName');
  }

  get firstName() {
    return this.form.get('firstName');
  }

  get lastName() {
    return this.form.get('lastName');
  }

  get emailAddress() {
    return this.form.get('emailAddress');
  }

  get password() {
    return this.form.get('password');
  }

  get repeatPassword() {
    return this.form.get('repeatPassword');
  }

  get userGroups() {
    return this.form.get('groups');
  }

  ngOnInit() {
    this.userService.groups()
      .subscribe({
        next: (it) => {
          this.allGroups = it;
          if (this.action == 'CREATE') {
            this.groups = this.allGroups.map(it => {
              return {name: it, checked: false}
            });
          } else {
            this.groups = this.allGroups.map(name => {
              const checked = this.user.groups.filter(it => it.name == name).length > 0
              return {name, checked}
            });
          }
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });

    if (this.user) {
      this.sidemenuLinks = [
        {
          id: '0',
          title: 'COMPONENTS.USER_DETAILS',
          key: 'COMPONENTS.USER_DETAILS',
          link: `/pages/user-management/user/${this.user.id}`,
        },
        {
          id: '1',
          title: 'COMPONENTS.CHANGE_PASSWORD',
          key: 'COMPONENTS.CHANGE_PASSWORD',
          link: `/pages/user-management/change-password/${this.user.id}`,
        }
      ];
    } else {
      this.sidemenuLinks = [
        {
          id: '0',
          title: 'COMPONENTS.USER_DETAILS',
          key: 'COMPONENTS.USER_DETAILS',
          link: `/pages/user-management/user`,
        },
        {
          id: '1',
          title: 'COMPONENTS.CHANGE_PASSWORD',
          key: 'COMPONENTS.CHANGE_PASSWORD',
          link: `/pages/user-management/change-password`,
        }
      ];
    }

    this.createForm();

    if (this.action == 'CREATE') {
      this.form.get('password').setValidators([Validators.required, Validators.pattern(this.pwdPattern)]);
      this.form.get('repeatPassword').setValidators([Validators.required]);
    } else {
      this.fillForm();
    }

  }

  checkPasswords(group: FormGroup) { // here we have the 'passwords' group
    const password = group.get('password').value;
    const confirmPassword = group.get('repeatPassword').value;
    return password === confirmPassword ? null : {notSame: true}
  }

  fillForm() {
    this.form.get('password').clearValidators();
    this.form.patchValue({
      firstName: this.user.firstName,
      lastName: this.user.lastName,
      userName: this.user.userName,
      password: '',
      repeatPassword: '',
      emailAddress: this.user.emailAddress,
      active: this.user.active,
      groups: [...this.user.groups],
    });
    this.cdr.detectChanges();
    this.findInvalidControls();
  }

  save() {
    this.loader = true;
    const newGroups = [];
    this.groups.forEach((el) => {
      if (el.checked) {
        newGroups.push({name: el.name});
      }
    });
    this.form.patchValue({groups: newGroups});
    const userData = this.form.value;
    if (userData.groups.length === 0) {
      this.toastr.warning(this.translate.instant('COMMON.ADDINGuser_GROUPS_ERROR'));
      this.loader = false;
      return;
    }
    if (this.user && this.user.id) {
      userData.id = this.user.id;
      this.userService.updateUser(userData, this.store)
        .subscribe({
          next: (res) => {
            this.toastr.success(this.translate.instant('USER_FORM.USER_UPDATED'));
            this.loader = false;
          },
          error: (err) => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
            this.loader = false;
          },
        });
    } else {
      this.userService.createUser(userData, this.store).subscribe({
        next: (res) => {
          this.loader = false;
          this.toastr.success(this.translate.instant('USER_FORM.USER_CREATED'));
          this.router.navigate(['pages/user-management/users']);
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
          this.loader = false;
        }
      });
    }
  }

  public findInvalidControls() {
    const invalid = [];
    const controls = this.form.controls;
    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
  }

  route(link) {
    this.router.navigate([link]);
  }

  goToBack() {
    this.router.navigate(['pages/user-management/users']);
  }

  checkGroup(name) {
    this.groups.forEach(it => {
      if (it.name == name) {
        it.checked = !it.checked;
      }
    });

  }

  private createForm() {
    const emailValidators = [Validators.required, Validators.email, Validators.pattern(this.emailPattern)];
    const disabled = this.action == 'VIEW';
    this.form = this.fb.group({
      firstName: new FormControl({value: '', disabled: disabled}, Validators.required),
      lastName: new FormControl({value: '', disabled: disabled}, Validators.required),
      userName: new FormControl({value: '', disabled: disabled}, Validators.required),
      emailAddress: new FormControl({value: '', disabled: disabled}, emailValidators),
      password: new FormControl({value: '', disabled: disabled}),
      repeatPassword: new FormControl({value: '', disabled: disabled}),
      active: new FormControl({value: false, disabled: disabled}),
      groups: new FormControl({value: [], disabled: disabled}),
    }, {validators: this.checkPasswords});
  }
}
