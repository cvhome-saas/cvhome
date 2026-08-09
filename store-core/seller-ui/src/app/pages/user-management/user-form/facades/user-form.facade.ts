import {Injectable, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {UserService} from 'seller-core';
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {UserFormFormService} from '../services/user-form-form.service';
import {User} from 'seller-core';
import {NEW_USER_SIDE_MENU_LINKS, USER_DETAILS_SIDE_MENU_LINKS} from '../../constants/user-management.constants';

@Injectable()
export class UserFormFacade {
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);
  private readonly formService = inject(UserFormFormService);

  readonly loader = signal<boolean>(false);
  readonly roles = signal<{ name: string; checked: boolean }[]>([]);
  readonly selectedItem = signal<string>('0');
  readonly sidemenuLinks = signal<(typeof USER_DETAILS_SIDE_MENU_LINKS)[number][]>([]);

  get form() {
    return this.formService.form;
  }

  get userName() {
    return this.formService.userName;
  }

  get firstName() {
    return this.formService.firstName;
  }

  get lastName() {
    return this.formService.lastName;
  }

  get emailAddress() {
    return this.formService.emailAddress;
  }

  get password() {
    return this.formService.password;
  }

  get repeatPassword() {
    return this.formService.repeatPassword;
  }

  get userRoles() {
    return this.formService.userRoles;
  }

  init(action: string, store: string, user?: User): void {
    this.formService.buildForm(action);

    if (user && user.id) {
      const links = USER_DETAILS_SIDE_MENU_LINKS.map(link => ({
        ...link,
        link: link.link.replace('{userId}', String(user.id))
      }));
      this.sidemenuLinks.set(links);
    } else {
      this.sidemenuLinks.set(NEW_USER_SIDE_MENU_LINKS);
    }

    this.userService.roles().subscribe({
      next: (allRoles) => {
        if (action === 'CREATE' || !user) {
          this.roles.set(allRoles.map(name => ({ name, checked: false })));
        } else {
          this.roles.set(allRoles.map(name => ({
            name,
            checked: (user.roles || []).includes(name)
          })));
        }
      },
      error: (err) => {
        this.apiErrors.notify(err);
      }
    });

    if (action !== 'CREATE' && user) {
      this.formService.fillForm(user);
    }
  }

  checkRole(name: string): void {
    const updated = this.roles().map(item => {
      if (item.name === name) {
        return { ...item, checked: !item.checked };
      }
      return item;
    });
    this.roles.set(updated);
  }

  save(action: string, store: string, user?: User): void {
    this.loader.set(true);
    const newRoles = this.roles().filter(r => r.checked).map(r => r.name);
    this.form.patchValue({ roles: newRoles });
    const userData = this.form.value;

    if (newRoles.length === 0) {
      this.notify.warning('COMMON.ADDING_USER_ROLES_ERROR');
      this.loader.set(false);
      return;
    }

    if (user && user.id) {
      userData.id = user.id;
      this.userService.updateUser(userData, store).subscribe({
        next: () => {
          this.notify.success('USER_FORM.USER_UPDATED');
          this.loader.set(false);
        },
        error: (err) => {
          this.apiErrors.notify(err);
          this.loader.set(false);
        }
      });
    } else {
      this.userService.createUser(userData, store).subscribe({
        next: () => {
          this.loader.set(false);
          this.notify.success('USER_FORM.USER_CREATED');
          this.router.navigate(['pages/user-management/users']);
        },
        error: (err) => {
          this.apiErrors.notify(err);
          this.loader.set(false);
        }
      });
    }
  }

  goToBack(): void {
    this.router.navigate(['pages/user-management/users']);
  }

  route(link: string): void {
    this.router.navigate([link]);
  }
}
