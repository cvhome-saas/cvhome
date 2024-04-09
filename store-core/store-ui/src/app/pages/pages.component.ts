import {Component} from '@angular/core';

import {MENU_ITEMS} from './pages-menu';
import {MenuItem} from "./menu-item";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'ngx-pages',
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
  roles = {
    isAdmin: true,
    canAccessToOrder: true,
    isAdminCatalogue: true,
    isAdminContent: true,
    isAdminOrder: true,
    isAdminRetail: true,
    isAdminStore: true,
    isCustomer: true,
    isSuperadmin: true,
  };

  constructor(
    private translate: TranslateService,
  ) {
    this.menu = MENU_ITEMS;
    this.translateMenu(this.menu);
    this.checkAccess(this.menu);
    this.translate.onLangChange.subscribe((lang) => {
      this.translateMenu(this.menu);
    });
  }

  checkAccess(menu) {
    menu.forEach(el => {
      el.hidden = el.guards && !el.guards.some((guard) => guard(this.roles));
      if (!el.hidden) {
        if (el.children && el.children.length) {
          this.checkAccess(el.children);
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
