import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {CreateOrgUser} from '@api/tenancy/org.service';
import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import {ORG_STATUSES, ORG_STATUS_TONE, type OrgRow} from '@models/platform';
import type {Tone} from '@cvhome-saas/ui-kit';
import {PlatformLabel} from '@shared/i18n/platform-label';
import type {SelectOption} from '@shared/ui/select/select';
import {ToastService} from '@shared/ui/toast/toast';
import {OrganizationsApi} from '../services/organizations.api.service';

export const PAGE_SIZE = 20;

/**
 * The tenant registry: every organization on the platform.
 *
 * **The filtering is the server's.** `POST org-manager/list` takes a term matched against the name
 * and the contact email, plus a status — so the box narrows the whole registry rather than the page
 * on screen, which is the only version of a search box worth having over a paged list.
 *
 * Nothing here is store-scoped. This is the one part of the console that is not a reading of one
 * shop, which is also why the page carries no store name in its header.
 */
@Injectable()
export class OrganizationsFacade {
  private readonly api = inject(OrganizationsApi);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly labels = inject(PlatformLabel);

  readonly busy = signal(false);

  /** Whether the create dialog is open. */
  readonly creating = signal(false);

  /**
   * The search term, as sent to the server.
   *
   * One box against one parameter: the server's `term` already spans the name and the contact email,
   * so — unlike the orders page, which routes its term to one of three fields by shape — there is
   * nothing here to choose between.
   */
  readonly search = signal('');

  /** The status filter, or `''` for any. */
  readonly status = signal('');

  /**
   * The page being read.
   *
   * A `linkedSignal` over the term **and** the status, so narrowing the list drops the reader back to
   * the first page rather than asking for page 4 of a smaller result.
   */
  readonly pageIndex = linkedSignal<unknown, number>({
    source: () => [this.search(), this.status()] as const,
    computation: () => 0,
  });

  private readonly orgs = snapshot(
    () => ({page: this.pageIndex(), count: PAGE_SIZE, term: this.search(), status: this.status()}),
    (query) => this.api.loadOrgs(query),
  );

  readonly isLoading = this.orgs.isLoading;
  readonly error = this.orgs.error;
  readonly isEmpty = this.orgs.isEmpty;
  readonly reload = () => this.orgs.reload();

  readonly rows = computed<readonly OrgRow[]>(() => this.orgs.value()?.rows ?? []);
  readonly totalElements = computed(() => this.orgs.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.orgs.value()?.totalPages ?? 0);

  /** Whether anything is narrowing the list. Drives which empty state the page shows. */
  readonly filtered = computed(() => !!this.search().trim() || !!this.status());

  /**
   * Whether the rows on screen answer the term in the box.
   *
   * False while a new term is in flight, because the last good rows stay up in the meantime. Only the
   * empty state reads it — telling an operator "nothing matched" over the previous query's results is
   * the mistake this prevents, and Module 9 shipped the same guard on the customers page.
   */
  readonly rowsMatchSearch = computed(() => this.orgs.value()?.term === this.search());

  /** The status filter's options. The empty value is every organization, the page's resting state. */
  readonly statusOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('platform.organizations.filter.allStatuses')},
      ...[...ORG_STATUSES].map((status) => ({value: status, label: this.labels.orgStatus(status)})),
    ];
  });

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('platform.organizations.heading.title'),
      context: this.transloco.translate('platform.organizations.heading.context'),
    };
  });

  /** The count as the header carries it, or null before the first response. */
  readonly totalLabel = computed<string | null>(() => {
    if (this.isEmpty()) {
      return null;
    }
    this.transloco.activeLang();
    return this.transloco.translate('platform.organizations.totalLabel', {
      total: this.localeFormat.localizeNumber(this.totalElements(), 'decimal'),
      count: this.totalElements(),
    });
  });

  /** What the table is showing right now, under the panel title. */
  readonly subtitle = computed(() => {
    this.transloco.activeLang();
    const shown = this.rows().length;
    if (!shown) {
      return this.transloco.translate('platform.organizations.subtitle.none');
    }
    const digits = (value: number) => this.localeFormat.localizeNumber(value, 'decimal');
    const from = this.pageIndex() * PAGE_SIZE + 1;
    return this.transloco.translate('platform.organizations.subtitle.range', {
      from: digits(from),
      to: digits(from + shown - 1),
      total: digits(this.totalElements()),
      count: this.totalElements(),
    });
  });

  statusLabel(status: string | null): string {
    return this.labels.orgStatus(status);
  }

  /** Known statuses keep their categorical colour; anything new stays neutral. */
  statusTone(status: string | null): Tone {
    return (status && ORG_STATUS_TONE[status as keyof typeof ORG_STATUS_TONE]) || 'slate';
  }

  setSearch(term: string): void {
    this.search.set(term);
  }

  setStatus(status: string): void {
    this.status.set(status);
  }

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  startCreate(): void {
    this.creating.set(true);
  }

  dismissCreate(): void {
    this.creating.set(false);
  }

  /**
   * Creates an organization and its first administrator.
   *
   * The response carries the created uaa account and is deliberately ignored: the list re-reads, so
   * the row shown is the one tenancy stored rather than the one the operator typed. The new
   * organization has **no name** — nothing sets one at creation — so it appears under its contact
   * email until someone renames it on the detail page. The dialog says so.
   */
  create(user: CreateOrgUser): void {
    if (this.busy()) {
      return;
    }
    this.busy.set(true);
    this.api.create(user).subscribe({
      next: () => {
        this.busy.set(false);
        this.creating.set(false);
        this.toast.success(
          this.transloco.translate('platform.organizations.toast.created', {email: user.emailAddress}),
        );
        this.orgs.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        /*
         * A duplicate email arrives as `UaaConflictException` — a 409 with no `fieldErrors[]`, the
         * same shape signup hits — so the toast is what carries it. The dialog stays open with the
         * operator's input intact.
         */
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }
}
