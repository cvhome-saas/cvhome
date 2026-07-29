import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AuthService} from '../shared/services/auth.service';
import {SelectedStoreService} from '../shared/services/selected-store.service';
import {SelectedLanguageService} from '../shared/services/selected-language.service';
import {Roles} from '../shared/models/roles';
import {MenuItem} from '../menu-item';
import {MENU_ITEMS} from '../pages-menu';

@Injectable()
export class PagesFacade {
  private readonly translate = inject(TranslateService);
  private readonly authService = inject(AuthService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly selectedLanguageService = inject(SelectedLanguageService);

  readonly menu = signal<MenuItem[]>(MENU_ITEMS);
  readonly storesReady = signal<boolean>(false);

  init(destroyRef: DestroyRef): void {
    this.applyMenuState();

    this.translate.use(this.selectedLanguageService.current() ?? 'en');

    this.translate.onLangChange
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe(() => this.applyMenuState());

    this.selectedStoreService.current()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        complete: () => this.storesReady.set(true)
      });
  }

  private applyMenuState(): void {
    this.translateMenu(MENU_ITEMS);
    this.applyAccess(MENU_ITEMS, this.authService.getRoles());
    this.menu.set([...MENU_ITEMS]);
  }

  private translateMenu(items: MenuItem[]): void {
    items.forEach((item) => {
      item.title = this.translate.instant(item.key);
      if (item.children) {
        this.translateMenu(item.children);
      }
    });
  }

  private applyAccess(items: MenuItem[], roles: Roles): void {
    items.forEach((item) => {
      item.hidden = item.guards && !item.guards.some((guard) => guard(roles));
      if (!item.hidden && item.children?.length) {
        this.applyAccess(item.children, roles);
      }
    });
  }
}
