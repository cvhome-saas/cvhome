import {Component, effect, inject, input, untracked} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import type {PlatformUserRow} from '@models/platform';
import {BusyOverlay, ConfirmDialog, EmptyState, LoadError, NoticeBar, PageHeader, Pagination, Panel, RolesDialog, Select, SetPasswordDialog} from '@cvhome-saas/ui-kit/ui';
import {UserAdminTable, type UserAdminIntent} from '@shared/ui/user-admin-table/user-admin-table';
import {PAGE_SIZE, PlatformUsersFacade} from './facades/platform-users.facade';

/**
 * Every account on the platform.
 *
 * The counterpart to `/users`, which is the open store's team and — because tenancy filters uaa on
 * `{org, store}` — cannot show an org admin at all. This asks uaa directly, so it shows everyone.
 *
 * **No search box.** `AdminService.getUsers` matches `metadata[...]` on equality and offers no query
 * over username, email or name, so the one filter is the organization. A box that searched the
 * twenty rows on screen would be a lie about what it searched.
 */
@Component({
  selector: 'app-platform-users',
  imports: [
    BusyOverlay,
    ConfirmDialog,
    EmptyState,
    LoadError,
    NoticeBar,
    PageHeader,
    Pagination,
    Panel,
    RolesDialog,
    Select,
    SetPasswordDialog,
    TranslocoDirective,
    UserAdminTable,
  ],
  providers: [PlatformUsersFacade],
  templateUrl: './platform-users.html',
  styleUrl: './platform-users.css',
})
export class PlatformUsers {
  private readonly router = inject(Router);

  protected readonly facade = inject(PlatformUsersFacade);

  /**
   * The organization filter, from `?org=`.
   *
   * In the URL so a filtered list is linkable and survives a reload — and so the organization
   * detail's Users tab can hand off to this page with the filter already applied.
   */
  readonly org = input<string>();

  protected readonly pageSize = PAGE_SIZE;

  /* Bound once as fields: a method reference created in a binding is a new function every tick. */
  protected readonly roleList = (roles: readonly string[]) => this.facade.roleList(roles);
  protected readonly orgLabel = (orgId: string | null) => this.facade.orgLabel(orgId);

  constructor() {
    /*
     * The URL is what a reload and a shared link restore the filter from. `onOrgFilter` is the other
     * writer, so this only carries what the page did not cause; the current value is read untracked
     * so this follows the URL rather than racing the state.
     */
    effect(() => {
      const requested = this.org() ?? '';
      if (requested !== untracked(() => this.facade.orgFilter())) {
        this.facade.setOrgFilter(requested);
      }
    });
  }

  /** The state leads and the URL mirrors it — a table that waited for the navigation would lag. */
  protected onOrgFilter(orgId: string): void {
    this.facade.setOrgFilter(orgId);
    void this.router.navigate([], {queryParams: {org: orgId || null}, queryParamsHandling: 'merge'});
  }

  protected onPage(page: number): void {
    this.facade.goToPage(page);
  }

  /** The shared table asks; this page decides how loudly. */
  protected onAction(intent: UserAdminIntent): void {
    switch (intent.kind) {
      case 'toggleEnabled':
        this.facade.toggleEnabled(intent.row);
        return;
      case 'resetPassword':
        this.facade.askReset(intent.row);
        return;
      case 'editRoles':
        this.facade.askEditRoles(intent.row);
        return;
      case 'delete':
        this.facade.askDelete(intent.row);
    }
  }

  protected userName(row: PlatformUserRow | null): string {
    return row?.name || row?.username || '';
  }
}
