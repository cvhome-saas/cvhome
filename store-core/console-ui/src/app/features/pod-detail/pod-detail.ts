import {Component, computed, effect, inject, input, untracked} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Badge, BusyOverlay, ConfirmDialog, CopyField, DataTable, type TableColumn, TableRow, EmptyState, Icon, LoadError, NoticeBar, PageHeader, Pagination, Panel, ProgressTrack, SearchBox} from '@cvhome-saas/ui-kit/ui';
import {PodFormDialog} from './components/pod-form-dialog/pod-form-dialog';
import {PodDetailFacade, STORES_PAGE_SIZE} from './facades/pod-detail.facade';

/** The stores table's columns. Widths are grid tracks, read straight into the row layout. */
const STORE_COLUMN_KEYS: readonly {key: string; labelKey: string; width: string}[] = [
  {key: 'store', labelKey: 'platform.pod.stores.column.store', width: 'minmax(12rem, 1.8fr)'},
  {key: 'organization', labelKey: 'platform.pod.stores.column.organization', width: 'minmax(9rem, 1.2fr)'},
  {key: 'status', labelKey: 'platform.pod.stores.column.status', width: 'minmax(6rem, 0.7fr)'},
  {key: 'provisioning', labelKey: 'platform.pod.stores.column.provisioning', width: 'minmax(7rem, 0.9fr)'},
  {key: 'billing', labelKey: 'platform.pod.stores.column.billing', width: 'minmax(6rem, 0.8fr)'},
];

/**
 * One pod — its state, where it routes, and the three levers over it — and the form that registers
 * a new one.
 *
 * **Drain is the safe operation and delete is not.** `PodApi.delete` checks nothing: there is no
 * foreign key from `manager_store.pod_id`, which lives in tenancy's schema, so deleting a populated
 * pod orphans every store on it and there is no undo. The delete dialog says so and asks the
 * operator to type the pod's name; the drain dialog says what draining does instead.
 *
 * **Editing is a dialog, not a panel.** The page's job is to show a pod's state, routing and
 * tenants; a form permanently at the foot of it made the page read as a form with facts above it.
 * The one writable thing on the page now sits with the other actions, in the header.
 *
 * `PodServiceImpl.update` reads `name` and `endpoint` off the body and ignores everything else, so
 * the dialog says which two fields it can change and renders the owner disabled on an edit.
 */
@Component({
  selector: 'app-pod-detail',
  imports: [
    Badge,
    BusyOverlay,
    ConfirmDialog,
    CopyField,
    DataTable,
    EmptyState,
    Icon,
    LoadError,
    NoticeBar,
    PageHeader,
    Pagination,
    Panel,
    PodFormDialog,
    ProgressTrack,
    RouterLink,
    SearchBox,
    TableRow,
    TranslocoDirective,
  ],
  providers: [PodDetailFacade],
  styleUrls: ['../../shared/styles/field.css', './pod-detail.css'],
  templateUrl: './pod-detail.html',
})
export class PodDetail {
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);

  protected readonly facade = inject(PodDetailFacade);

  protected readonly storesPageSize = STORES_PAGE_SIZE;

  /*
   * Bound once as fields: a method reference created in a binding is a new function every change detection.
   */
  protected readonly storeStatusLabel = (status: string | null) => this.facade.storeStatusLabel(status);
  protected readonly provisioningLabel = (state: string | null) => this.facade.provisioningLabel(state);
  protected readonly billingLabel = (status: string | null) => this.facade.billingLabel(status);
  protected readonly orgLabel = (orgId: string) => this.facade.orgLabel(orgId);

  protected readonly storeColumns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return STORE_COLUMN_KEYS.map((column) => ({
      key: column.key,
      label: this.transloco.translate(column.labelKey),
      width: column.width,
    }));
  });

  /** The store search term, from `?q=`, so a narrowed panel survives a reload and can be linked to. */
  readonly q = input<string>();

  /**
   * The `:id` route param, absent on `/platform/pods/new`.
   *
   * The static `new` segment is declared before `:id` in the route table, so this is only ever a
   * real pod id — a pod literally called "new" would still be reachable by its ObjectId.
   */
  readonly id = input<string>();

  constructor() {
    effect(() => this.facade.podId.set(this.id() ?? null));

    /*
     * The create route has nothing to read, so it opens straight into the dialog. Read untracked so
     * this does not re-open a form the operator has just cancelled.
     */
    effect(() => {
      if (this.facade.isNew() && !untracked(() => this.facade.editing())) {
        this.facade.startEdit();
      }
    });

    // The URL is what a reload and a shared link restore the store search from; `onStoreSearch` is
    // the other writer, so this only carries what the page did not cause.
    effect(() => {
      const term = this.q() ?? '';
      if (term !== untracked(() => this.facade.storeSearch())) {
        this.facade.setStoreSearch(term);
      }
    });
  }

  /** The state leads and the URL mirrors it, as everywhere else in this console. */
  protected onStoreSearch(term: string): void {
    this.facade.setStoreSearch(term);
    void this.router.navigate([], {queryParams: {q: term || null}, queryParamsHandling: 'merge'});
  }
}
