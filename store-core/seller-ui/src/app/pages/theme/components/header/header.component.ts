import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {
  NbActionsModule,
  NbContextMenuModule,
  NbIconModule,
  NbMenuService,
  NbOptionModule,
  NbSearchModule,
  NbSelectModule,
  NbSidebarService,
  NbUserModule
} from '@nebular/theme';
import {ManagerStoreId} from '../../../shared/models/commons';
import {HEADER_USER_MENU} from './constants/header.constants';
import {HeaderFacade} from './facades/header.facade';

@Component({
  selector: 'ngx-header',
  standalone: true,
  imports: [NbIconModule, NbSelectModule, NbOptionModule, NbActionsModule, NbSearchModule, NbUserModule, NbContextMenuModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  providers: [HeaderFacade]
})
export class HeaderComponent implements OnInit {
  protected readonly facade = inject(HeaderFacade);
  protected readonly userMenu = HEADER_USER_MENU;
  protected readonly userPictureOnly = false;

  private readonly sidebarService = inject(NbSidebarService);
  private readonly menuService = inject(NbMenuService);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }

  onStoreChange(id: ManagerStoreId): void {
    this.facade.onStoreChange(id);
  }

  onLanguageChange(language: string): void {
    this.facade.onLanguageChange(language);
  }

  toggleSidebar(): boolean {
    this.sidebarService.toggle(true, 'menu-sidebar');
    return false;
  }

  navigateHome(): boolean {
    this.menuService.navigateHome();
    return false;
  }
}
