import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {ThemeModule} from './theme/theme.module';
import {SharedModule} from './shared/shared.module';
import {MenuItem} from './menu-item';
import {MENU_ITEMS} from './pages-menu';
import {NbIconLibraries} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {Roles} from './shared/models/roles';
import {AuthService} from './shared/services/auth.service';

@Component({
  selector: 'app-home',
  template: `
    <ngx-one-column-layout>
      <nb-menu [items]="menu"></nb-menu>
      <router-outlet></router-outlet>
    </ngx-one-column-layout>
  `,
  imports: [
    RouterOutlet,
    ThemeModule,
    SharedModule,
  ]
})
export class HomeComponent {
  menu: MenuItem[] = MENU_ITEMS;

  constructor(private iconLibraries: NbIconLibraries,
              private translateService: TranslateService,
              private authService: AuthService
  ) {
    this.iconLibraries.registerFontPack('font-awesome', {packClass: 'fa', iconClassPrefix: 'fa'});
    this.iconLibraries.setDefaultPack('font-awesome');
    this.translateService.use("en");
    this.translateService.onLangChange.subscribe((lang) => {
      this.translateMenu(this.menu);
    });
    this.authService.getAuthUser().subscribe(auth => {
      this.checkAccess(this.menu, authService.getRoles());
    })

  }

  translateMenu(array) {
    array.forEach((el, index) => {
      el.title = this.translateService.instant(el.key);
      if (el.children) {
        this.translateMenu(el.children);
      }
    });
  }

  checkAccess(menu: MenuItem[], roles: Roles) {
    menu.forEach(el => {
      el.hidden = el.guards && !el.guards.some((guard) => guard(roles));
      if (!el.hidden) {
        if (el.children && el.children.length) {
          this.checkAccess(el.children, roles);
        }
      }
    });
  }
}
