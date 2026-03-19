import {Component, OnDestroy, OnInit} from '@angular/core';
import {NbMenuService, NbSidebarService} from '@nebular/theme';
import {AuthService} from "../../../shared/services/auth.service";
import {filter, map, Subject, takeUntil} from "rxjs";

@Component({
  selector: 'ngx-header',
  standalone: false,
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  user: any;
  userMenu = [{title: 'Profile'}, {title: 'Log out'}];
  private unsubscribe$ = new Subject<void>();


  constructor(private sidebarService: NbSidebarService,
              private menuService: NbMenuService,
              private authService: AuthService) {
  }

  ngOnInit() {
    this.menuService.onItemClick()
      .pipe(
        takeUntil(this.unsubscribe$),
        filter(({tag}) => tag === 'user-menu'),
        map(({item: {title}}) => title),
      ).subscribe(title => {
      if (title === 'Log out') {
        this.authService.logout();
      }
    });
  }

  toggleSidebar(): boolean {
    this.sidebarService.toggle(true, 'menu-sidebar');
    return false;
  }

  navigateHome() {
    this.menuService.navigateHome();
    return false;
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }
}
