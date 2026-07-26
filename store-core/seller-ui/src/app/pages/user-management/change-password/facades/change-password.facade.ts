import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {map} from 'rxjs/operators';
import {UserService} from '../../../shared/services/user.service';
import {ErrorService} from '../../../shared/services/error.service';
import {ChangePasswordFormService} from '../services/change-password-form.service';
import {USER_DETAILS_SIDE_MENU_LINKS} from '../../constants/user-management.constants';

@Injectable()
export class ChangePasswordFacade {
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly toastr = inject(NbToastrService);
  private readonly translate = inject(TranslateService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly errorService = inject(ErrorService);
  private readonly formService = inject(ChangePasswordFormService);

  readonly loader = signal<boolean>(false);
  readonly errorMessage = signal<string>('');
  readonly selectedItem = signal<string>('1');
  readonly sidemenuLinks = signal<any[]>([]);

  get form() {
    return this.formService.form;
  }

  get password() {
    return this.formService.password;
  }

  get newPassword() {
    return this.formService.newPassword;
  }

  get confirmNewPassword() {
    return this.formService.confirmNewPassword;
  }

  init(): void {
    this.activatedRoute.params
      .pipe(map(it => it['id']))
      .subscribe((userId) => {
        if (userId) {
          const links = USER_DETAILS_SIDE_MENU_LINKS.map(link => ({
            ...link,
            link: link.link.replace('{userId}', userId)
          }));
          this.sidemenuLinks.set(links);
        }
      });
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }
    this.loader.set(true);
    this.errorMessage.set('');
    const passwords = {
      changePassword: this.form.value.newPassword,
      password: this.form.value.password
    };
    this.userService.updatePassword(this.userService.getUserId(), passwords).subscribe({
      next: () => {
        this.loader.set(false);
        this.toastr.success(this.translate.instant('USER.PASSWORD_CHANGED'));
      },
      error: (err) => {
        this.loader.set(false);
        this.errorMessage.set(this.translate.instant('USER.PASSWORD_NOT_MATCH'));
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  goToBack(): void {
    this.router.navigate(['pages/user-management/users']);
  }

  route(link: string): void {
    this.router.navigate([link]);
  }
}
