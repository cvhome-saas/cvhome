import {Component} from '@angular/core';

import {MENU_ITEMS} from './pages-menu';
import {MenuItem} from "./menu-item";
import {TranslateService} from "@ngx-translate/core";
import {AuthService} from '../shared/services/auth.service';
import {Roles} from "../shared/models/roles";

@Component({
  selector: 'ngx-pages',
  standalone: false,
  styleUrls: ['pages.component.scss'],
  template: `
    <ngx-one-column-layout>
      <nb-menu [items]="menu"></nb-menu>
      <router-outlet></router-outlet>
    </ngx-one-column-layout>
  `,
})
export class PagesComponent {
  menu: MenuItem[];

  constructor(
    private translate: TranslateService,
    private authService: AuthService) {
    this.menu = MENU_ITEMS;
    this.translateMenu(this.menu);
    this.checkAccess(this.menu, this.authService.getRoles());
    this.translate.onLangChange.subscribe((lang) => {
      this.translateMenu(this.menu);
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

  translateMenu(array) {
    array.forEach((el, index) => {
      el.title = this.translate.instant(el.key);
      if (el.children) {
        this.translateMenu(el.children);
      }
    });
  }
}
