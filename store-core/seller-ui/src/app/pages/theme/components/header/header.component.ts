import {Component, OnDestroy, OnInit} from '@angular/core';
import {NbMediaBreakpointsService, NbMenuItem, NbMenuService, NbSidebarService} from '@nebular/theme';

import {filter, map, Subject, takeUntil} from 'rxjs';
import {TranslateService} from "@ngx-translate/core";
import {NavigationEnd, Router} from "@angular/router";
import {AuthService} from "../../../shared/services/auth.service";
import {StoreService} from "../../../shared/services/store.service";
import {ErrorService} from "../../../shared/services/error.service";
import {ManagerStoreId, Store} from "../../../shared/models/commons";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {SelectedLanguageService} from '../../../shared/services/selected-language.service';

@Component({
  selector: 'ngx-header',
  standalone: false,
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {

  isStoreSelectDisabled: boolean = false;
  userPictureOnly: boolean = false;
  user: any;
  languages: string[];
  currentLanguage: string;
  userMenu = [{title: 'Profile'}, {title: 'Log out'}];
  selectedStoreId: string | undefined;
  stores: Store[] | undefined;
  private destroy$: Subject<void> = new Subject<void>();

  constructor(private sidebarService: NbSidebarService,
              private menuService: NbMenuService,
              private router: Router,
              private breakpointService: NbMediaBreakpointsService,
              private translateService: TranslateService,
              private authService: AuthService,
              private storeService: StoreService,
              private selectedStoreService: SelectedStoreService,
              private selectedLanguageService: SelectedLanguageService,
              private errorService: ErrorService
  ) {
    this.languages = this.selectedLanguageService.languages();
    this.currentLanguage = this.selectedLanguageService.current();
    this.translateService.use(this.currentLanguage);
  }

  changed($event: ManagerStoreId) {
    if ($event != undefined && $event.id != undefined) {
      this.selectedStoreService.select($event.id);
      location.reload();
    }
  }

  ngOnInit() {
    this.checkStoreSelectDisabledState(this.router.url);
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map(it => (<NavigationEnd>it)),
      takeUntil(this.destroy$)
    ).subscribe((event: NavigationEnd) => {
      this.checkStoreSelectDisabledState(event.urlAfterRedirects || event.url);
    });
    this.selectedStoreService.current().subscribe(it => {
      this.selectedStoreId = it;
    });
    this.menuService.onItemClick().subscribe(it => this.onNbMenuItemClick(it.item));
    this.authService.getAuthUser().subscribe(it => {
      this.user = it
    });
    this.storeService.list()
      .subscribe({
        next: (it) => {
          this.stores = it.content;
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });


    const {xl} = this.breakpointService.getBreakpointsMap();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  changeLanguage(language: string) {
    this.selectedLanguageService.select(language);
    location.reload();
  }

  toggleSidebar(): boolean {
    this.sidebarService.toggle(true, 'menu-sidebar');
    return false;
  }

  navigateHome() {
    this.menuService.navigateHome();
    return false;
  }

  onNbMenuItemClick(item: NbMenuItem) {
    if (item.title == 'Profile') {
      this.router.navigate(['/pages/user-management/profile'])
    }
    if (item.title == 'Log out') {
      this.authService.logout();
    }
  }

  private checkStoreSelectDisabledState(currentUrl: string): void {
    const disablePatterns = [
      '/pages/store-management',
      '/pages/subscription-and-usage',
      '/pages/org-management'
    ];
    this.isStoreSelectDisabled = disablePatterns.some(pattern => currentUrl.startsWith(pattern));
  }
}
