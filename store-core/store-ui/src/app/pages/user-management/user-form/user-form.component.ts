import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';

import {ConfigService} from '../../shared/services/config.service';
import {UserService} from '../../shared/services/user.service';
import {User} from '../../shared/models/user';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {StoreService} from '../../store-management/services/store.service';
import {SecurityService} from '../../shared/services/security.service';
import {StorageService} from '../../shared/services/storage.service';

@Component({
  selector: 'ngx-user-form',
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit {
  form: FormGroup;
  @Input() title: string;
  @Input() action: string = 'CREATE';
  private _user: User;
  @Input() store: string = '';

  @Input()
  set user(user: User) {
    this._user = user;
  }

  get user(): User {
    return this._user;
  }

  allGroups: string[] = [];
  groups = [];
  pwdPattern = '^(?=[^A-Z]*[A-Z])(?=[^a-z]*[a-z])(?=[^0-9]*[0-9]).{6,12}$';
  emailPattern = '^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$';
  selectedItem = '0';
  sidemenuLinks = [
    {
      id: '0',
      title: 'COMPONENTS.MY_PROFILE',
      key: 'COMPONENTS.MY_PROFILE',
      link: '/pages/user-management/profile',
    },
    {
      id: '1',
      title: 'COMPONENTS.CHANGE_PASSWORD',
      key: 'COMPONENTS.CHANGE_PASSWORD',
      link: '/pages/user-management/change-password',
    }
  ];

  loader = false;

  constructor(
    private fb: FormBuilder,
    private configService: ConfigService,
    private userService: UserService,
    private storeService: StoreService,
    private storageService: StorageService,
    private securityService: SecurityService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastr: NbToastrService,
    private translate: TranslateService) {
  }

  ngOnInit() {
    this.userService.groups().subscribe(it => {
      this.allGroups = it;
      if (this.action == 'CREATE') {
        this.groups = this.allGroups.map(it => {
          return {name: it, checked: false}
        });
      } else {
        this.groups = this.allGroups.map(name => {
          const checked = this._user.groups.filter(it => it.name == name).length > 0
          return {name, checked}
        });
      }
    });

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

  fillForm() {
    this.form.get('password').clearValidators();
    this.form.patchValue({
      firstName: this._user.firstName,
      lastName: this._user.lastName,
      userName: this._user.userName,
      password: '',
      repeatPassword: '',
      emailAddress: this._user.emailAddress,
      active: this._user.active,
      groups: [...this._user.groups],
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
    if (this.form.value.groups.length === 0) {
      this.toastr.warning(this.translate.instant('COMMON.ADDING_USER_GROUPS_ERROR'));
      this.loader = false;
      return;
    }
    if (this._user && this._user.id) {
      this.userService.updateUser(+this._user.id, this.form.value, this.store)
        .subscribe({
          next: (res) => {
            this.toastr.success(this.translate.instant('USER_FORM.USER_UPDATED'));
            this.loader = false;
          },
          error: (err) => {
            this.toastr.danger(err.error.detail);
            this.loader = false;
          },
        });
    } else {
      this.userService.createUser(this.form.value, this.store).subscribe({
        next: (res) => {
          this.loader = false;
          this.toastr.success(this.translate.instant('USER_FORM.USER_CREATED'));
          this.router.navigate(['pages/user-management/users']);
        },
        error: (err) => {
          this.toastr.danger(err.error.detail);
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
}
