import {Component, OnDestroy, OnInit} from '@angular/core';
import {NbMediaBreakpointsService, NbMenuService, NbSidebarService, NbThemeService} from '@nebular/theme';

import {map, takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';
import {TranslateService} from "@ngx-translate/core";
import {NbMenuItem} from "@nebular/theme/components/menu/menu.service";
import {Router} from "@angular/router";
import {AuthService} from "../../../shared/service/auth.service";

@Component({
  selector: 'ngx-header',
  styleUrls: ['./header.component.scss'],
  templateUrl: './header.component.html',
})
export class HeaderComponent implements OnInit, OnDestroy {

  private destroy$: Subject<void> = new Subject<void>();
  userPictureOnly: boolean = false;
  user: any;

  themes = [
    {
      value: 'default',
      name: 'Light',
    },
    {
      value: 'dark',
      name: 'Dark',
    },
    {
      value: 'cosmic',
      name: 'Cosmic',
    },
    {
      value: 'corporate',
      name: 'Corporate',
    },
  ];
  languages = ["en", "ar", "es", "ru", "fr"]
  currentLanguage = this.languages[0]
  currentTheme = 'default';

  userMenu = [{title: 'Profile'}, {title: 'Log out'}];

  constructor(private sidebarService: NbSidebarService,
              private menuService: NbMenuService,
              private themeService: NbThemeService,
              private authService:AuthService,
              private router: Router,
              private breakpointService: NbMediaBreakpointsService,
              private translateService: TranslateService
  ) {
  }

  ngOnInit() {
    this.currentTheme = this.themeService.currentTheme;
    this.menuService.onItemClick().subscribe(it => this.onNbMenuItemClick(it.item));
    this.authService.getAuthUser().subscribe(it => {
      console.log("is this user")
      console.log(it)
      this.user = it
    });


    const {xl} = this.breakpointService.getBreakpointsMap();
    this.themeService.onMediaQueryChange()
      .pipe(
        map(([, currentBreakpoint]) => currentBreakpoint.width < xl),
        takeUntil(this.destroy$),
      )
      .subscribe((isLessThanXl: boolean) => this.userPictureOnly = isLessThanXl);

    this.themeService.onThemeChange()
      .pipe(
        map(({name}) => name),
        takeUntil(this.destroy$),
      )
      .subscribe(themeName => this.currentTheme = themeName);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  changeTheme(themeName: string) {
    this.themeService.changeTheme(themeName);
  }

  changeLanguage(language: string) {
    this.translateService.use(language);
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
      this.router.navigate(['external-logout-link'])
    }
  }
}
