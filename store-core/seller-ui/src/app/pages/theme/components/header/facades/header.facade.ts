import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';
import {NbMenuItem, NbMenuService} from '@nebular/theme';
import {filter, map} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AuthService, AuthUser} from 'seller-core';
import {ManagerStoreService} from 'seller-core';
import {ApiErrorService} from 'seller-core';
import {SelectedStoreService} from 'seller-core';
import {SelectedLanguageService} from 'seller-core';
import {ManagerStore} from 'seller-core';
import {HeaderMenuAction, STORE_SELECT_DISABLED_ROUTE_PREFIXES} from '../constants/header.constants';

@Injectable()
export class HeaderFacade {
  private readonly router = inject(Router);
  private readonly menuService = inject(NbMenuService);
  private readonly authService = inject(AuthService);
  private readonly storeService = inject(ManagerStoreService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly selectedLanguageService = inject(SelectedLanguageService);

  readonly languages = this.selectedLanguageService.languages();
  readonly currentLanguage = signal<string>(this.selectedLanguageService.current() ?? '');
  readonly user = signal<AuthUser | null>(null);
  readonly stores = signal<ManagerStore[]>([]);
  readonly selectedStoreId = signal<string | undefined>(undefined);
  readonly isStoreSelectDisabled = signal<boolean>(false);

  init(destroyRef: DestroyRef): void {
    this.checkStoreSelectDisabledState(this.router.url);

    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map((event) => event as NavigationEnd),
      takeUntilDestroyed(destroyRef)
    ).subscribe((event) => {
      this.checkStoreSelectDisabledState(event.urlAfterRedirects || event.url);
    });

    this.selectedStoreService.current()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe((store) => this.selectedStoreId.set(store));

    this.authService.getAuthUser()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe((user) => this.user.set(user));

    this.storeService.list()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (page) => this.stores.set(page.content),
        error: (err) => this.apiErrors.notify(err)
      });

    this.menuService.onItemClick()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe((event) => this.onUserMenuItemClick(event.item));
  }

  onStoreChange(storeId: string | undefined): void {
    if (storeId != undefined) {
      this.selectedStoreService.select(storeId);
      location.reload();
    }
  }

  onLanguageChange(language: string): void {
    this.selectedLanguageService.select(language);
    location.reload();
  }

  onUserMenuItemClick(item: NbMenuItem): void {
    if (item.title === HeaderMenuAction.Profile) {
      this.router.navigate(['/pages/user-management/profile']);
    }
    if (item.title === HeaderMenuAction.Logout) {
      this.authService.logout();
    }
  }

  private checkStoreSelectDisabledState(currentUrl: string): void {
    this.isStoreSelectDisabled.set(
      STORE_SELECT_DISABLED_ROUTE_PREFIXES.some((pattern) => currentUrl.startsWith(pattern))
    );
  }
}
