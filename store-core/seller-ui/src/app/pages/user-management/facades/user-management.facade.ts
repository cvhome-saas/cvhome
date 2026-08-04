import {Injectable, inject, signal} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

@Injectable()
export class UserManagementFacade {
  private readonly translate = inject(TranslateService);

  readonly showSide = signal<boolean>(false);
  readonly sideMenuLinks = signal([
    {
      title: 'COMPONENTS.MY_PROFILE',
      key: 'COMPONENTS.MY_PROFILE',
      link: 'profile'
    },
    {
      title: 'COMPONENTS.CHANGE_PASSWORD',
      key: 'COMPONENTS.CHANGE_PASSWORD',
      link: 'change-password'
    }
  ]);

  constructor() {
    this.updateTranslations();
    this.translate.onLangChange
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.updateTranslations());
  }

  checkHash(): void {
    if (typeof window !== 'undefined' && window.location) {
      const hash = window.location.hash;
      this.showSide.set(
        hash.indexOf('users') === -1 && hash.indexOf('create-user') === -1
      );
    }
  }

  private updateTranslations(): void {
    const updated = this.sideMenuLinks().map(item => ({
      ...item,
      title: this.translate.instant(item.key)
    }));
    this.sideMenuLinks.set(updated);
  }
}
