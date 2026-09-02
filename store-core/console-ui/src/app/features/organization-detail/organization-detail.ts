import {Component, computed, effect, inject, input, signal, untracked} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoDatePipe} from '@jsverse/transloco-locale';

import type {PlatformUserRow} from '@models/platform';
import {Badge, BusyOverlay, ConfirmDialog, CopyField, DataTable, type TableColumn, TableRow, EmptyState, Icon, LoadError, PageHeader, Pagination, Panel, RolesDialog, SectionNav, SetPasswordDialog, TabSwitcher, type TabItem, TextField} from '@cvhome-saas/ui-kit/ui';
import {UserAdminTable, type UserAdminIntent} from '@cvhome-saas/ui-kit/uaa';
import {SuspendOrgDialog} from './components/suspend-org-dialog/suspend-org-dialog';
import {ORG_SECTIONS, OrganizationDetailFacade, PAGE_SIZE, type OrgSection} from './facades/organization-detail.facade';

const STORE_COLUMN_KEYS: readonly {key: string; labelKey: string; width: string}[] = [
  {key: 'store', labelKey: 'platform.organization.stores.column.store', width: 'minmax(12rem, 2fr)'},
  {key: 'status', labelKey: 'platform.organization.stores.column.status', width: 'minmax(6rem, 0.7fr)'},
  {key: 'provisioning', labelKey: 'platform.organization.stores.column.provisioning', width: 'minmax(7rem, 0.9fr)'},
  {key: 'billing', labelKey: 'platform.organization.stores.column.billing', width: 'minmax(6rem, 0.8fr)'},
  {key: 'pod', labelKey: 'platform.organization.stores.column.pod', width: 'minmax(7rem, 1fr)'},
];

/** Which tab keys the route accepts. Anything else settles on `overview`. */
const SECTION_KEYS = new Set<string>(ORG_SECTIONS.map((section) => section.key));

/**
 * One organization: who it is, what it owns, who administers it, and what an operator may do to it.
 *
 * **The tabs are route segments**, so a tab is linkable and survives a reload — the same call
 * store management and billing made, and the replacement for seller-ui's `ORG_SIDEMENU_LINKS`, a
 * `<select>` used as navigation between three separate pages.
 *
 * **Activity is built and empty on purpose.** `tenancy_audit` records every rename and every
 * lifecycle move with an actor, a from-state and a to-state, and no endpoint reads it. The tab says
 * so rather than being left out, because the rows exist and the endpoint is a small addition — see
 * lessons.md, "Organizations — no audit read".
 */
@Component({
  selector: 'app-organization-detail',
  imports: [
    Badge,
    BusyOverlay,
    ConfirmDialog,
    CopyField,
    DataTable,
    EmptyState,
    Icon,
    LoadError,
    PageHeader,
    Pagination,
    Panel,
    RolesDialog,
    RouterLink,
    SectionNav,
    SetPasswordDialog,
    SuspendOrgDialog,
    TabSwitcher,
    TableRow,
    TextField,
    TranslocoDatePipe,
    TranslocoDirective,
    UserAdminTable,
  ],
  providers: [OrganizationDetailFacade],
  templateUrl: './organization-detail.html',
  styleUrls: ['../../shared/styles/field.css', './organization-detail.css'],
})
export class OrganizationDetail {
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);

  protected readonly facade = inject(OrganizationDetailFacade);

  /** The `:id` route param. Bound by the router, not read from `ActivatedRoute`. */
  readonly id = input.required<string>();
  /** The `:section` route param. An unknown value settles on `overview` in the effect below. */
  readonly section = input<string>();

  /**
   * Whether the section rail is folded to icons.
   *
   * Held here rather than in the rail because the width it animates is a column of *this* grid.
   * Not persisted: nothing on the platform stores operator preferences.
   */
  protected readonly railCollapsed = signal(false);

  protected readonly pageSize = PAGE_SIZE;

  /*
   * Bound once as fields rather than as `facade.x.bind(facade)` in the template: a method reference
   * created in a binding is a new function every change detection, which makes a child's inputs look
   * changed on every tick.
   */
  protected readonly roleList = (roles: readonly string[]) => this.facade.roleList(roles);
  protected readonly storeStatusLabel = (status: string | null) => this.facade.storeStatusLabel(status);
  protected readonly provisioningLabel = (state: string | null) => this.facade.provisioningLabel(state);
  protected readonly billingLabel = (status: string | null) => this.facade.billingLabel(status);

  protected readonly storeColumns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return STORE_COLUMN_KEYS.map((column) => ({
      key: column.key,
      label: this.transloco.translate(column.labelKey),
      width: column.width,
    }));
  });

  /** The narrow-layout equivalent of the rail. Same keys, same order. */
  protected readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    return ORG_SECTIONS.map((section) => ({key: section.key, label: this.transloco.translate(section.labelKey)}));
  });

  constructor() {
    // The route is the single source of both. `id` cannot change without the component being reused,
    // and when it is, everything keyed on it re-reads.
    effect(() => this.facade.orgId.set(this.id()));

    /*
     * An unknown segment settles on `overview` here rather than being matched in the route: a fixed
     * `:section` list there would make adding a tab a two-file change, which is the call store
     * management and the catalogue both made.
     */
    effect(() => {
      const requested = this.section() ?? 'overview';
      const settled = (SECTION_KEYS.has(requested) ? requested : 'overview') as OrgSection;
      if (settled !== untracked(() => this.facade.section())) {
        this.facade.section.set(settled);
      }
    });
  }

  /** The tab strip's writer. The rail's links navigate on their own. */
  protected pickSection(key: string): void {
    void this.router.navigate(['/platform/organizations', this.id(), key]);
  }

  protected onStoresPage(page: number): void {
    this.facade.storesPage.set(page);
  }

  protected onUsersPage(page: number): void {
    this.facade.usersPage.set(page);
  }

  /** The shared table asks; this page decides how loudly. */
  protected onUserAction(intent: UserAdminIntent): void {
    switch (intent.kind) {
      case 'toggleEnabled':
        this.facade.toggleUserEnabled(intent.row);
        return;
      case 'resetPassword':
        this.facade.askResetUser(intent.row);
        return;
      case 'editRoles':
        this.facade.askEditRoles(intent.row);
        return;
      case 'delete':
        this.facade.askDeleteUser(intent.row);
    }
  }

  /** The organization every account on this tab belongs to — so the column has nothing to add. */
  protected readonly showScope = false;

  protected userName(row: PlatformUserRow | null): string {
    return row?.name || row?.username || '';
  }
}
